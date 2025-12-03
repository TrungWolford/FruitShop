package server.FruitShop.dto.request.Account;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Data
public class CreateAccountRequest {
    @NotBlank(message = "Tên tài khoản không được để trống")
    private String accountName;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không đúng định dạng (10-11 số)")
    private String accountPhone;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
    
    private Set<String> roleIds;
}

