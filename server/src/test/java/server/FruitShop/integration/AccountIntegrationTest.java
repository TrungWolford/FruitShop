package server.FruitShop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Account.CreateAccountRequest;
import server.FruitShop.dto.request.Account.LoginRequest;
import server.FruitShop.dto.request.Account.UpdateAccountRequest;
import server.FruitShop.entity.Account;
import server.FruitShop.entity.Role;
import server.FruitShop.repository.AccountRepository;
import server.FruitShop.repository.RoleRepository;

import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Account API
 * Test toàn bộ flow: Controller → Service → Repository → Database
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private BCryptPasswordEncoder passwordEncoder;
    private Account testAccount;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        
        // Xóa dữ liệu cũ
        accountRepository.deleteAll();
        roleRepository.deleteAll();

        // Tạo role Customer
        customerRole = new Role();
        customerRole.setRoleName("CUSTOMER");
        customerRole = roleRepository.save(customerRole);

        // Tạo account test
        testAccount = new Account();
        testAccount.setAccountName("Nguyễn Văn A");
        testAccount.setAccountPhone("0355142890");
        testAccount.setPassword(passwordEncoder.encode("123456"));
        testAccount.setStatus(1);
        testAccount.setRoles(new HashSet<>(Collections.singletonList(customerRole)));
        testAccount = accountRepository.save(testAccount);
    }

    @Test
    @DisplayName("Integration Test 1: Lấy tất cả accounts - Thành công")
    void testGetAllAccounts_Success() throws Exception {
        mockMvc.perform(get("/api/account")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].accountName").value("Nguyễn Văn A"));
    }

    @Test
    @DisplayName("Integration Test 2: Lấy account theo ID - Thành công")
    void testGetAccountById_Success() throws Exception {
        mockMvc.perform(get("/api/account/{id}", testAccount.getAccountId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.accountPhone").value("0355142890"))
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    @DisplayName("Integration Test 3: Lấy account theo ID - Không tồn tại")
    void testGetAccountById_NotFound() throws Exception {
        mockMvc.perform(get("/api/account/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration Test 4: Tạo account mới - Thành công")
    void testCreateAccount_Success() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountName("Trần Thị B");
        request.setAccountPhone("0999999999");
        request.setPassword("password123");
        request.setRoleIds(new HashSet<>(Collections.singletonList(customerRole.getRoleId())));

        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountName").value("Trần Thị B"))
                .andExpect(jsonPath("$.accountPhone").value("0999999999"))
                .andExpect(jsonPath("$.status").value(1));

        // Verify trong database
        long count = accountRepository.count();
        assert count == 2; // 1 account ban đầu + 1 account mới
    }

    @Test
    @DisplayName("Integration Test 5: Cập nhật account - Thành công")
    void testUpdateAccount_Success() throws Exception {
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccountName("Nguyễn Văn A - Updated");
        request.setAccountPhone("0355142890");
        request.setPassword("newpassword");
        request.setStatus(1);
        request.setRoleIds(new HashSet<>(Collections.singletonList(customerRole.getRoleId())));

        mockMvc.perform(put("/api/account/{id}", testAccount.getAccountId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountName").value("Nguyễn Văn A - Updated"));

        // Verify trong database
        Account updated = accountRepository.findById(testAccount.getAccountId()).orElseThrow();
        assert updated.getAccountName().equals("Nguyễn Văn A - Updated");
    }

    @Test
    @DisplayName("Integration Test 6: Xóa account - Thành công")
    void testDeleteAccount_Success() throws Exception {
        mockMvc.perform(delete("/api/account/{id}", testAccount.getAccountId()))
                .andExpect(status().isNoContent());

        // Verify trong database
        boolean exists = accountRepository.existsById(testAccount.getAccountId());
        assert !exists;
    }

    @Test
    @DisplayName("Integration Test 7: Lấy account theo phone - Thành công")
    void testGetAccountByPhone_Success() throws Exception {
        mockMvc.perform(get("/api/account/phone/{phone}", "0355142890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountPhone").value("0355142890"))
                .andExpect(jsonPath("$.accountName").value("Nguyễn Văn A"));
    }

    @Test
    @DisplayName("Integration Test 8: Login - Thành công")
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setAccountPhone("0355142890");
        request.setPassword("123456");

        mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountPhone").value("0355142890"))
                .andExpect(jsonPath("$.accountName").value("Nguyễn Văn A"));
    }

    @Test
    @DisplayName("Integration Test 9: Login - Sai mật khẩu")
    void testLogin_WrongPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setAccountPhone("0355142890");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Integration Test 10: Tìm kiếm account theo tên - Thành công")
    void testSearchAccountsByName_Success() throws Exception {
        // Tạo thêm account
        Account account2 = new Account();
        account2.setAccountName("Lê Văn C");
        account2.setAccountPhone("0888888888");
        account2.setPassword(passwordEncoder.encode("password"));
        account2.setStatus(1);
        account2.setRoles(new HashSet<>(Collections.singletonList(customerRole)));
        accountRepository.save(account2);

        mockMvc.perform(get("/api/account/search")
                        .param("accountName", "Nguyễn")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].accountName", containsString("Nguyễn")));
    }

    @Test
    @DisplayName("Integration Test 11: Lấy accounts theo status - Thành công")
    void testGetAccountsByStatus_Success() throws Exception {
        mockMvc.perform(get("/api/account/status/{status}", 1)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].status").value(1));
    }

    @Test
    @DisplayName("Integration Test 12: Tạo account - Phone đã tồn tại")
    void testCreateAccount_PhoneExists() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountName("Test User");
        request.setAccountPhone("0355142890"); // Phone đã tồn tại
        request.setPassword("password");
        request.setRoleIds(new HashSet<>(Collections.singletonList(customerRole.getRoleId())));

        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError()); // Expect error khi phone duplicate
    }
}
