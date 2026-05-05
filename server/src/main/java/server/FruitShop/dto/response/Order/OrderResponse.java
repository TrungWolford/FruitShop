package server.FruitShop.dto.response.Order;

import lombok.Data;
import server.FruitShop.dto.response.Payment.PaymentResponse;
import server.FruitShop.dto.response.Shipping.ShippingResponse;
import server.FruitShop.entity.Order;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderResponse {
    private String orderId;
    private String accountId;
    private String accountName;
    private Date createdAt;
    private int status; // 0: Huy, 1: Dang van chuyen, 2: Da hoan thanh
    private long totalAmount;
    private List<OrderItemResponse> orderItems;
    private int totalItems;
    private ShippingResponse shipping;
    private PaymentResponse payment;

    public static OrderResponse fromEntity(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setCreatedAt(order.getCreatedAt());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());

        if (order.getAccount() != null) {
            response.setAccountId(order.getAccount().getAccountId());
            response.setAccountName(order.getAccount().getAccountName());
        }

        if (order.getShipping() != null) {
            response.setShipping(ShippingResponse.fromEntity(order.getShipping()));
        }

        if (order.getPayment() != null) {
            response.setPayment(PaymentResponse.fromEntity(order.getPayment()));
        }

        if (order.getOrderItems() != null) {
            List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                    .map(OrderItemResponse::fromEntity)
                    .collect(Collectors.toList());
            response.setOrderItems(itemResponses);
            response.setTotalItems(order.getOrderItems().size());
        }

        return response;
    }
}
