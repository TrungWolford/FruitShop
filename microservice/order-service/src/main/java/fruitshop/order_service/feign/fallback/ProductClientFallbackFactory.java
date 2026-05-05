package fruitshop.order_service.feign.fallback;

import fruitshop.order_service.feign.ProductClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {
    @Override
    public ProductClient create(Throwable cause) {
        log.error("ProductClient fallback triggered. Cause: ", cause);
        return productId -> null;
    }
}
