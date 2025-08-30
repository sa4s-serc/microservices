package cancel.service;

import edu.fudan.common.entity.NotifyInfo;
import edu.fudan.common.entity.User;
import edu.fudan.common.util.Response;
import org.springframework.http.HttpHeaders;

/**
 * @author fdse
 */
public interface CancelService {

    Response calculateRefund(String orderId, HttpHeaders headers);

    Response cancelOrder(String orderId, String loginId, HttpHeaders headers);

    Response<User> getAccount(String accountId, HttpHeaders headers);

    Boolean sendEmail(NotifyInfo notifyInfo, HttpHeaders headers);

    Boolean drawbackMoney(String money, String userId, HttpHeaders headers);
}