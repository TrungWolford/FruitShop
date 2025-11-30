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
        // Xóa dữ liệu cũ
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

    @Test
    @DisplayName("Integration Test 2: Lấy payment theo ID - Thành công")
    void testGetPaymentById_Success() throws Exception {
        mockMvc.perform(get("/api/payment/{id}", testPayment.getPaymentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("COD"))
                .andExpect(jsonPath("$.amount").value(100000))
                .andExpect(jsonPath("$.paymentStatus").value(0));
    }

    @Test
    @DisplayName("Integration Test 3: Lấy payment theo ID - Không tồn tại")
    void testGetPaymentById_NotFound() throws Exception {
        mockMvc.perform(get("/api/payment/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

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

    @Test
    @DisplayName("Integration Test 8: Lấy payment theo transactionId - Thành công")
    void testGetPaymentByTransactionId_Success() throws Exception {
        mockMvc.perform(get("/api/payment/transaction/{transactionId}", "TXN123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN123456"))
                .andExpect(jsonPath("$.paymentMethod").value("COD"));
    }

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

    @Test
    @DisplayName("Integration Test 11: Cập nhật payment status - Status không hợp lệ")
    void testUpdatePaymentStatus_InvalidStatus() throws Exception {
        mockMvc.perform(put("/api/payment/{id}/status", testPayment.getPaymentId())
                        .param("status", "5")) // Invalid status > 3
                .andExpect(status().isBadRequest());
    }
}
