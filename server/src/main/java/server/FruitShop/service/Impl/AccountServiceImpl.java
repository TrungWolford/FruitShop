package server.FruitShop.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Account.CreateAccountRequest;
import server.FruitShop.dto.request.Account.UpdateAccountRequest;
import server.FruitShop.dto.response.Account.AccountResponse;
import server.FruitShop.entity.Account;
import server.FruitShop.entity.Role;
import server.FruitShop.repository.AccountRepository;
import server.FruitShop.repository.RoleRepository;
import server.FruitShop.service.AccountService;
import server.FruitShop.exception.ResourceNotFoundException;
import server.FruitShop.exception.DuplicateResourceException;

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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

        account.setAccountName(request.getAccountName());
        account.setAccountPhone(request.getAccountPhone());
        if(request.getPassword() != null){
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        } else{
            account.setPassword(account.getPassword());
        }

        account.setStatus(request.getStatus());

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
    public AccountResponse authenticateAccount(String accountPhone, String password) {
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
        
        return AccountResponse.fromEntity(account);
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