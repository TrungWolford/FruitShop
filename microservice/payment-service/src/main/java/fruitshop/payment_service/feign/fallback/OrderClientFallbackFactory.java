package fruitshop.payment_service.feign.fallback;

import fruitshop.payment_service.feign.OrderClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderClientFallbackFactory implements FallbackFactory<OrderClient> {
    @Override
    public OrderClient create(Throwable cause) {
        log.error("Feign call to order-service failed: {}", cause.getMessage());
        return orderId -> null;
    }
}
