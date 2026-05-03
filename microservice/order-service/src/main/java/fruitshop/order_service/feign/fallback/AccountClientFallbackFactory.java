package fruitshop.order_service.feign.fallback;

import fruitshop.order_service.feign.AccountClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class AccountClientFallbackFactory implements FallbackFactory<AccountClient> {
    @Override
    public AccountClient create(Throwable cause) {
        return accountId -> null;
    }
}
