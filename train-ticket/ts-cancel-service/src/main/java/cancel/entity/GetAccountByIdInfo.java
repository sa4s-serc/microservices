package cancel.entity;

import lombok.Data;

/**
 * @author fdse
 */
@Data
public class GetAccountByIdInfo {
    private String accountId;

    public GetAccountByIdInfo() {
        //Default Constructor
    }

    public GetAccountByIdInfo(String accountId) {
        this.accountId = accountId;
    }
}