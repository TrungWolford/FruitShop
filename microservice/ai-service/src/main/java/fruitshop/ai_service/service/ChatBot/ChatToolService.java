package fruitshop.ai_service.service.ChatBot;

import org.springframework.stereotype.Service;

@Service
public class ChatToolService {

    private String notImplemented(String toolName) {
        return "{\"error\":\"Tool '" + toolName + "' is not wired in ai-service yet\"}";
    }

    public String searchProducts(String keyword, int limit) { return notImplemented("searchProducts"); }
    public String getProductDetail(String productId) { return notImplemented("getProductDetail"); }
    public String suggestProducts(long maxPrice, int limit) { return notImplemented("suggestProducts"); }
    public String getOrdersByAccount(String accountId) { return notImplemented("getOrdersByAccount"); }
    public String getOrderDetail(String orderId) { return notImplemented("getOrderDetail"); }
    public String getUserShippingAddresses(String accountId) { return notImplemented("getUserShippingAddresses"); }
    public String createShippingAddress(String accountId, String receiverName, String receiverPhone, String receiverAddress, String city) {
        return notImplemented("createShippingAddress");
    }
    public String createOrderFromChat(String accountId, String itemsJson, String shippingId, int paymentMethod) {
        return notImplemented("createOrderFromChat");
    }
    public String addToCart(String accountId, String productId, int quantity) { return notImplemented("addToCart"); }
    public String getCartItems(String accountId) { return notImplemented("getCartItems"); }
    public String removeFromCart(String cartItemId) { return notImplemented("removeFromCart"); }
    public String createMomoPayment(String orderId) { return notImplemented("createMomoPayment"); }
    public String getRefundsByOrder(String orderId) { return notImplemented("getRefundsByOrder"); }
    public String createRefundFromChat(String accountId, String orderId, String reason, long refundAmount) {
        return notImplemented("createRefundFromChat");
    }
}
