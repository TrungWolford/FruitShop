package fruitshop.review_service.feign.fallback;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import fruitshop.review_service.feign.AccountClient;

@Component
public class AccountClientFallbackFactory implements FallbackFactory<AccountClient> {
    @Override
    public AccountClient create(Throwable cause) {
        return accountId -> null;
    }
}
