package fruitshop.ai_service.orchestrator.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/api/catalog/products/search")
    String searchProducts(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );
}
