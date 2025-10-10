package server.FruitShop.dto.request.Order;

import lombok.Data;

@Data
public class UpdateOrderRequest {
    private int status;
    private String paymentId;
}