package server.FruitShop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import server.FruitShop.dto.request.Momo.CreateMomoRequest;
import server.FruitShop.dto.response.Momo.CreateMomoResponse;
import server.FruitShop.momo.MomoApi;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MomoService {

    private final MomoApi momoApi;

    @Value("${momo.partner-code}")
    private String PARTNER_CODE;

    @Value("${momo.access-key}")
    private String ACCESS_KEY;

    @Value("${momo.secret-key}")
    private String SECRET_KEY;

    @Value("${momo.return-url}")
    private String RETURN_URL;

    @Value("${momo.ipn-url}")
    private String IPN_URL;

    @Value("${momo.request-type}")
    private String REQUEST_TYPE;

    /**
     * Create MoMo QR payment
     * @param orderId Order ID from your system
     * @param amount Payment amount in VND
     * @param orderInfo Order description
     * @return CreateMomoResponse containing payment URL and QR code
     */
    public CreateMomoResponse createQR(String orderId, long amount, String orderInfo) {
        try {
            log.info("🔵 Creating MoMo QR payment for orderId: {}, amount: {}", orderId, amount);

            // Generate unique request ID
            String requestId = UUID.randomUUID().toString();
            
            // Extra data (optional)
            String extraData = "";
            
            // Create raw signature string according to MoMo documentation
            String rawSignature = "accessKey=" + ACCESS_KEY +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + IPN_URL +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + PARTNER_CODE +
                    "&redirectUrl=" + RETURN_URL +
                    "&requestId=" + requestId +
                    "&requestType=" + REQUEST_TYPE;

            log.debug("Raw signature string: {}", rawSignature);

            // Generate HMAC SHA256 signature
            String signature = generateHMACSHA256(rawSignature, SECRET_KEY);
            log.debug("Generated signature: {}", signature);

            // Build MoMo request
            CreateMomoRequest request = CreateMomoRequest.builder()
                    .partnerCode(PARTNER_CODE)
                    .requestId(requestId)
                    .amount(amount)
                    .orderId(orderId)
                    .orderInfo(orderInfo)
                    .redirectUrl(RETURN_URL)
                    .ipnUrl(IPN_URL)
                    .requestType(REQUEST_TYPE)
                    .extraData(extraData)
                    .lang("vi")
                    .signature(signature)
                    .build();

            log.info("📤 Sending request to MoMo API...");

            // Call MoMo API
            CreateMomoResponse response = momoApi.createMomoQR(request);

            log.info("📥 MoMo response - resultCode: {}, message: {}", 
                    response.getResultCode(), response.getMessage());
            log.info("📥 MoMo response details:");
            log.info("  - payUrl: {}", response.getPayUrl());
            log.info("  - deeplink: {}", response.getDeeplink());
            log.info("  - qrCodeUrl: {}", response.getQrCodeUrl());
            log.info("  - deeplinkMiniApp: {}", response.getDeeplinkMiniApp());

            if (response.isSuccess()) {
                log.info("✅ MoMo QR created successfully");
                if (response.getDeeplink() != null) {
                    log.info("✅ Deeplink available: {}", response.getDeeplink());
                } else {
                    log.warn("⚠️ Deeplink is NULL - QR code may not work with MoMo app");
                }
            } else {
                log.error("❌ MoMo QR creation failed: {}", response.getErrorMessage());
            }

            return response;

        } catch (Exception e) {
            log.error("💥 Error creating MoMo QR payment", e);
            return CreateMomoResponse.builder()
                    .resultCode(-1)
                    .message("Internal error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Verify MoMo callback signature
     * @param rawSignature Raw signature string from MoMo callback
     * @param signature Signature from MoMo callback
     * @return true if signature is valid
     */
    public boolean verifySignature(String rawSignature, String signature) {
        try {
            String expectedSignature = generateHMACSHA256(rawSignature, SECRET_KEY);
            boolean isValid = expectedSignature.equals(signature);
            log.info("Signature verification: {}", isValid ? "✅ VALID" : "❌ INVALID");
            return isValid;
        } catch (Exception e) {
            log.error("Error verifying MoMo signature", e);
            return false;
        }
    }

    /**
     * Generate HMAC SHA256 signature
     * @param data Data to sign
     * @param secretKey Secret key
     * @return Hex string of signature
     */
    private String generateHMACSHA256(String data, String secretKey) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8), 
                "HmacSHA256"
        );
        sha256HMAC.init(secretKeySpec);
        byte[] hash = sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        // Convert to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
