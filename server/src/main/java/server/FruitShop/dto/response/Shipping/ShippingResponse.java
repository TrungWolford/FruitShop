package server.FruitShop.dto.response.Shipping;

import lombok.Data;
import server.FruitShop.entity.Shipping;
import java.util.Date;

@Data
public class ShippingResponse {
    private String shippingId;
    private String startLocation;
    private String endLocation;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String city;
    private long shippingFee;
    private Date shippedAt;
    private int status;

    public static ShippingResponse fromEntity(Shipping shipping) {
        ShippingResponse response = new ShippingResponse();
        response.setShippingId(shipping.getShippingId());
        response.setStartLocation(shipping.getStartLocation());
        response.setEndLocation(shipping.getEndLocation());
        response.setReceiverName(shipping.getReceiverName());
        response.setReceiverPhone(shipping.getReceiverPhone());
        response.setReceiverAddress(shipping.getReceiverAddress());
        response.setCity(shipping.getCity());
        response.setShippingFee(shipping.getShippingFee());
        response.setShippedAt(shipping.getShippedAt());
        response.setStatus(shipping.getStatus());
        return response;
    }
}
