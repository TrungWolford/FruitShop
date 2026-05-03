package fruitshop.payment_service.feign.dto;

import lombok.Data;

@Data
public class OrderSummaryDto {
    private String orderId;
    private String accountId;
    private int status;
    private long totalAmount;
}
