package other.service;

import edu.fudan.common.entity.*;
import edu.fudan.common.util.Response;
import other.entity.Order;
import other.entity.OrderAlterInfo;
import other.repository.OrderOtherRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(JUnit4.class)
public class OrderOtherServiceImplTest {

    @InjectMocks
    private OrderOtherServiceImpl orderOtherServiceImpl;

    @Mock
    private OrderOtherRepository orderOtherRepository;

    @Mock
    private RestTemplate restTemplate;

    private HttpHeaders headers = new HttpHeaders();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetSoldTickets1() {
        Seat seatRequest = new Seat();
        ArrayList<Order> list = new ArrayList<>();
        list.add(new Order());
        Mockito.when(orderOtherRepository.findByTravelDateAndTrainNumber(Mockito.any(String.class), Mockito.anyString())).thenReturn(list);
        Response result = orderOtherServiceImpl.getSoldTickets(seatRequest, headers);
        Assert.assertEquals("Success", result.getMsg());
    }

    @Test
    public void testGetSoldTickets2() {
        Seat seatRequest = new Seat();
        Mockito.when(orderOtherRepository.findByTravelDateAndTrainNumber(Mockito.any(String.class), Mockito.anyString())).thenReturn(null);
        Response result = orderOtherServiceImpl.getSoldTickets(seatRequest, headers);
        Assert.assertEquals("Order is Null.", result.getMsg());
    }

    @Test
    public void testFindOrderById1() {
        String id = UUID.randomUUID().toString();
        Mockito.when(orderOtherRepository.findById(Mockito.any(String.class))).thenReturn(java.util.Optional.empty());
        Response result = orderOtherServiceImpl.findOrderById(id, headers);
        Assert.assertEquals("No Content by this id", result.getMsg());
    }

    @Test
    public void testFindOrderById2() {
        String id = UUID.randomUUID().toString();
        Order order = new Order();
        Mockito.when(orderOtherRepository.findById(Mockito.any(String.class))).thenReturn(java.util.Optional.of(order));
        Response result = orderOtherServiceImpl.findOrderById(id, headers);
        Assert.assertEquals("Success", result.getMsg());
    }

    @Test
    public void testCreate1() {
        Order order = new Order();
        order.setAccountId("test_account");
        ArrayList<Order> accountOrders = new ArrayList<>();
        accountOrders.add(order);
        Mockito.when(orderOtherRepository.findByAccountId(Mockito.any(String.class))).thenReturn(accountOrders);
        Response result = orderOtherServiceImpl.create(order, headers);
        Assert.assertEquals("Order already exist", result.getMsg());
    }

    @Test
    public void testCreate2() {
        Order order = new Order();
        order.setAccountId("test_account");
        ArrayList<Order> accountOrders = new ArrayList<>();
        Mockito.when(orderOtherRepository.findByAccountId(Mockito.any(String.class))).thenReturn(accountOrders);
        Mockito.when(orderOtherRepository.save(Mockito.any(Order.class))).thenReturn(order);
        Response result = orderOtherServiceImpl.create(order, headers);
        Assert.assertEquals("Success", result.getMsg());
    }

    @Test
    public void testGetAllOrders1() {
        Mockito.when(orderOtherRepository.findAll()).thenReturn(null);
        Response result = orderOtherServiceImpl.getAllOrders(headers);
        Assert.assertEquals("No Content.", result.getMsg());
    }

    @Test
    public void testGetAllOrders2() {
        ArrayList<Order> orders = new ArrayList<>();
        Mockito.when(orderOtherRepository.findAll()).thenReturn(orders);
        Response result = orderOtherServiceImpl.getAllOrders(headers);
        Assert.assertEquals("No Content.", result.getMsg());
    }

    @Test
    public void testModifyOrder2() {
        Order order = new Order();
        Mockito.when(orderOtherRepository.findById(Mockito.any(String.class))).thenReturn(java.util.Optional.of(order));
        Mockito.when(orderOtherRepository.save(Mockito.any(Order.class))).thenReturn(null);
        Response result = orderOtherServiceImpl.modifyOrder(UUID.randomUUID().toString().toString(), 1, headers);
        Assert.assertEquals("Modify Order Success", result.getMsg());
    }

    @Test
    public void testPayOrder2() {
        Order order = new Order();
        Mockito.when(orderOtherRepository.findById(Mockito.any(String.class))).thenReturn(java.util.Optional.of(order));
        Mockito.when(orderOtherRepository.save(Mockito.any(Order.class))).thenReturn(null);
        Response result = orderOtherServiceImpl.payOrder(UUID.randomUUID().toString().toString(), headers);
        Assert.assertEquals("Pay Order Success.", result.getMsg());
    }

    @Test
    public void testGetOrderById2() {
        Order order = new Order();
        Mockito.when(orderOtherRepository.findById(Mockito.any(String.class))).thenReturn(java.util.Optional.of(order));
        Response result = orderOtherServiceImpl.getOrderById(UUID.randomUUID().toString().toString(), headers);
        Assert.assertEquals("Success.", result.getMsg());
    }

    @Test
    public void testCheckSecurityAboutOrder() {
        ArrayList<Order> orders = new ArrayList<>();
        Mockito.when(orderOtherRepository.findByAccountId(Mockito.any(String.class))).thenReturn(orders);
        Response result = orderOtherServiceImpl.checkSecurityAboutOrder(new Date(), UUID.randomUUID().toString().toString(), headers);
        Assert.assertEquals("Check Security Success . ", result.getMsg());
    }

    @Test
    public void testDeleteOrder2() {
        Order order = new Order();
        String orderUuid = UUID.randomUUID().toString();
        Mockito.when(orderOtherRepository.findById(Mockito.any(String.class))).thenReturn(java.util.Optional.of(order));
        Mockito.doNothing().doThrow(new RuntimeException()).when(orderOtherRepository).deleteById(Mockito.any(String.class));
        Response result = orderOtherServiceImpl.deleteOrder(orderUuid.toString(), headers);
        Assert.assertEquals("Delete Order Success", result.getMsg());
    }

    @Test
    public void testUpdateOrder1() {
        Order order = new Order();
        Mockito.when(orderOtherRepository.findById(Mockito.any(String.class))).thenReturn(java.util.Optional.empty());
        Response result = orderOtherServiceImpl.updateOrder(order, headers);
        Assert.assertEquals("Order Not Found, Can't update", result.getMsg());
    }

    @Test
    public void testUpdateOrder2() {
        Order order = new Order();
        order.setId("test_order_id");
        Mockito.when(orderOtherRepository.findById(Mockito.any(String.class))).thenReturn(java.util.Optional.of(order));
        Mockito.when(orderOtherRepository.save(Mockito.any(Order.class))).thenReturn(order);
        Response result = orderOtherServiceImpl.updateOrder(order, headers);
        Assert.assertEquals("Admin Update Order Success", result.getMsg());
    }
}