package fruitshop.account_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import fruitshop.account_service.dto.request.Account.CreateAccountRequest;
import fruitshop.account_service.dto.request.Account.UpdateAccountRequest;
import fruitshop.account_service.dto.response.Account.AccountResponse;

import java.util.List;

public interface AccountService {
    Page<AccountResponse> getAllAccounts(Pageable pageable);
    AccountResponse getAccountById(String accountId);
    AccountResponse createAccount(CreateAccountRequest request);
    AccountResponse updateAccount(String accountId, UpdateAccountRequest request);
    void deleteAccount(String accountId);
    Page<AccountResponse> getAccountsByStatus(int status, Pageable pageable);
    AccountResponse getAccountByPhone(String accountPhone);
    AccountResponse authenticateAccount(String accountPhone, String password);
    Page<AccountResponse> searchAccountsByName(String accountName, Pageable pageable);
}
