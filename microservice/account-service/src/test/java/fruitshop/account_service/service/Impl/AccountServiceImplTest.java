package fruitshop.account_service.service.Impl;

import fruitshop.account_service.dto.request.Account.CreateAccountRequest;
import fruitshop.account_service.entity.Account;
import fruitshop.account_service.entity.Role;
import fruitshop.account_service.exception.DuplicateResourceException;
import fruitshop.account_service.repository.AccountRepository;
import fruitshop.account_service.repository.RoleRepository;
import fruitshop.account_service.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private StreamBridge streamBridge;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void createAccount_duplicatePhone_throwsDuplicateResourceException() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountName("User A");
        request.setAccountPhone("0999999999");
        request.setPassword("secret123");

        Account existing = new Account();
        existing.setAccountId("acc-1");
        when(accountRepository.findByAccountPhone("0999999999")).thenReturn(Optional.of(existing));

        assertThrows(DuplicateResourceException.class, () -> accountService.createAccount(request));
    }

    @Test
    void authenticateAccount_validCredentials_returnsLoginResponse() {
        Role role = new Role();
        role.setRoleId("r-1");
        role.setRoleName("CUSTOMER");

        Account account = new Account();
        account.setAccountId("acc-1");
        account.setAccountPhone("0911111111");
        account.setPassword("encoded-password");
        account.setStatus(1);
        account.setRoles(Set.of(role));

        when(accountRepository.findByAccountPhone("0911111111")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("123456", "encoded-password")).thenReturn(true);
        when(jwtService.generateAccessToken(any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken("acc-1")).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(3600000L);

        var response = accountService.authenticateAccount("0911111111", "123456");

        assertEquals("Bearer", response.getTokenType());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(jwtService).generateAccessToken(any(), any());
    }
}
