package server.FruitShop.dto.response.Momo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for MoMo payment gateway API
 * Based on MoMo API v2.0 documentation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMomoResponse {
    
    /**
     * Partner code - Mã định danh của merchant
     */
    @JsonProperty("partnerCode")
    private String partnerCode;
    
    /**
     * Order ID - Mã đơn hàng của merchant
     */
    @JsonProperty("orderId")
    private String orderId;
    
    /**
     * Request ID - Mã request, trùng với mã request của merchant
     */
    @JsonProperty("requestId")
    private String requestId;
    
    /**
     * Amount - Số tiền thanh toán
     */
    @JsonProperty("amount")
    private Long amount;
    
    /**
     * Response time - Thời gian phản hồi
     */
    @JsonProperty("responseTime")
    private Long responseTime;
    
    /**
     * Message - Thông báo từ MoMo
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * Result code - Mã kết quả
     * 0: Success
     * Other codes: Error
     */
    @JsonProperty("resultCode")
    private Integer resultCode;
    
    /**
     * Pay URL - Link thanh toán để redirect người dùng
     */
    @JsonProperty("payUrl")
    private String payUrl;
    
    /**
     * Deep link - Link mở ứng dụng MoMo
     */
    @JsonProperty("deeplink")
    private String deeplink;
    
    /**
     * QR Code URL - Link QR code để thanh toán
     */
    @JsonProperty("qrCodeUrl")
    private String qrCodeUrl;
    
    /**
     * Deeplink Mini App - Link mở MoMo mini app
     */
    @JsonProperty("deeplinkMiniApp")
    private String deeplinkMiniApp;
    
    /**
     * Signature - Chữ ký xác thực
     */
    @JsonProperty("signature")
    private String signature;
    
    // Helper methods
    
    /**
     * Check if the payment creation was successful
     * @return true if resultCode is 0 (success)
     */
    public boolean isSuccess() {
        return resultCode != null && resultCode == 0;
    }
    
    /**
     * Get error message if payment creation failed
     * @return error message or null if successful
     */
    public String getErrorMessage() {
        if (isSuccess()) {
            return null;
        }
        return message != null ? message : "Unknown error occurred";
    }
    
    /**
     * Get the appropriate payment URL based on available options
     * Priority: payUrl > deeplink > qrCodeUrl
     * @return payment URL string
     */
    public String getPreferredPaymentUrl() {
        if (payUrl != null && !payUrl.isEmpty()) {
            return payUrl;
        }
        if (deeplink != null && !deeplink.isEmpty()) {
            return deeplink;
        }
        if (qrCodeUrl != null && !qrCodeUrl.isEmpty()) {
            return qrCodeUrl;
        }
        return null;
    }
}
