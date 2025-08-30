package other.init;

import edu.fudan.common.entity.OrderStatus;
import edu.fudan.common.entity.SeatClass;
import edu.fudan.common.util.StringUtils;
import other.entity.Order;
import other.service.OrderOtherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author fdse
 */
@Component
public class InitData implements CommandLineRunner {

    @Autowired
    OrderOtherService orderOtherService;

    @Override
    public void run(String... args) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        
        Order order1 = new Order();
        order1.setId("5ad7750b-a68b-49c0-a8c0-32776b067703");
        order1.setTrainNumber("K1345");
        order1.setAccountId("4d2a46c7-71cb-4cf1-b5bb-b68406d9da6f");
        order1.setBoughtDate("2013-05-04 09:51:52");
        order1.setTravelDate("2013-05-04");
        order1.setTravelTime("09:51:52");
        order1.setCoachNumber(5);
        order1.setSeatClass(SeatClass.HARDSEAT.getCode());
        order1.setSeatNumber("1");
        order1.setFrom("nanjing");
        order1.setTo("shanghaihongqiao");
        order1.setStatus(OrderStatus.PAID.getCode());
        order1.setPrice("100.0");
        order1.setContactsName("Contacts_One");
        order1.setContactsDocumentNumber("DocumentNumber_One");
        order1.setDocumentType(1);
        orderOtherService.initOrder(order1, headers);

        Order order2 = new Order();
        order2.setId("3f357241-7c01-4a3e-8081-605aed98563f");
        order2.setTrainNumber("K1346");
        order2.setAccountId("4d2a46c7-71cb-4cf1-b5bb-b68406d9da6f");
        order2.setBoughtDate("2013-05-04 09:51:52");
        order2.setTravelDate("2013-05-04");
        order2.setTravelTime("09:51:52");
        order2.setCoachNumber(5);
        order2.setSeatClass(SeatClass.HARDSEAT.getCode());
        order2.setSeatNumber("2");
        order2.setFrom("nanjing");
        order2.setTo("shanghaihongqiao");
        order2.setStatus(OrderStatus.PAID.getCode());
        order2.setPrice("100.0");
        order2.setContactsName("Contacts_One");
        order2.setContactsDocumentNumber("DocumentNumber_One");
        order2.setDocumentType(1);
        orderOtherService.initOrder(order2, headers);
    }
}