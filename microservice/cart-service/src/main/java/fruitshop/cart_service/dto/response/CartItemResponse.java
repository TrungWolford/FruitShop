package fruitshop.cart_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import fruitshop.cart_service.feign.dto.ProductSummaryDto;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private String cartItemId;
    private String productId;
    private String productName;
    private long productPrice;
    private int quantity;
    private long totalPrice;
    private List<ProductSummaryDto.ProductImageDto> images;
}
