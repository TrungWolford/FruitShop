package server.FruitShop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "shippings")
@Data
public class Shipping {
    @Id
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

}
