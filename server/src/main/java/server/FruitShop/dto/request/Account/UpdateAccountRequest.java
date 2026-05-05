package server.FruitShop.dto.request.Account;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateAccountRequest {
    private String accountName;
    private String accountPhone;
    private String password;
    private int status;  // Bắt buộc - luôn phải có giá trị
    private Set<String> roleIds;
}