package server.FruitShop.dto.request.Account;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateAccountRequest {
    private String accountName;
    private String accountPhone;
    private String password;
    private Integer status;  // Đổi từ int sang Integer để có thể null (partial update)
    private Set<String> roleIds;
}