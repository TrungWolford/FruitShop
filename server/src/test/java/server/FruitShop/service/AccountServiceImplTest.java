package server.FruitShop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import server.FruitShop.dto.request.Account.CreateAccountRequest;
import server.FruitShop.dto.response.Account.AccountResponse;
import server.FruitShop.entity.Account;
import server.FruitShop.entity.Role;
import server.FruitShop.repository.AccountRepository;
import server.FruitShop.repository.RoleRepository;
import server.FruitShop.service.Impl.AccountServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho AccountService
 * 
 * Mục đích: Test các chức năng CRUD và business logic của AccountService
 * Không cần database thật, dùng Mockito để giả lập
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test - Account Service")
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;  // Giả lập Repository

    @Mock
    private RoleRepository roleRepository;  // Giả lập Role Repository

    @InjectMocks
    private AccountServiceImpl accountService;  // Service thật, nhưng dùng mock repository

    private Account testAccount;
    private Role customerRole;
    private BCryptPasswordEncoder passwordEncoder;  // Dùng BCrypt thật để test

    /**
     * Chuẩn bị dữ liệu test trước mỗi test case
     */
    @BeforeEach
    void setUp() {
        // Khởi tạo BCryptPasswordEncoder thật
        passwordEncoder = new BCryptPasswordEncoder();
        
        // Tạo role Customer
        customerRole = new Role();
        customerRole.setRoleId("role-customer");
        customerRole.setRoleName("CUSTOMER");

        // Tạo account test với password đã mã hóa BCrypt
        testAccount = new Account();
        testAccount.setAccountId("acc-001");
        testAccount.setAccountName("Nguyễn Văn A");
        testAccount.setAccountPhone("0355142890");
        testAccount.setPassword(passwordEncoder.encode("123456"));  // Mã hóa password thật
        testAccount.setStatus(1);
        testAccount.setRoles(new HashSet<>(Collections.singletonList(customerRole)));
    }

    /**
     * Test 1: Lấy thông tin tài khoản theo ID
     * Mục đích: Kiểm tra getAccountById() trả về đúng thông tin account
     * Input: accountId hợp lệ "acc-001"
     */
    @Test
    @DisplayName("Test 1: Lấy thông tin account theo ID - Thành công")
    void testGetAccountById_Success() {
        // ARRANGE: Chuẩn bị dữ liệu giả
        when(accountRepository.findByIdWithRoles("acc-001"))
                .thenReturn(Optional.of(testAccount));

        // ACT: Gọi phương thức cần test
        AccountResponse result = accountService.getAccountById("acc-001");

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result, "Kết quả không được null");
        assertEquals("acc-001", result.getAccountId());
        assertEquals("Nguyễn Văn A", result.getAccountName());
        assertEquals("0355142890", result.getAccountPhone());
        
        // Kiểm tra repository được gọi đúng 1 lần
        verify(accountRepository, times(1)).findByIdWithRoles("acc-001");
    }

    /**
     * Test 2: Lấy account với ID không tồn tại
     * Mục đích: Kiểm tra getAccountById() throw exception khi không tìm thấy
     * Input: accountId không tồn tại "invalid-id"
     */
    @Test
    @DisplayName("Test 2: Lấy account theo ID - Không tìm thấy (Exception)")
    void testGetAccountById_NotFound() {
        // ARRANGE: Giả lập không tìm thấy account
        when(accountRepository.findByIdWithRoles("invalid-id"))
                .thenReturn(Optional.empty());

        // ACT & ASSERT: Kiểm tra có throw exception không
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.getAccountById("invalid-id");
        });
        
        assertTrue(exception.getMessage().contains("Account not found"));
        verify(accountRepository, times(1)).findByIdWithRoles("invalid-id");
    }

    /**
     * Test 3: Tạo tài khoản mới
     * Mục đích: Kiểm tra createAccount() tạo account mới vào database
     * Input: CreateAccountRequest (name, phone, password, roleIds)
     */
    @Test
    @DisplayName("Test 3: Tạo account mới - Thành công")
    void testCreateAccount_Success() {
        // ARRANGE
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountName("Trần Thị B");
        request.setAccountPhone("0999999999");
        request.setPassword("password123");
        request.setRoleIds(new HashSet<>(Collections.singletonList("role-customer")));

        when(accountRepository.findByAccountPhone("0999999999")).thenReturn(Optional.empty());
        when(roleRepository.findById("role-customer")).thenReturn(Optional.of(customerRole));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // ACT
        AccountResponse result = accountService.createAccount(request);

        // ASSERT
        assertNotNull(result);
        assertEquals("Nguyễn Văn A", result.getAccountName());
        verify(accountRepository, times(1)).findByAccountPhone("0999999999");
        verify(roleRepository, times(1)).findById("role-customer");
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    /**
     * Test 4: Tạo account với role không tồn tại
     * Mục đích: Kiểm tra createAccount() throw exception khi roleId không hợp lệ
     * Input: CreateAccountRequest với roleId không tồn tại
     */
    @Test
    @DisplayName("Test 4: Tạo account - Role không tồn tại (Exception)")
    void testCreateAccount_RoleNotFound() {
        // ARRANGE
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountName("Test User");
        request.setAccountPhone("0999999999");
        request.setPassword("password");
        request.setRoleIds(new HashSet<>(Collections.singletonList("invalid-role")));

        when(accountRepository.findByAccountPhone("0999999999")).thenReturn(Optional.empty());
        when(roleRepository.findById("invalid-role")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.createAccount(request);
        });
        
        assertTrue(exception.getMessage().contains("Role not found"));
        verify(accountRepository, times(1)).findByAccountPhone("0999999999");
        verify(roleRepository, times(1)).findById("invalid-role");
        verify(accountRepository, never()).save(any(Account.class));  // Không được lưu
    }

    /**
     * Test 5: Xóa tài khoản
     * Mục đích: Kiểm tra deleteAccount() xóa account khỏi database
     * Input: accountId hợp lệ "acc-001"
     */
    @Test
    @DisplayName("Test 5: Xóa account - Thành công")
    void testDeleteAccount_Success() {
        // ARRANGE
        when(accountRepository.existsById("acc-001")).thenReturn(true);
        doNothing().when(accountRepository).deleteById("acc-001");

        // ACT
        accountService.deleteAccount("acc-001");

        // ASSERT
        verify(accountRepository, times(1)).existsById("acc-001");
        verify(accountRepository, times(1)).deleteById("acc-001");
    }

    /**
     * Test 6: Xóa account không tồn tại
     * Mục đích: Kiểm tra deleteAccount() throw exception khi accountId không hợp lệ
     * Input: accountId không tồn tại "invalid-id"
     */
    @Test
    @DisplayName("Test 6: Xóa account - Không tìm thấy (Exception)")
    void testDeleteAccount_NotFound() {
        // ARRANGE
        when(accountRepository.existsById("invalid-id")).thenReturn(false);

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.deleteAccount("invalid-id");
        });
        
        assertTrue(exception.getMessage().contains("Account not found"));
        verify(accountRepository, times(1)).existsById("invalid-id");
        verify(accountRepository, never()).deleteById(anyString());  // Không được xóa
    }

    /**
     * Test 7: Lấy tài khoản theo số điện thoại
     * Mục đích: Kiểm tra getAccountByPhone() tìm account bằng phone number
     * Input: phone "0355142890"
     */
    @Test
    @DisplayName("Test 7: Lấy account theo SĐT - Thành công")
    void testGetAccountByPhone_Success() {
        // ARRANGE
        when(accountRepository.findByAccountPhone("0355142890"))
                .thenReturn(Optional.of(testAccount));

        // ACT
        AccountResponse result = accountService.getAccountByPhone("0355142890");

        // ASSERT
        assertNotNull(result);
        assertEquals("0355142890", result.getAccountPhone());
        verify(accountRepository, times(1)).findByAccountPhone("0355142890");
    }

    /**
     * Test 8: Đăng nhập thành công
     * Mục đích: Kiểm tra authenticateAccount() xác thực đúng phone và password
     * Input: phone "0355142890", password "123456"
     */
    @Test
    @DisplayName("Test 8: Login - Thành công")
    void testAuthenticateAccount_Success() {
        // ARRANGE
        when(accountRepository.findByAccountPhone("0355142890"))
                .thenReturn(Optional.of(testAccount));

        // ACT
        AccountResponse result = accountService.authenticateAccount("0355142890", "123456");

        // ASSERT
        assertNotNull(result);
        assertEquals("0355142890", result.getAccountPhone());
        verify(accountRepository, times(1)).findByAccountPhone("0355142890");
    }

    /**
     * Test 9: Đăng nhập với mật khẩu sai
     * Mục đích: Kiểm tra authenticateAccount() throw exception khi password không khớp
     * Input: phone đúng, password sai "wrong-password"
     */
    @Test
    @DisplayName("Test 9: Login - Sai mật khẩu (Exception)")
    void testAuthenticateAccount_WrongPassword() {
        // ARRANGE
        when(accountRepository.findByAccountPhone("0355142890"))
                .thenReturn(Optional.of(testAccount));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.authenticateAccount("0355142890", "wrong-password");
        });
        
        assertTrue(exception.getMessage().contains("Invalid phone or password"));
        verify(accountRepository, times(1)).findByAccountPhone("0355142890");
    }

    /**
     * Test 10: Tạo tài khoản không gán role
     * Mục đích: Kiểm tra createAccount() vẫn tạo được account khi không có roleIds
     * Input: CreateAccountRequest với roleIds = null
     */
    @Test
    @DisplayName("Test 10: Tạo account không có role - Thành công")
    void testCreateAccount_WithoutRoles() {
        // ARRANGE
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountName("User No Role");
        request.setAccountPhone("0888888888");
        request.setPassword("password");
        request.setRoleIds(null);  // Không có role

        when(accountRepository.findByAccountPhone("0888888888")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // ACT
        AccountResponse result = accountService.createAccount(request);

        // ASSERT
        assertNotNull(result);
        verify(accountRepository, times(1)).findByAccountPhone("0888888888");
        verify(roleRepository, never()).findById(anyString());  // Không gọi roleRepository
        verify(accountRepository, times(1)).save(any(Account.class));
    }
}
