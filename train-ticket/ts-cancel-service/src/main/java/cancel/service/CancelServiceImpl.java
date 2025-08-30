package cancel.service;

import edu.fudan.common.entity.NotifyInfo;
import edu.fudan.common.entity.Order;
import edu.fudan.common.entity.OrderStatus;
import edu.fudan.common.entity.User;
import edu.fudan.common.util.Response;
import edu.fudan.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    @Override
    public Response calculateRefund(String orderId, HttpHeaders headers) {
        LOGGER.info("[calculateRefund][Calculate Refund][OrderId: {}]", orderId);
        
        Response<Order> orderResult = getOrderByIdFromOrder(orderId, headers);
        
        if (orderResult.getStatus() == 0) {
            LOGGER.info("[calculateRefund][Order not found in ts-order-service, checking ts-order-other-service][OrderId: {}]", orderId);
            orderResult = getOrderByIdFromOrderOther(orderId, headers);
        }
        
        if (orderResult.getStatus() == 0) {
            LOGGER.warn("[calculateRefund][Order not found in both services][OrderId: {}]", orderId);
            return new Response<>(0, "Order Not Found", null);
        }
        
        Order order = orderResult.getData();
        
        if (order.getStatus() == OrderStatus.USED.getCode() || 
            order.getStatus() == OrderStatus.COLLECTED.getCode() ||
            order.getStatus() == OrderStatus.CANCEL.getCode() ||
            order.getStatus() == OrderStatus.REFUNDS.getCode()) {
            LOGGER.warn("[calculateRefund][Order status does not permit cancellation][OrderId: {}, Status: {}]", orderId, order.getStatus());
            return new Response<>(0, "Order Status Cancel Not Permitted", null);
        }
        
        String refundMoney = calculateRefundAmount(order);
        
        LOGGER.info("[calculateRefund][Refund calculated][OrderId: {}, RefundAmount: {}]", orderId, refundMoney);
        return new Response<>(1, "Success", refundMoney);
    }

    @Override
    public Response cancelOrder(String orderId, String loginId, HttpHeaders headers) {
        LOGGER.info("[cancelOrder][Cancel Order][OrderId: {}, LoginId: {}]", orderId, loginId);
        
        Response<Order> orderResult = getOrderByIdFromOrder(orderId, headers);
        boolean isHighSpeedTrain = true;
        
        if (orderResult.getStatus() == 0) {
            LOGGER.info("[cancelOrder][Order not found in ts-order-service, checking ts-order-other-service][OrderId: {}]", orderId);
            orderResult = getOrderByIdFromOrderOther(orderId, headers);
            isHighSpeedTrain = false;
        }
        
        if (orderResult.getStatus() == 0) {
            LOGGER.warn("[cancelOrder][Order not found in both services][OrderId: {}]", orderId);
            return new Response<>(0, "Order Not Found", null);
        }
        
        Order order = orderResult.getData();
        
        if (order.getStatus() != OrderStatus.NOTPAID.getCode() && 
            order.getStatus() != OrderStatus.PAID.getCode() &&
            order.getStatus() != OrderStatus.CHANGE.getCode()) {
            LOGGER.warn("[cancelOrder][Order status does not permit cancellation][OrderId: {}, Status: {}]", orderId, order.getStatus());
            return new Response<>(0, "Order Status Cancel Not Permitted", null);
        }
        
        String refundMoney = calculateRefundAmount(order);
        
        order.setStatus(OrderStatus.CANCEL.getCode());
        
        boolean updateResult;
        if (isHighSpeedTrain) {
            updateResult = updateOrderInOrderService(order, headers);
        } else {
            updateResult = updateOrderInOrderOtherService(order, headers);
        }
        
        if (!updateResult) {
            LOGGER.error("[cancelOrder][Failed to update order status][OrderId: {}]", orderId);
            return new Response<>(0, "Cancel Failed", null);
        }
        
        if (!refundMoney.equals("0")) {
            boolean drawbackResult = drawbackMoney(refundMoney, order.getAccountId(), headers);
            if (!drawbackResult) {
                LOGGER.error("[cancelOrder][Failed to process refund][OrderId: {}, RefundAmount: {}]", orderId, refundMoney);
                return new Response<>(0, "Refund Failed", null);
            }
        }
        
        Response<User> userResult = getAccount(order.getAccountId(), headers);
        if (userResult.getStatus() == 1) {
            User user = userResult.getData();
            NotifyInfo notifyInfo = new NotifyInfo();
            notifyInfo.setEmail(user.getEmail());
            notifyInfo.setOrderNumber(orderId);
            notifyInfo.setUsername(user.getUserName());
            notifyInfo.setStartPlace(order.getFrom());
            notifyInfo.setEndPlace(order.getTo());
            notifyInfo.setStartTime(order.getTravelTime());
            notifyInfo.setDate(order.getTravelDate());
            notifyInfo.setPrice(refundMoney);
            
            // TODO: Enable email notification when async messaging is implemented
            // sendEmail(notifyInfo, headers);
        }
        
        LOGGER.info("[cancelOrder][Cancel order success][OrderId: {}, RefundAmount: {}]", orderId, refundMoney);
        return new Response<>(1, "Cancel Success", refundMoney);
    }

    @Override
    public Response<User> getAccount(String accountId, HttpHeaders headers) {
        HttpEntity requestEntity = new HttpEntity(headers);
        ResponseEntity<Response<User>> re = restTemplate.exchange(
                "http://ts-user-service/api/v1/userservice/users/id/" + accountId,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Response<User>>() {
                });
        return re.getBody();
    }

    @Override
    public Boolean sendEmail(NotifyInfo notifyInfo, HttpHeaders headers) {
        LOGGER.info("[sendEmail][Send email notification][Email: {}]", notifyInfo.getEmail());
        HttpEntity requestEntity = new HttpEntity(notifyInfo, headers);
        ResponseEntity<Boolean> re = restTemplate.exchange(
                "http://ts-notification-service/api/v1/notifyservice/notification/order_cancel_success",
                HttpMethod.POST,
                requestEntity,
                Boolean.class);
        return re.getBody();
    }

    @Override
    public Boolean drawbackMoney(String money, String userId, HttpHeaders headers) {
        LOGGER.info("[drawbackMoney][Process refund][UserId: {}, Amount: {}]", userId, money);
        HttpEntity requestEntity = new HttpEntity(headers);
        ResponseEntity<Response> re = restTemplate.exchange(
                "http://ts-inside-payment-service/api/v1/inside_pay_service/inside_payment/drawback/" + userId + "/" + money,
                HttpMethod.GET,
                requestEntity,
                Response.class);
        Response result = re.getBody();
        return result.getStatus() == 1;
    }

    private Response<Order> getOrderByIdFromOrder(String orderId, HttpHeaders headers) {
        LOGGER.info("[getOrderByIdFromOrder][Get order from ts-order-service][OrderId: {}]", orderId);
        HttpEntity requestEntity = new HttpEntity(headers);
        ResponseEntity<Response<Order>> re = restTemplate.exchange(
                "http://ts-order-service/api/v1/orderservice/order/" + orderId,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Response<Order>>() {
                });
        return re.getBody();
    }

    private Response<Order> getOrderByIdFromOrderOther(String orderId, HttpHeaders headers) {
        LOGGER.info("[getOrderByIdFromOrderOther][Get order from ts-order-other-service][OrderId: {}]", orderId);
        HttpEntity requestEntity = new HttpEntity(headers);
        ResponseEntity<Response<Order>> re = restTemplate.exchange(
                "http://ts-order-other-service/api/v1/orderOtherService/orderOther/" + orderId,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Response<Order>>() {
                });
        return re.getBody();
    }

    private boolean updateOrderInOrderService(Order order, HttpHeaders headers) {
        LOGGER.info("[updateOrderInOrderService][Update order in ts-order-service][OrderId: {}]", order.getId());
        HttpEntity requestEntity = new HttpEntity(order, headers);
        ResponseEntity<Response> re = restTemplate.exchange(
                "http://ts-order-service/api/v1/orderservice/order",
                HttpMethod.PUT,
                requestEntity,
                Response.class);
        Response result = re.getBody();
        return result.getStatus() == 1;
    }

    private boolean updateOrderInOrderOtherService(Order order, HttpHeaders headers) {
        LOGGER.info("[updateOrderInOrderOtherService][Update order in ts-order-other-service][OrderId: {}]", order.getId());
        HttpEntity requestEntity = new HttpEntity(order, headers);
        ResponseEntity<Response> re = restTemplate.exchange(
                "http://ts-order-other-service/api/v1/orderOtherService/orderOther",
                HttpMethod.PUT,
                requestEntity,
                Response.class);
        Response result = re.getBody();
        return result.getStatus() == 1;
    }

    private String calculateRefundAmount(Order order) {
        if (order.getStatus() == OrderStatus.NOTPAID.getCode()) {
            return "0";
        }
        
        if (order.getStatus() == OrderStatus.PAID.getCode() || order.getStatus() == OrderStatus.CHANGE.getCode()) {
            Date travelDate = StringUtils.String2Date(order.getTravelDate());
            Date currentDate = new Date(System.currentTimeMillis()); // NOSONAR
            
            if (currentDate.before(travelDate)) {
                double originalPrice = Double.parseDouble(order.getPrice());
                double refundAmount = originalPrice * 0.8;
                return String.valueOf(refundAmount);
            } else {
                return "0";
            }
        }
        
        return "0";
    }
}