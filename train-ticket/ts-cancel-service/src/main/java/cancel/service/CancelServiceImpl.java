package cancel.service;

import cancel.entity.GetAccountByIdInfo;
import cancel.entity.GetAccountByIdResult;
import cancel.entity.GetOrderByIdInfo;
import edu.fudan.common.entity.NotifyInfo;
import edu.fudan.common.entity.Order;
import edu.fudan.common.entity.OrderStatus;
import edu.fudan.common.entity.User;
import edu.fudan.common.util.Response;
import edu.fudan.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private String getServiceUrl(String serviceName) {
        return "http://" + serviceName;
    }

    @Override
    public Response calculateRefund(String orderId, HttpHeaders headers) {
        LOGGER.info("[calculateRefund][Calculate Refund For OrderId: {}]", orderId);
        
        Response<Order> orderResult = getOrderById(orderId, headers);
        if (orderResult.getStatus() == 0) {
            LOGGER.warn("[calculateRefund][Order Not Found][OrderId: {}]", orderId);
            return new Response<>(0, "Order Not Found", null);
        }

        Order order = orderResult.getData();
        if (order.getStatus() == OrderStatus.NOTPAID.getCode()) {
            LOGGER.info("[calculateRefund][Order Not Paid][OrderId: {}][Refund: 0.0]", orderId);
            return new Response<>(1, "Success", "0.0");
        }

        if (order.getStatus() == OrderStatus.PAID.getCode() || order.getStatus() == OrderStatus.CHANGE.getCode()) {
            Date travelDate = StringUtils.String2Date(order.getTravelDate());
            Date currentDate = new Date();
            
            // If travel date is in the past, no refund
            if (travelDate.before(currentDate)) {
                LOGGER.info("[calculateRefund][Travel Date Passed][OrderId: {}][Refund: 0.0]", orderId);
                return new Response<>(1, "Success", "0.0");
            }

            // Calculate 80% refund
            double originalPrice = Double.parseDouble(order.getPrice());
            double refundAmount = originalPrice * 0.8;
            String refund = String.format("%.2f", refundAmount);
            
            LOGGER.info("[calculateRefund][Refund Calculated][OrderId: {}][Original: {}][Refund: {}]", 
                       orderId, order.getPrice(), refund);
            return new Response<>(1, "Success", refund);
        }

        LOGGER.warn("[calculateRefund][Order Cannot Be Cancelled][OrderId: {}][Status: {}]", orderId, order.getStatus());
        return new Response<>(0, "Order Cannot Be Cancelled", "0.0");
    }

    @Override
    public Response cancelOrder(String orderId, String loginId, HttpHeaders headers) {
        LOGGER.info("[cancelOrder][Cancel Order][OrderId: {}][LoginId: {}]", orderId, loginId);
        
        // Get order information
        Response<Order> orderResult = getOrderById(orderId, headers);
        if (orderResult.getStatus() == 0) {
            LOGGER.warn("[cancelOrder][Order Not Found][OrderId: {}]", orderId);
            return new Response<>(0, "Order Not Found", null);
        }

        Order order = orderResult.getData();
        
        // Check if order can be cancelled
        if (order.getStatus() != OrderStatus.NOTPAID.getCode() && 
            order.getStatus() != OrderStatus.PAID.getCode() && 
            order.getStatus() != OrderStatus.CHANGE.getCode()) {
            LOGGER.warn("[cancelOrder][Order Cannot Be Cancelled][OrderId: {}][Status: {}]", orderId, order.getStatus());
            return new Response<>(0, "Order Cannot Be Cancelled", null);
        }

        // Calculate refund amount
        Response refundResult = calculateRefund(orderId, headers);
        if (refundResult.getStatus() == 0) {
            return refundResult;
        }

        String refundAmount = (String) refundResult.getData();
        double refund = Double.parseDouble(refundAmount);

        // Process refund if amount > 0
        if (refund > 0) {
            LOGGER.info("[cancelOrder][Processing Refund][OrderId: {}][Amount: {}]", orderId, refundAmount);
            Response drawBackResult = drawBackMoney(order.getAccountId(), refundAmount, headers);
            if (drawBackResult.getStatus() == 0) {
                LOGGER.error("[cancelOrder][Refund Failed][OrderId: {}]", orderId);
                return new Response<>(0, "Refund Failed", null);
            }
        }

        // Update order status to CANCEL
        order.setStatus(OrderStatus.CANCEL.getCode());
        Response updateResult = updateOrderStatus(order, headers);
        if (updateResult.getStatus() == 0) {
            LOGGER.error("[cancelOrder][Update Order Status Failed][OrderId: {}]", orderId);
            return new Response<>(0, "Update Order Status Failed", null);
        }

        // TODO: Send notification email (commented out as per requirements)
        // sendCancelNotification(order, headers);

        LOGGER.info("[cancelOrder][Order Cancelled Successfully][OrderId: {}][Refund: {}]", orderId, refundAmount);
        return new Response<>(1, "Cancel Success", refundAmount);
    }

    private Response<Order> getOrderById(String orderId, HttpHeaders headers) {
        LOGGER.info("[getOrderById][Get Order By Id][OrderId: {}]", orderId);
        
        // First try to get from ts-order-service (for G/D trains)
        try {
            HttpEntity requestEntity = new HttpEntity(headers);
            ResponseEntity<Response> response = restTemplate.exchange(
                getServiceUrl("ts-order-service") + "/api/v1/orderservice/order/" + orderId,
                HttpMethod.GET, requestEntity, Response.class);
            
            if (response.getBody() != null && response.getBody().getStatus() == 1) {
                LOGGER.info("[getOrderById][Found in order-service][OrderId: {}]", orderId);
                return response.getBody();
            }
        } catch (Exception e) {
            LOGGER.warn("[getOrderById][Error accessing order-service][OrderId: {}][Error: {}]", orderId, e.getMessage());
        }

        // If not found, try ts-order-other-service (for other trains)
        try {
            HttpEntity requestEntity = new HttpEntity(headers);
            ResponseEntity<Response> response = restTemplate.exchange(
                getServiceUrl("ts-order-other-service") + "/api/v1/orderOtherService/orderOther/" + orderId,
                HttpMethod.GET, requestEntity, Response.class);
            
            if (response.getBody() != null && response.getBody().getStatus() == 1) {
                LOGGER.info("[getOrderById][Found in order-other-service][OrderId: {}]", orderId);
                return response.getBody();
            }
        } catch (Exception e) {
            LOGGER.warn("[getOrderById][Error accessing order-other-service][OrderId: {}][Error: {}]", orderId, e.getMessage());
        }

        LOGGER.warn("[getOrderById][Order Not Found In Any Service][OrderId: {}]", orderId);
        return new Response<>(0, "Order Not Found", null);
    }

    private Response drawBackMoney(String userId, String money, HttpHeaders headers) {
        LOGGER.info("[drawBackMoney][Draw Back Money][UserId: {}][Amount: {}]", userId, money);
        
        try {
            HttpEntity requestEntity = new HttpEntity(headers);
            ResponseEntity<Response> response = restTemplate.exchange(
                getServiceUrl("ts-inside-payment-service") + "/api/v1/inside_pay_service/inside_payment/drawback/" + userId + "/" + money,
                HttpMethod.GET, requestEntity, Response.class);
            
            if (response.getBody() != null) {
                LOGGER.info("[drawBackMoney][Drawback Result][UserId: {}][Status: {}]", userId, response.getBody().getStatus());
                return response.getBody();
            }
        } catch (Exception e) {
            LOGGER.error("[drawBackMoney][Error][UserId: {}][Error: {}]", userId, e.getMessage());
        }
        
        return new Response<>(0, "Drawback Failed", null);
    }

    private Response updateOrderStatus(Order order, HttpHeaders headers) {
        LOGGER.info("[updateOrderStatus][Update Order Status][OrderId: {}][Status: {}]", order.getId(), order.getStatus());
        
        String trainNumber = order.getTrainNumber();
        boolean isHighSpeedTrain = trainNumber.startsWith("G") || trainNumber.startsWith("D");
        
        try {
            HttpEntity<Order> requestEntity = new HttpEntity<>(order, headers);
            String serviceUrl;
            String endpoint;
            
            if (isHighSpeedTrain) {
                serviceUrl = getServiceUrl("ts-order-service");
                endpoint = "/api/v1/orderservice/order";
            } else {
                serviceUrl = getServiceUrl("ts-order-other-service");
                endpoint = "/api/v1/orderOtherService/orderOther";
            }
            
            ResponseEntity<Response> response = restTemplate.exchange(
                serviceUrl + endpoint, HttpMethod.PUT, requestEntity, Response.class);
            
            if (response.getBody() != null) {
                LOGGER.info("[updateOrderStatus][Update Result][OrderId: {}][Status: {}]", order.getId(), response.getBody().getStatus());
                return response.getBody();
            }
        } catch (Exception e) {
            LOGGER.error("[updateOrderStatus][Error][OrderId: {}][Error: {}]", order.getId(), e.getMessage());
        }
        
        return new Response<>(0, "Update Failed", null);
    }

    // TODO: Implement notification service call when async messaging is ready
    // private void sendCancelNotification(Order order, HttpHeaders headers) {
    //     try {
    //         Response<User> userResult = getUserById(order.getAccountId(), headers);
    //         if (userResult.getStatus() == 1) {
    //             NotifyInfo notifyInfo = new NotifyInfo();
    //             notifyInfo.setEmail(userResult.getData().getEmail());
    //             notifyInfo.setOrderNumber(order.getId());
    //             notifyInfo.setUsername(userResult.getData().getUserName());
    //             
    //             HttpEntity<NotifyInfo> requestEntity = new HttpEntity<>(notifyInfo, headers);
    //             restTemplate.postForObject(
    //                 getServiceUrl("ts-notification-service") + "/api/v1/notifyservice/notification/order_cancel_success",
    //                 requestEntity, Response.class);
    //             
    //             LOGGER.info("[sendCancelNotification][Notification Sent][OrderId: {}]", order.getId());
    //         }
    //     } catch (Exception e) {
    //         LOGGER.warn("[sendCancelNotification][Error][OrderId: {}][Error: {}]", order.getId(), e.getMessage());
    //     }
    // }

    private Response<User> getUserById(String userId, HttpHeaders headers) {
        LOGGER.info("[getUserById][Get User By Id][UserId: {}]", userId);
        
        try {
            HttpEntity requestEntity = new HttpEntity(headers);
            ResponseEntity<Response> response = restTemplate.exchange(
                getServiceUrl("ts-user-service") + "/api/v1/userservice/users/id/" + userId,
                HttpMethod.GET, requestEntity, Response.class);
            
            if (response.getBody() != null) {
                LOGGER.info("[getUserById][User Found][UserId: {}][Status: {}]", userId, response.getBody().getStatus());
                return response.getBody();
            }
        } catch (Exception e) {
            LOGGER.warn("[getUserById][Error][UserId: {}][Error: {}]", userId, e.getMessage());
        }
        
        return new Response<>(0, "User Not Found", null);
    }
}