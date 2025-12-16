package server.FruitShop.controller;
// z
import jakarta.validation.Valid;
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
import server.FruitShop.exception.DuplicateResourceException;
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
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        try {
            AccountResponse createdAccount = accountService.createAccount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
        } catch (DuplicateResourceException e) {
            // Số điện thoại đã tồn tại → HTTP 409 Conflict
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Validation 1: Kiểm tra trường bắt buộc
            if (request.getAccountPhone() == null || request.getAccountPhone().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Số điện thoại không được để trống");
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body("Mật khẩu không được để trống");
            }

            // Validation 2: Kiểm tra định dạng số điện thoại (10-11 số)
            String phoneRegex = "^[0-9]{10,11}$";
            if (!request.getAccountPhone().matches(phoneRegex)) {
                return ResponseEntity.badRequest().body("Số điện thoại không đúng định dạng (10-11 số)");
            }

            // Validation 3: Kiểm tra độ dài mật khẩu (tối thiểu 6 ký tự)
            if (request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body("Mật khẩu phải có ít nhất 6 ký tự");
            }

            AccountResponse account = accountService.authenticateAccount(request.getAccountPhone(), request.getPassword());
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            // Kiểm tra nếu là tài khoản bị vô hiệu hóa
            if (e.getMessage() != null && e.getMessage().contains("deactivated")) {
                return ResponseEntity.status(403).body("Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.");
            }
            return ResponseEntity.status(401).body("Tài khoản hoặc mật khẩu không đúng");
        }
    }
}