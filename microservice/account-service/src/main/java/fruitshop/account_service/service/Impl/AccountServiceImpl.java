package fruitshop.account_service.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fruitshop.account_service.dto.request.Account.CreateAccountRequest;
import fruitshop.account_service.dto.request.Account.RefreshTokenRequest;
import fruitshop.account_service.dto.request.Account.UpdateAccountRequest;
import fruitshop.account_service.dto.response.Account.AccountResponse;
import fruitshop.account_service.dto.response.Account.LoginResponse;
import fruitshop.account_service.dto.response.Account.RefreshTokenResponse;
import fruitshop.account_service.entity.Account;
import fruitshop.account_service.entity.Role;
import fruitshop.account_service.repository.AccountRepository;
import fruitshop.account_service.repository.RoleRepository;
import fruitshop.account_service.service.AccountService;
import fruitshop.account_service.exception.ResourceNotFoundException;
import fruitshop.account_service.exception.DuplicateResourceException;
import fruitshop.account_service.security.JwtService;
import fruitshop.account_service.event.AccountDeactivatedEvent;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        // Get all accounts with roles loaded
        List<Account> allAccounts = accountRepository.findAllWithRoles();

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allAccounts.size());

        List<Account> pageContent = allAccounts.subList(start, end);
        List<AccountResponse> responses = pageContent.stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, allAccounts.size());
    }

    @Override
    public AccountResponse getAccountById(String accountId) {
        Account account = accountRepository.findByIdWithRoles(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        return AccountResponse.fromEntity(account);
    }

    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        // Kiểm tra phone đã tồn tại chưa
        if (accountRepository.findByAccountPhone(request.getAccountPhone()).isPresent()) {
            throw new DuplicateResourceException("Phone number already exists");
        }
        
        Account account = new Account();
        account.setAccountName(request.getAccountName());
        account.setAccountPhone(request.getAccountPhone());
        // Hash password trước khi lưu
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setStatus(1);

        // Set roles if provided
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
                roles.add(role);
            }
            account.setRoles(roles);
        }

        Account savedAccount = accountRepository.save(account);
        return AccountResponse.fromEntity(savedAccount);
    }

    @Override
    public AccountResponse updateAccount(String accountId, UpdateAccountRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        // Partial update: chỉ cập nhật các trường được gửi lên (không null/rỗng)
        if (request.getAccountName() != null && !request.getAccountName().trim().isEmpty()) {
            account.setAccountName(request.getAccountName());
        }
        
        if (request.getAccountPhone() != null && !request.getAccountPhone().trim().isEmpty()) {
            account.setAccountPhone(request.getAccountPhone());
        }
        
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Status: luôn cập nhật (bắt buộc)
        int oldStatus = account.getStatus();
        account.setStatus(request.getStatus());
        
        if (oldStatus == 1 && request.getStatus() == 0) {
            streamBridge.send("accountDeactivatedSupplier-out-0", new AccountDeactivatedEvent(account.getAccountId(), new Date()));
        }

        // Update roles if provided
        if (request.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
                roles.add(role);
            }
            account.setRoles(roles);
        }

        Account updatedAccount = accountRepository.save(account);
        return AccountResponse.fromEntity(updatedAccount);
    }

    @Override
    public void deleteAccount(String accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }
        accountRepository.deleteById(accountId);
    }

    @Override
    public Page<AccountResponse> getAccountsByStatus(int status, Pageable pageable) {
        // Get accounts by status with roles loaded
        List<Account> accountsByStatus = accountRepository.findByStatusWithRoles(status);

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), accountsByStatus.size());

        List<Account> pageContent = accountsByStatus.subList(start, end);
        List<AccountResponse> responses = pageContent.stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, accountsByStatus.size());
    }

    @Override
    public AccountResponse getAccountByPhone(String accountPhone) {
        Account account = accountRepository.findByAccountPhone(accountPhone)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with phone: " + accountPhone));
        return AccountResponse.fromEntity(account);
    }

    @Override
    public LoginResponse authenticateAccount(String accountPhone, String password) {
        // Tìm account theo phone
        Account account = accountRepository.findByAccountPhone(accountPhone)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid phone or password"));
        
        // Kiểm tra password hash
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new ResourceNotFoundException("Invalid phone or password");
        }
        
        // Kiểm tra trạng thái active
        if (account.getStatus() != 1) {
            throw new ResourceNotFoundException("Account is deactivated");
        }

        List<String> roleNames = account.getRoles().stream()
                .map(Role::getRoleName)
                .toList();

        String accessToken = jwtService.generateAccessToken(account.getAccountId(), roleNames);
        String refreshToken = jwtService.generateRefreshToken(account.getAccountId());

        return LoginResponse.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .account(AccountResponse.fromEntity(account))
                .build();
    }

    @Override
    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new ResourceNotFoundException("Refresh token is required");
        }

        String refreshToken = request.getRefreshToken().trim();

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new ResourceNotFoundException("Invalid refresh token");
        }

        String accountId = jwtService.extractSubject(refreshToken);
        Account account = accountRepository.findByIdWithRoles(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getStatus() != 1) {
            throw new ResourceNotFoundException("Account is deactivated");
        }

        List<String> roleNames = account.getRoles().stream()
                .map(Role::getRoleName)
                .toList();

        String newAccessToken = jwtService.generateAccessToken(account.getAccountId(), roleNames);

        return RefreshTokenResponse.builder()
                .tokenType("Bearer")
                .accessToken(newAccessToken)
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .build();
    }

    @Override
    public Page<AccountResponse> searchAccountsByName(String accountName, Pageable pageable) {
        // Get accounts by name with roles loaded
        List<Account> accountsByName = accountRepository.findByAccountNameContainingIgnoreCaseWithRoles(accountName);

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), accountsByName.size());

        List<Account> pageContent = accountsByName.subList(start, end);
        List<AccountResponse> responses = pageContent.stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, accountsByName.size());
    }
}