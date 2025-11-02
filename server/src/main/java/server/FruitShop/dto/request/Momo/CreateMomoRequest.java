package server.FruitShop.dto.request.Momo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMomoRequest {
    
    @JsonProperty("partnerCode")
    private String partnerCode;
    
    @JsonProperty("requestType")
    private String requestType;
    
    @JsonProperty("ipnUrl")
    private String ipnUrl;
    
    @JsonProperty("redirectUrl")
    private String redirectUrl;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("amount")
    private long amount;
    
    @JsonProperty("orderInfo")
    private String orderInfo;
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("extraData")
    private String extraData;
    
    @JsonProperty("lang")
    private String lang;
    
    @JsonProperty("signature")
    private String signature;
}
