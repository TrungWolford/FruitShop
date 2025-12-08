package server.FruitShop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private Account testAccount;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        // Clear database before each test
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
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response trả về có ít nhất 1 account (account test đã tạo trong setUp)
     * - Account đầu tiên có tên "Nguyễn Văn A"
     * - Hỗ trợ phân trang (page=0, size=10)
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
     * Input: ID của testAccount đã tạo trong setUp
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response chứa đầy đủ thông tin: accountName, accountPhone, status
     * - Các giá trị trả về khớp với dữ liệu đã tạo
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
     * Input: ID không tồn tại trong database ("invalid-id")
     * Kết quả mong muốn:
     * - HTTP Status: 404 Not Found
     * - Hệ thống phải xử lý exception và trả về lỗi phù hợp
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
     * Input: CreateAccountRequest với thông tin hợp lệ
     * - accountName: "Trần Thị B"
     * - accountPhone: "0999999999" (số mới chưa tồn tại)
     * - password: "password123"
     * - roleIds: CUSTOMER role
     * Kết quả mong muốn:
     * - HTTP Status: 201 Created
     * - Response trả về thông tin account vừa tạo
     * - Status mặc định = 1 (active)
     * - Database có 2 accounts (1 account ban đầu + 1 account mới)
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
     * Input: UpdateAccountRequest với thông tin mới
     * - accountName: "Nguyễn Văn A - Updated" (đổi tên)
     * - password: "newpassword" (đổi mật khẩu)
     * - Giữ nguyên phone, status, roles
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response trả về thông tin đã được cập nhật
     * - Dữ liệu trong database phải được cập nhật thành công
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
     * Input: ID của testAccount
     * Kết quả mong muốn:
     * - HTTP Status: 204 No Content
     * - Account bị xóa khỏi database (hard delete)
     * - Không còn tồn tại trong database khi query lại
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
     * Test Case 7: Tìm account theo số điện thoại
     * Mục đích: Kiểm tra API GET /api/account/phone/{phone} có tìm được account không
     * Input: Số điện thoại "0355142890"
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Trả về đúng account có số điện thoại đó
     * - Thông tin accountName và accountPhone khớp với dữ liệu test
     * Use case: Dùng để kiểm tra phone đã tồn tại trước khi đăng ký
     */
    @Test
    @DisplayName("Integration Test 7: Lấy account theo phone - Thành công")
    void testGetAccountByPhone_Success() throws Exception {
        mockMvc.perform(get("/api/account/phone/{phone}", "0355142890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountPhone").value("0355142890"))
                .andExpect(jsonPath("$.accountName").value("Nguyễn Văn A"));
    }

    /**
     * Test Case 8: Đăng nhập thành công
     * Mục đích: Kiểm tra API POST /api/account/login với thông tin đúng
     * Input: LoginRequest
     * - accountPhone: "0355142890"
     * - password: "123456" (plain text, sẽ được so sánh với BCrypt hash)
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response trả về thông tin account (không bao gồm password)
     * - Có thể dùng thông tin này để tạo session/token
     * Business logic: Xác thực người dùng trước khi cho phép truy cập hệ thống
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
                .andExpect(jsonPath("$.accountPhone").value("0355142890"))
                .andExpect(jsonPath("$.accountName").value("Nguyễn Văn A"));
    }

    /**
     * Test Case 9: Đăng nhập với mật khẩu sai
     * Mục đích: Kiểm tra xử lý lỗi khi đăng nhập với password không đúng
     * Input: LoginRequest
     * - accountPhone: "0355142890" (đúng)
     * - password: "wrongpassword" (sai)
     * Kết quả mong muốn:
     * - HTTP Status: 401 Unauthorized
     * - Không trả về thông tin account
     * - Bảo mật: Không tiết lộ thông tin chi tiết về lỗi
     * Business logic: Ngăn chặn truy cập trái phép
     */
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

    /**
     * Test Case 10: Tìm kiếm account theo tên
     * Mục đích: Kiểm tra API GET /api/account/search có tìm kiếm đúng không
     * Setup: Tạo thêm 1 account khác để test tìm kiếm
     * - Account 1: "Nguyễn Văn A" (từ setUp)
     * - Account 2: "Lê Văn C" (tạo trong test)
     * Input: Search keyword "Nguyễn"
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Chỉ trả về 1 kết quả (account có tên chứa "Nguyễn")
     * - Account "Lê Văn C" không xuất hiện trong kết quả
     * Use case: Admin tìm kiếm khách hàng theo tên
     */
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

    /**
     * Test Case 11: Lấy danh sách account theo trạng thái
     * Mục đích: Kiểm tra API GET /api/account/status/{status} có lọc đúng không
     * Input: status = 1 (active)
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Trả về danh sách có ít nhất 1 account
     * - Tất cả accounts trong danh sách đều có status = 1
     * Business logic:
     * - Status 1: Active (tài khoản hoạt động)
     * - Status 0: Inactive (tài khoản bị khóa)
     * Use case: Admin xem danh sách tài khoản đang hoạt động hoặc bị khóa
     */
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

    /**
     * Test Case 12: Tạo account với số điện thoại đã tồn tại
     * Mục đích: Kiểm tra validation khi tạo account với phone trùng lặp
     * Input: CreateAccountRequest
     * - accountPhone: "0355142890" (đã tồn tại từ testAccount)
     * - Các thông tin khác hợp lệ
     * Kết quả mong muốn:
     * - HTTP Status: 4xx Client Error (Bad Request hoặc Conflict)
     * - Không tạo account mới trong database
     * Business logic:
     * - Phone number phải unique (không được trùng lặp)
     * - Mỗi số điện thoại chỉ đăng ký 1 tài khoản
     * - Ngăn chặn việc tạo nhiều tài khoản với cùng 1 số điện thoại
     */
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
