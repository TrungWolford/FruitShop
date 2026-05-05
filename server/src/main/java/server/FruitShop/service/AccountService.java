package server.FruitShop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.Account.CreateAccountRequest;
import server.FruitShop.dto.request.Account.UpdateAccountRequest;
import server.FruitShop.dto.response.Account.AccountResponse;

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
