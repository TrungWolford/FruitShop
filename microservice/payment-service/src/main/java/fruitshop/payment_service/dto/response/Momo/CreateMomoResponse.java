package fruitshop.payment_service.dto.response.Momo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMomoResponse {
    @JsonProperty("partnerCode")
    private String partnerCode;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("amount")
    private Long amount;
    @JsonProperty("responseTime")
    private Long responseTime;
    @JsonProperty("message")
    private String message;
    @JsonProperty("resultCode")
    private Integer resultCode;
    @JsonProperty("payUrl")
    private String payUrl;
    @JsonProperty("deeplink")
    private String deeplink;
    @JsonProperty("qrCodeUrl")
    private String qrCodeUrl;
    @JsonProperty("deeplinkMiniApp")
    private String deeplinkMiniApp;
    @JsonProperty("signature")
    private String signature;

    public boolean isSuccess() {
        return resultCode != null && resultCode == 0;
    }

    public String getErrorMessage() {
        if (isSuccess()) return null;
        return message != null ? message : "Unknown error occurred";
    }

    public String getPreferredPaymentUrl() {
        if (payUrl != null && !payUrl.isEmpty()) return payUrl;
        if (deeplink != null && !deeplink.isEmpty()) return deeplink;
        if (qrCodeUrl != null && !qrCodeUrl.isEmpty()) return qrCodeUrl;
        return null;
    }
}