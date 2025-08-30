package cancel.entity;

import lombok.Data;

/**
 * @author fdse
 */
@Data
public class GetAccountByIdInfo {
    private String orderId;

    public GetAccountByIdInfo() {
        // Default constructor
    }

    public GetAccountByIdInfo(String orderId) {
        this.orderId = orderId;
    }
}