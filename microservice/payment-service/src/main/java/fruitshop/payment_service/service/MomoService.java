package fruitshop.payment_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import fruitshop.payment_service.dto.request.Momo.CreateMomoRequest;
import fruitshop.payment_service.dto.response.Momo.CreateMomoResponse;
import fruitshop.payment_service.momo.MomoApi;

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

    public CreateMomoResponse createQR(String orderId, long amount, String orderInfo) {
        try {
            String requestId = UUID.randomUUID().toString();
            String extraData = "";

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

            String signature = generateHMACSHA256(rawSignature, SECRET_KEY);

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

            return momoApi.createMomoQR(request);
        } catch (Exception e) {
            return CreateMomoResponse.builder()
                    .resultCode(-1)
                    .message("Internal error: " + e.getMessage())
                    .build();
        }
    }

    public boolean verifySignature(String rawSignature, String signature) {
        try {
            String expectedSignature = generateHMACSHA256(rawSignature, SECRET_KEY);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String generateHMACSHA256(String data, String secretKey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256HMAC.init(secretKeySpec);
        byte[] hash = sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}