package fruitshop.review_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import fruitshop.review_service.feign.dto.AccountSummaryDto;
import fruitshop.review_service.feign.fallback.AccountClientFallbackFactory;

@FeignClient(
        name = "account-service",
        path = "/api/account",
        fallbackFactory = AccountClientFallbackFactory.class
)
public interface AccountClient {
    @GetMapping("/{accountId}")
    AccountSummaryDto getAccountById(@PathVariable("accountId") String accountId);
}
