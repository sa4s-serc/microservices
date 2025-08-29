package cancel.service;

import edu.fudan.common.util.Response;
import org.springframework.http.HttpHeaders;

/**
 * @author fdse
 */
public interface CancelService {

    Response calculateRefund(String orderId, HttpHeaders headers);

    Response cancelOrder(String orderId, String loginId, HttpHeaders headers);
}