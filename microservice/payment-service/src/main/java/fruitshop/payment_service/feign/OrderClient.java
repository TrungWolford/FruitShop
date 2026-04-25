package fruitshop.payment_service.feign;

import fruitshop.payment_service.feign.dto.OrderSummaryDto;
import fruitshop.payment_service.feign.fallback.OrderClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "order-service",
        path = "/api/orders",
        fallbackFactory = OrderClientFallbackFactory.class
)
public interface OrderClient {
    @GetMapping("/{orderId}")
    OrderSummaryDto getById(@PathVariable("orderId") String orderId);
}
