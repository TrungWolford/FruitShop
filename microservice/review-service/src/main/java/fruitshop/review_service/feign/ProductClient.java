package fruitshop.review_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import fruitshop.review_service.feign.dto.ProductSummaryDto;
import fruitshop.review_service.feign.fallback.ProductClientFallbackFactory;

@FeignClient(
        name = "catalog-service",
        path = "/api/catalog/products",
        fallbackFactory = ProductClientFallbackFactory.class
)
public interface ProductClient {
    @GetMapping("/{productId}")
    ProductSummaryDto getProductById(@PathVariable("productId") String productId);
}
