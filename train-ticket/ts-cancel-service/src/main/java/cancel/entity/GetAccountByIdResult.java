package cancel.entity;

import edu.fudan.common.entity.User;
import lombok.Data;

/**
 * @author fdse
 */
@Data
public class GetAccountByIdResult {
    private User user;

    public GetAccountByIdResult() {
        //Default Constructor
    }

    public GetAccountByIdResult(User user) {
        this.user = user;
    }
}