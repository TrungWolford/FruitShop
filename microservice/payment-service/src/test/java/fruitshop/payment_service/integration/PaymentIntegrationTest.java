package fruitshop.payment_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fruitshop.payment_service.dto.request.Payment.PaymentRequest;
import fruitshop.payment_service.entity.Payment;
import fruitshop.payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Payment API
 * Payment Status: 0=Pending, 1=Completed, 2=Failed, 3=Refunded
 */
class PaymentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        
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
}
