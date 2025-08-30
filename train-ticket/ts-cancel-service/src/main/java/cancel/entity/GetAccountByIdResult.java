package cancel.entity;

import lombok.Data;

/**
 * @author fdse
 */
@Data
public class GetAccountByIdResult {
    private String id;
    private String name;
    private String accountType;
    private String password;
    private int gender;
    private int documentType;
    private String documentNumber;
    private String email;

    public GetAccountByIdResult() {
        // Default constructor
    }
}