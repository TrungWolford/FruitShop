package fruitshop.order_service.feign.dto;

import lombok.Data;

@Data
public class AccountSummaryDto {
    private String accountId;
    private int status;
}
