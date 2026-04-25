package fruitshop.payment_service.feign.fallback;

import fruitshop.payment_service.feign.OrderClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderClientFallbackFactory implements FallbackFactory<OrderClient> {
    @Override
    public OrderClient create(Throwable cause) {
        return orderId -> null;
    }
}
