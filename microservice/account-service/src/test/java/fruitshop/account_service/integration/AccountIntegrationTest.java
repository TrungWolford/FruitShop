package fruitshop.account_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fruitshop.account_service.dto.request.Account.CreateAccountRequest;
import fruitshop.account_service.dto.request.Account.LoginRequest;
import fruitshop.account_service.dto.request.Account.UpdateAccountRequest;
import fruitshop.account_service.entity.Account;
import fruitshop.account_service.entity.Role;
import fruitshop.account_service.repository.AccountRepository;
import fruitshop.account_service.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Account API
 * Test toàn bộ flow: Controller → Service → Repository → Database
 */
@WithMockUser(authorities = "ROLE_ADMIN")
class AccountIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private Account testAccount;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        // Trong môi trường Microservice Testcontainers + @Transactional (WebEnvironment.MOCK),
        // mỗi @Test sẽ tự động rollback dữ liệu. Tuy nhiên, clear trước cũng tốt để đảm bảo.
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

    /**
     * Test Case 1: Lấy danh sách tất cả tài khoản với phân trang
     * Mục đích: Kiểm tra API GET /api/account có trả về đúng danh sách accounts không
     */
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

    /**
     * Test Case 2: Lấy thông tin account theo ID
     * Mục đích: Kiểm tra API GET /api/account/{id} có trả về đúng thông tin account không
     */
    @Test
    @DisplayName("Integration Test 2: Lấy account theo ID - Thành công")
    void testGetAccountById_Success() throws Exception {
        mockMvc.perform(get("/api/account/{id}", testAccount.getAccountId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.accountPhone").value("0355142890"))
                .andExpect(jsonPath("$.status").value(1));
    }

    /**
     * Test Case 3: Lấy account với ID không tồn tại
     * Mục đích: Kiểm tra xử lý lỗi khi tìm account với ID không hợp lệ
     */
    @Test
    @DisplayName("Integration Test 3: Lấy account theo ID - Không tồn tại")
    void testGetAccountById_NotFound() throws Exception {
        mockMvc.perform(get("/api/account/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test Case 4: Tạo tài khoản mới thành công
     * Mục đích: Kiểm tra API POST /api/account có tạo được account mới không
     */
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

    /**
     * Test Case 5: Cập nhật thông tin account
     * Mục đích: Kiểm tra API PUT /api/account/{id} có cập nhật được thông tin không
     */
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

    /**
     * Test Case 6: Xóa account
     * Mục đích: Kiểm tra API DELETE /api/account/{id} có xóa được account không
     */
    @Test
    @DisplayName("Integration Test 6: Xóa account - Thành công")
    void testDeleteAccount_Success() throws Exception {
        mockMvc.perform(delete("/api/account/{id}", testAccount.getAccountId()))
                .andExpect(status().isNoContent());

        // Verify trong database
        boolean exists = accountRepository.existsById(testAccount.getAccountId());
        assert !exists;
    }

    /**
     * Test Case 8: Đăng nhập thành công
     * Mục đích: Kiểm tra API POST /api/account/login với thông tin đúng
     */
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
                .andExpect(jsonPath("$.account.accountPhone").value("0355142890"))
                .andExpect(jsonPath("$.account.accountName").value("Nguyễn Văn A"));
    }
}
