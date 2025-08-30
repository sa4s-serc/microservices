package cancel.service;

import cancel.entity.GetAccountByIdInfo;
import cancel.entity.GetAccountByIdResult;
import cancel.entity.GetOrderByIdInfo;
import com.alibaba.fastjson.JSONObject;
import edu.fudan.common.entity.NotifyInfo;
import edu.fudan.common.entity.Order;
import edu.fudan.common.entity.OrderStatus;
import edu.fudan.common.util.Response;
import edu.fudan.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

/**
 * @author fdse
 */
@Service
public class CancelServiceImpl implements CancelService {

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(CancelServiceImpl.class);

    private static final String ORDER_SERVICE_URL = "http://ts-order-service:12031";
    private static final String ORDER_OTHER_SERVICE_URL = "http://ts-order-other-service:12032";
    private static final String INSIDE_PAYMENT_SERVICE_URL = "http://ts-inside-payment-service:18673";
    private static final String USER_SERVICE_URL = "http://ts-user-service:12342";
    private static final String NOTIFICATION_SERVICE_URL = "http://ts-notification-service:17853";

    @Override
    public Response calculateRefund(String orderId, HttpHeaders headers) {
        LOGGER.info("[calculateRefund][Calculate refund][orderId: {}]", orderId);

        try {
            Order order = getOrderByOrderId(orderId, headers);
            if (order == null) {
                LOGGER.warn("[calculateRefund][Order not found][orderId: {}]", orderId);
                return new Response<>(0, "Order not found.", null);
            }

            if (order.getStatus() != OrderStatus.PAID.getCode() && 
                order.getStatus() != OrderStatus.NOTPAID.getCode() && 
                order.getStatus() != OrderStatus.CHANGE.getCode()) {
                LOGGER.warn("[calculateRefund][Order status not refundable][orderId: {}, status: {}]", orderId, order.getStatus());
                return new Response<>(0, "Order status is not refundable.", null);
            }

            double refundAmount = 0.0;

            if (order.getStatus() == OrderStatus.NOTPAID.getCode()) {
                refundAmount = 0.0;
            } else if (order.getStatus() == OrderStatus.PAID.getCode() || order.getStatus() == OrderStatus.CHANGE.getCode()) {
                Date travelDate = StringUtils.String2Date(order.getTravelDate());
                Date currentDate = new Date();
                
                if (currentDate.before(travelDate)) {
                    double originalPrice = Double.parseDouble(order.getPrice());
                    refundAmount = originalPrice * 0.8;
                } else {
                    refundAmount = 0.0;
                }
            }

            LOGGER.info("[calculateRefund][Refund calculated][orderId: {}, amount: {}]", orderId, refundAmount);
            return new Response<>(1, "Success.", String.valueOf(refundAmount));

        } catch (Exception e) {
            LOGGER.error("[calculateRefund][Error calculating refund][orderId: {}][error: {}]", orderId, e.getMessage());
            return new Response<>(0, "Error calculating refund: " + e.getMessage(), null);
        }
    }

    @Override
    public Response cancelTicket(String orderId, String loginId, HttpHeaders headers) {
        LOGGER.info("[cancelTicket][Cancel ticket][orderId: {}, loginId: {}]", orderId, loginId);

        try {
            Order order = getOrderByOrderId(orderId, headers);
            if (order == null) {
                LOGGER.warn("[cancelTicket][Order not found][orderId: {}]", orderId);
                return new Response<>(0, "Order not found.", null);
            }

            if (order.getStatus() != OrderStatus.PAID.getCode() && 
                order.getStatus() != OrderStatus.NOTPAID.getCode() && 
                order.getStatus() != OrderStatus.CHANGE.getCode()) {
                LOGGER.warn("[cancelTicket][Order status not cancellable][orderId: {}, status: {}]", orderId, order.getStatus());
                return new Response<>(0, "Order status is not cancellable.", null);
            }

            double refundAmount = 0.0;
            if (order.getStatus() == OrderStatus.PAID.getCode() || order.getStatus() == OrderStatus.CHANGE.getCode()) {
                Date travelDate = StringUtils.String2Date(order.getTravelDate());
                @SuppressWarnings("deprecation")
                Date currentDate = new Date();
                
                if (currentDate.before(travelDate)) {
                    double originalPrice = Double.parseDouble(order.getPrice());
                    refundAmount = originalPrice * 0.8;
                }
            }

            boolean updateResult = updateOrderStatus(orderId, OrderStatus.CANCEL.getCode(), order.getTrainNumber(), headers);
            if (!updateResult) {
                LOGGER.error("[cancelTicket][Failed to update order status][orderId: {}]", orderId);
                return new Response<>(0, "Failed to update order status.", null);
            }

            if (refundAmount > 0) {
                boolean drawbackResult = processDrawback(order.getAccountId(), String.valueOf(refundAmount), headers);
                if (!drawbackResult) {
                    LOGGER.error("[cancelTicket][Failed to process refund][orderId: {}, amount: {}]", orderId, refundAmount);
                    return new Response<>(0, "Failed to process refund.", null);
                }
            }

            try {
                GetAccountByIdResult accountInfo = getUserAccountInfo(order.getAccountId(), headers);
                if (accountInfo != null) {
                    sendCancelNotification(order, accountInfo.getEmail(), headers);
                }
            } catch (Exception e) {
                LOGGER.warn("[cancelTicket][Failed to send notification][orderId: {}][error: {}]", orderId, e.getMessage());
            }

            LOGGER.info("[cancelTicket][Ticket cancelled successfully][orderId: {}, refund: {}]", orderId, refundAmount);
            return new Response<>(1, "Success.", "Ticket cancelled successfully. Refund amount: " + refundAmount);

        } catch (Exception e) {
            LOGGER.error("[cancelTicket][Error cancelling ticket][orderId: {}][error: {}]", orderId, e.getMessage());
            return new Response<>(0, "Error cancelling ticket: " + e.getMessage(), null);
        }
    }

    private Order getOrderByOrderId(String orderId, HttpHeaders headers) {
        LOGGER.info("[getOrderByOrderId][Get order][orderId: {}]", orderId);

        HttpEntity requestEntity = new HttpEntity(headers);

        try {
            String orderServiceUrl = ORDER_SERVICE_URL + "/api/v1/orderservice/order/" + orderId;
            ResponseEntity<Response<Order>> orderResponse = restTemplate.exchange(
                    orderServiceUrl,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Response<Order>>() {}
            );

            if (orderResponse.getBody() != null && orderResponse.getBody().getStatus() == 1) {
                LOGGER.info("[getOrderByOrderId][Found order in order service][orderId: {}]", orderId);
                return orderResponse.getBody().getData();
            }
        } catch (Exception e) {
            LOGGER.warn("[getOrderByOrderId][Order service error][orderId: {}][error: {}]", orderId, e.getMessage());
        }

        try {
            String orderOtherServiceUrl = ORDER_OTHER_SERVICE_URL + "/api/v1/orderOtherService/orderOther/" + orderId;
            ResponseEntity<Response<Order>> orderOtherResponse = restTemplate.exchange(
                    orderOtherServiceUrl,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Response<Order>>() {}
            );

            if (orderOtherResponse.getBody() != null && orderOtherResponse.getBody().getStatus() == 1) {
                LOGGER.info("[getOrderByOrderId][Found order in order-other service][orderId: {}]", orderId);
                return orderOtherResponse.getBody().getData();
            }
        } catch (Exception e) {
            LOGGER.warn("[getOrderByOrderId][Order-other service error][orderId: {}][error: {}]", orderId, e.getMessage());
        }

        LOGGER.warn("[getOrderByOrderId][Order not found in any service][orderId: {}]", orderId);
        return null;
    }

    private boolean updateOrderStatus(String orderId, int status, String trainNumber, HttpHeaders headers) {
        LOGGER.info("[updateOrderStatus][Update order status][orderId: {}, status: {}]", orderId, status);

        HttpEntity requestEntity = new HttpEntity(headers);

        try {
            if (trainNumber.startsWith("G") || trainNumber.startsWith("D")) {
                String updateUrl = ORDER_SERVICE_URL + "/api/v1/orderservice/order/status/" + orderId + "/" + status;
                ResponseEntity<Response> response = restTemplate.exchange(
                        updateUrl,
                        HttpMethod.GET,
                        requestEntity,
                        Response.class
                );
                return response.getBody() != null && response.getBody().getStatus() == 1;
            } else {
                String updateUrl = ORDER_OTHER_SERVICE_URL + "/api/v1/orderOtherService/orderOther/status/" + orderId + "/" + status;
                ResponseEntity<Response> response = restTemplate.exchange(
                        updateUrl,
                        HttpMethod.GET,
                        requestEntity,
                        Response.class
                );
                return response.getBody() != null && response.getBody().getStatus() == 1;
            }
        } catch (Exception e) {
            LOGGER.error("[updateOrderStatus][Error updating order status][orderId: {}][error: {}]", orderId, e.getMessage());
            return false;
        }
    }

    private boolean processDrawback(String userId, String money, HttpHeaders headers) {
        LOGGER.info("[processDrawback][Process drawback][userId: {}, money: {}]", userId, money);

        HttpEntity requestEntity = new HttpEntity(headers);

        try {
            String drawbackUrl = INSIDE_PAYMENT_SERVICE_URL + "/api/v1/inside_pay_service/inside_payment/drawback/" + userId + "/" + money;
            ResponseEntity<Response> response = restTemplate.exchange(
                    drawbackUrl,
                    HttpMethod.GET,
                    requestEntity,
                    Response.class
            );

            boolean success = response.getBody() != null && response.getBody().getStatus() == 1;
            LOGGER.info("[processDrawback][Drawback result][userId: {}, money: {}, success: {}]", userId, money, success);
            return success;
        } catch (Exception e) {
            LOGGER.error("[processDrawback][Error processing drawback][userId: {}, money: {}][error: {}]", userId, money, e.getMessage());
            return false;
        }
    }

    private GetAccountByIdResult getUserAccountInfo(String accountId, HttpHeaders headers) {
        LOGGER.info("[getUserAccountInfo][Get user account info][accountId: {}]", accountId);

        HttpEntity requestEntity = new HttpEntity(headers);

        try {
            String userServiceUrl = USER_SERVICE_URL + "/api/v1/userservice/users/id/" + accountId;
            ResponseEntity<Response<GetAccountByIdResult>> response = restTemplate.exchange(
                    userServiceUrl,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Response<GetAccountByIdResult>>() {}
            );

            if (response.getBody() != null && response.getBody().getStatus() == 1) {
                LOGGER.info("[getUserAccountInfo][User account info retrieved][accountId: {}]", accountId);
                return response.getBody().getData();
            }
        } catch (Exception e) {
            LOGGER.warn("[getUserAccountInfo][Error getting user account info][accountId: {}][error: {}]", accountId, e.getMessage());
        }

        return null;
    }

    private void sendCancelNotification(Order order, String email, HttpHeaders headers) {
        LOGGER.info("[sendCancelNotification][Send cancel notification][orderId: {}, email: {}]", order.getId(), email);

        try {
            NotifyInfo notifyInfo = new NotifyInfo();
            notifyInfo.setOrderNumber(order.getId());
            notifyInfo.setUsername(order.getContactsName());
            notifyInfo.setStartPlace(order.getFrom());
            notifyInfo.setEndPlace(order.getTo());
            notifyInfo.setStartTime(order.getTravelTime());
            notifyInfo.setDate(order.getTravelDate());
            notifyInfo.setSeatClass("" + order.getSeatClass());
            notifyInfo.setSeatNumber(order.getSeatNumber());
            notifyInfo.setPrice(order.getPrice());
            notifyInfo.setEmail(email);

            HttpEntity<NotifyInfo> requestEntity = new HttpEntity<>(notifyInfo, headers);
            String notificationUrl = NOTIFICATION_SERVICE_URL + "/api/v1/notifyservice/notification/order_cancel_success";
            
            ResponseEntity<Response> response = restTemplate.exchange(
                    notificationUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Response.class
            );

            if (response.getBody() != null && response.getBody().getStatus() == 1) {
                LOGGER.info("[sendCancelNotification][Notification sent successfully][orderId: {}]", order.getId());
            } else {
                LOGGER.warn("[sendCancelNotification][Failed to send notification][orderId: {}]", order.getId());
            }
        } catch (Exception e) {
            LOGGER.error("[sendCancelNotification][Error sending notification][orderId: {}][error: {}]", order.getId(), e.getMessage());
        }
    }
}