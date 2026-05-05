package server.FruitShop.dto.request.Shipping;

import lombok.Data;

import java.util.Date;

@Data
public class ShippingRequest {
    private String accountId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String city;
    private String shipperName;
    private long shippingFee;
    private Date shippedAt;
    private int status;
}
