package server.FruitShop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Payment.PaymentRequest;
import server.FruitShop.entity.Payment;
import server.FruitShop.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Payment API
 * Payment Status: 0=Pending, 1=Completed, 2=Failed, 3=Refunded
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        // @Transactional sẽ tự động rollback sau mỗi test
        
        // Tạo payment test
        testPayment = new Payment();
        testPayment.setPaymentMethod("COD");
        testPayment.setPaymentStatus(0); // Pending
        testPayment.setPaymentDate(new Date());
        testPayment.setAmount(BigDecimal.valueOf(100000));
        testPayment.setTransactionId("TXN123456");
        testPayment = paymentRepository.save(testPayment);
    }

    /**
     * Test 1: Lấy tất cả payments với phân trang
     * Mục đích: Kiểm tra API GET /api/payment trả về danh sách payments
     * Input: page=0, size=10
     * Kết quả mong muốn: 200 OK, response chứa list payments với paymentMethod, amount, status
     */
    @Test
    @DisplayName("Integration Test 1: Lấy tất cả payments - Thành công")
    void testGetAllPayments_Success() throws Exception {
        mockMvc.perform(get("/api/payment")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].paymentMethod").value("COD"));
    }

    /**
     * Test 2: Lấy payment theo ID
     * Mục đích: Kiểm tra API GET /api/payment/{id} trả về thông tin chi tiết payment
     * Input: paymentId hợp lệ
     * Kết quả mong muốn: 200 OK, response chứa paymentMethod=COD, amount=100000, status=0
     */
    @Test
    @DisplayName("Integration Test 2: Lấy payment theo ID - Thành công")
    void testGetPaymentById_Success() throws Exception {
        mockMvc.perform(get("/api/payment/{id}", testPayment.getPaymentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("COD"))
                .andExpect(jsonPath("$.amount").value(100000))
                .andExpect(jsonPath("$.paymentStatus").value(0));
    }

    /**
     * Test 3: Lấy payment với ID không tồn tại
     * Mục đích: Kiểm tra xử lý lỗi khi payment không tồn tại
     * Input: paymentId không hợp lệ ("invalid-id")
     * Kết quả mong muốn: 404 Not Found
     */
    @Test
    @DisplayName("Integration Test 3: Lấy payment theo ID - Không tồn tại")
    void testGetPaymentById_NotFound() throws Exception {
        mockMvc.perform(get("/api/payment/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test 4: Tạo payment mới
     * Mục đích: Kiểm tra API POST /api/payment tạo payment mới vào database
     * Input: PaymentRequest (method=BANK_TRANSFER, amount=200000, status=1, transactionId=TXN789012)
     * Kết quả mong muốn: 201 Created, payment được insert vào DB, total count = 2
     */
    @Test
    @DisplayName("Integration Test 4: Tạo payment mới - Thành công")
    void testCreatePayment_Success() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod("BANK_TRANSFER");
        request.setPaymentStatus(1); // Completed
        request.setPaymentDate(new Date());
        request.setAmount(BigDecimal.valueOf(200000));
        request.setTransactionId("TXN789012");

        mockMvc.perform(post("/api/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod").value("BANK_TRANSFER"))
                .andExpect(jsonPath("$.amount").value(200000));

        // Verify trong database
        long count = paymentRepository.count();
        assert count == 2;
    }

    /**
     * Test 5: Cập nhật thông tin payment
     * Mục đích: Kiểm tra API PUT /api/payment/{id} cập nhật payment trong database
     * Input: PaymentRequest (method=MOMO, amount=150000, status=1)
     * Kết quả mong muốn: 200 OK, payment được update với paymentMethod=MOMO, amount=150000
     */
    @Test
    @DisplayName("Integration Test 5: Cập nhật payment - Thành công")
    void testUpdatePayment_Success() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod("MOMO");
        request.setPaymentStatus(1);
        request.setPaymentDate(new Date());
        request.setAmount(BigDecimal.valueOf(150000));
        request.setTransactionId("TXN123456");

        mockMvc.perform(put("/api/payment/{id}", testPayment.getPaymentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("MOMO"))
                .andExpect(jsonPath("$.amount").value(150000));

        // Verify trong database
        Payment updated = paymentRepository.findById(testPayment.getPaymentId()).orElseThrow();
        assert updated.getPaymentMethod().equals("MOMO");
    }

    /**
     * Test 6: Lấy danh sách payments theo status
     * Mục đích: Kiểm tra API GET /api/payment/status/{status} lọc payments theo trạng thái
     * Input: status=0 (Pending), page=0, size=10
     * Kết quả mong muốn: 200 OK, chỉ trả về payments có paymentStatus=0
     */
    @Test
    @DisplayName("Integration Test 6: Lấy payments theo status - Thành công")
    void testGetPaymentsByStatus_Success() throws Exception {
        mockMvc.perform(get("/api/payment/status/{status}", 0)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].paymentStatus").value(0));
    }

    /**
     * Test 7: Cập nhật payment status
     * Mục đích: Kiểm tra API PUT /api/payment/{id}/status cập nhật trạng thái thanh toán
     * Input: status=1 (Completed)
     * Kết quả mong muốn: 200 OK, paymentStatus được update từ 0 → 1 trong DB
     */
    @Test
    @DisplayName("Integration Test 7: Cập nhật payment status - Thành công")
    void testUpdatePaymentStatus_Success() throws Exception {
        mockMvc.perform(put("/api/payment/{id}/status", testPayment.getPaymentId())
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value(1));

        // Verify trong database
        Payment updated = paymentRepository.findById(testPayment.getPaymentId()).orElseThrow();
        assert updated.getPaymentStatus() == 1;
    }

    /**
     * Test 8: Lấy payment theo transaction ID
     * Mục đích: Kiểm tra API GET /api/payment/transaction/{transactionId} tìm payment bằng mã giao dịch
     * Input: transactionId="TXN123456"
     * Kết quả mong muốn: 200 OK, response chứa payment với transactionId=TXN123456, method=COD
     */
    @Test
    @DisplayName("Integration Test 8: Lấy payment theo transactionId - Thành công")
    void testGetPaymentByTransactionId_Success() throws Exception {
        mockMvc.perform(get("/api/payment/transaction/{transactionId}", "TXN123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN123456"))
                .andExpect(jsonPath("$.paymentMethod").value("COD"));
    }

    /**
     * Test 9: Tạo payment thiếu paymentMethod
     * Mục đích: Kiểm tra validation khi thiếu trường bắt buộc
     * Input: PaymentRequest thiếu paymentMethod (chỉ có status và amount)
     * Kết quả mong muốn: 400 Bad Request, payment không được tạo
     */
    @Test
    @DisplayName("Integration Test 9: Tạo payment - Thiếu paymentMethod")
    void testCreatePayment_MissingPaymentMethod() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentStatus(0);
        request.setAmount(BigDecimal.valueOf(100000));

        mockMvc.perform(post("/api/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 10: Tạo payment với amount không hợp lệ
     * Mục đích: Kiểm tra validation amount phải là số dương
     * Input: PaymentRequest với amount=-1000 (âm)
     * Kết quả mong muốn: 400 Bad Request, payment không được tạo
     */
    @Test
    @DisplayName("Integration Test 10: Tạo payment - Amount không hợp lệ")
    void testCreatePayment_InvalidAmount() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod("COD");
        request.setPaymentStatus(0);
        request.setAmount(BigDecimal.valueOf(-1000)); // Negative amount

        mockMvc.perform(post("/api/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 11: Cập nhật payment status với giá trị không hợp lệ
     * Mục đích: Kiểm tra validation status phải trong khoảng 0-3
     * Input: status=5 (ngoài range 0-3)
     * Kết quả mong muốn: 400 Bad Request, status không được update
     */
    @Test
    @DisplayName("Integration Test 11: Cập nhật payment status - Status không hợp lệ")
    void testUpdatePaymentStatus_InvalidStatus() throws Exception {
        mockMvc.perform(put("/api/payment/{id}/status", testPayment.getPaymentId())
                        .param("status", "5")) // Invalid status > 3
                .andExpect(status().isBadRequest());
    }
}
