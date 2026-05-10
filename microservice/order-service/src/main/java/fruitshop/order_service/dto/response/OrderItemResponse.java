package fruitshop.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private String orderDetailId;
    private String productId;
    private String productName;
    private int quantity;
    private long unitPrice;
    private long totalPrice;
    private List<String> productImages;
}
