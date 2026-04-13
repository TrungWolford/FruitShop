package fruitshop.cart_service.feign.fallback;

import fruitshop.cart_service.feign.ProductClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {
    @Override
    public ProductClient create(Throwable cause) {
        return productId -> null;
    }
}
