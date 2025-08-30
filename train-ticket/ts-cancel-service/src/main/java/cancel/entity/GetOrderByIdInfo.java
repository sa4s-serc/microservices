package cancel.entity;

import lombok.Data;

/**
 * @author fdse
 */
@Data
public class GetOrderByIdInfo {
    private String orderId;

    public GetOrderByIdInfo() {
        // Default constructor
    }

    public GetOrderByIdInfo(String orderId) {
        this.orderId = orderId;
    }
}