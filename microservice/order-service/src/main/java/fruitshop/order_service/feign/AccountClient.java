package fruitshop.order_service.feign;

import fruitshop.order_service.feign.dto.AccountSummaryDto;
import fruitshop.order_service.feign.fallback.AccountClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "account-service",
        path = "/api/account",
        fallbackFactory = AccountClientFallbackFactory.class
)
public interface AccountClient {
    @GetMapping("/{accountId}")
    AccountSummaryDto getById(@PathVariable("accountId") String accountId);
}
