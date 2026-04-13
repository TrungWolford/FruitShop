package fruitshop.cart_service.feign;

import fruitshop.cart_service.feign.dto.ProductSummaryDto;
import fruitshop.cart_service.feign.fallback.ProductClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "catalog-service",
        path = "/api/catalog/products",
        fallbackFactory = ProductClientFallbackFactory.class
)
public interface ProductClient {
    @GetMapping("/{productId}")
    ProductSummaryDto getById(@PathVariable("productId") String productId);
}
