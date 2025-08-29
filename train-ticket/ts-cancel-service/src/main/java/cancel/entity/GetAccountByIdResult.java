package cancel.entity;

import edu.fudan.common.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fdse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAccountByIdResult {
    private boolean success;
    private User user;
}