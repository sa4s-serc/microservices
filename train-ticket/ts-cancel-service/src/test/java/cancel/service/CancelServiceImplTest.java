package cancel.service;

import edu.fudan.common.entity.Order;
import edu.fudan.common.entity.OrderStatus;
import edu.fudan.common.util.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CancelServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CancelServiceImpl cancelService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCalculateRefundNotPaidOrder() {
        // Arrange
        Order order = new Order();
        order.setId("order123");
        order.setStatus(OrderStatus.NOTPAID.getCode());
        order.setPrice("100.00");

        Response<Order> orderResponse = new Response<>(1, "Success", order);
        ResponseEntity<Response> responseEntity = ResponseEntity.ok(orderResponse);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Response.class)))
                .thenReturn(responseEntity);

        // Act
        Response result = cancelService.calculateRefund("order123", new HttpHeaders());

        // Assert
        assertEquals(1, result.getStatus());
        assertEquals("0.0", result.getData());
    }

    @Test
    void testCalculateRefundPaidOrder() {
        // Arrange
        Order order = new Order();
        order.setId("order123");
        order.setStatus(OrderStatus.PAID.getCode());
        order.setPrice("100.00");
        order.setTravelDate("2025-12-31");

        Response<Order> orderResponse = new Response<>(1, "Success", order);
        ResponseEntity<Response> responseEntity = ResponseEntity.ok(orderResponse);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Response.class)))
                .thenReturn(responseEntity);

        // Act
        Response result = cancelService.calculateRefund("order123", new HttpHeaders());

        // Assert
        assertEquals(1, result.getStatus());
        assertEquals("80.00", result.getData());
    }

    @Test
    void testCalculateRefundOrderNotFound() {
        // Arrange
        Response<Order> orderResponse = new Response<>(0, "Order Not Found", null);
        ResponseEntity<Response> responseEntity = ResponseEntity.ok(orderResponse);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Response.class)))
                .thenReturn(responseEntity);

        // Act
        Response result = cancelService.calculateRefund("order123", new HttpHeaders());

        // Assert
        assertEquals(0, result.getStatus());
        assertEquals("Order Not Found", result.getMsg());
    }

    @Test
    void testCancelOrderSuccess() {
        // Arrange
        Order order = new Order();
        order.setId("order123");
        order.setStatus(OrderStatus.PAID.getCode());
        order.setPrice("100.00");
        order.setTravelDate("2025-12-31");
        order.setAccountId("user456");
        order.setTrainNumber("G1234");

        // Mock order retrieval
        Response<Order> orderResponse = new Response<>(1, "Success", order);
        ResponseEntity<Response> orderResponseEntity = ResponseEntity.ok(orderResponse);

        // Mock drawback
        Response<String> drawbackResponse = new Response<>(1, "Success", null);
        ResponseEntity<Response> drawbackResponseEntity = ResponseEntity.ok(drawbackResponse);

        // Mock order update
        Response<Order> updateResponse = new Response<>(1, "Success", order);
        ResponseEntity<Response> updateResponseEntity = ResponseEntity.ok(updateResponse);

        when(restTemplate.exchange(contains("/order/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Response.class)))
                .thenReturn(orderResponseEntity);
        when(restTemplate.exchange(contains("/drawback/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Response.class)))
                .thenReturn(drawbackResponseEntity);
        when(restTemplate.exchange(contains("/orderservice/order"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Response.class)))
                .thenReturn(updateResponseEntity);

        // Act
        Response result = cancelService.cancelOrder("order123", "user456", new HttpHeaders());

        // Assert
        assertEquals(1, result.getStatus());
        assertEquals("Cancel Success", result.getMsg());
        assertEquals("80.00", result.getData());
    }
}