package server.FruitShop.dto.response.Cart;

import lombok.Data;
import server.FruitShop.entity.Account;

@Data
public class CartAccountResponse {
    private String accountId;
    private String accountName;
    private String accountPhone;
    private int status;
    
    public static CartAccountResponse fromEntity(Account account) {
        if (account == null) {
            return null;
        }
        
        CartAccountResponse response = new CartAccountResponse();
        response.setAccountId(account.getAccountId());
        response.setAccountName(account.getAccountName());
        response.setAccountPhone(account.getAccountPhone());
        response.setStatus(account.getStatus());
        
        return response;
    }
}
