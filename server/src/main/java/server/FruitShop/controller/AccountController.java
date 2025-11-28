package server.FruitShop.controller;
// z
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.FruitShop.dto.request.Account.CreateAccountRequest;
import server.FruitShop.dto.request.Account.LoginRequest;
import server.FruitShop.dto.request.Account.UpdateAccountRequest;
import server.FruitShop.dto.response.Account.AccountResponse;
import server.FruitShop.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/api/account")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountResponse> accounts = accountService.getAllAccounts(pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable String accountId) {
        AccountResponse account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(account);
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody CreateAccountRequest request) {
        AccountResponse createdAccount = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable String accountId, @RequestBody UpdateAccountRequest request) {
        AccountResponse updatedAccount = accountService.updateAccount(accountId, request);
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<AccountResponse>> getAccountsByStatus(
            @PathVariable int status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountResponse> accounts = accountService.getAccountsByStatus(status, pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/phone/{accountPhone}")
    public ResponseEntity<AccountResponse> getAccountByPhone(@PathVariable String accountPhone) {
        AccountResponse account = accountService.getAccountByPhone(accountPhone);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AccountResponse>> searchAccountsByName(
            @RequestParam String accountName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountResponse> accounts = accountService.searchAccountsByName(accountName, pageable);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/login")
    public ResponseEntity<AccountResponse> login(@RequestBody LoginRequest request) {
        try {
            AccountResponse account = accountService.authenticateAccount(request.getAccountPhone(), request.getPassword());
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }
}