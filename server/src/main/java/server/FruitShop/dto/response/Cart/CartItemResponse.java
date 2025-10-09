package server.FruitShop.dto.response.Cart;
import lombok.Data;
import server.FruitShop.entity.CartItem;
import server.FruitShop.entity.Product;
import server.FruitShop.entity.ProductImage;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class CartItemResponse {
    private String cartItemId;
    private String productId;
    private String productName;
    private long productPrice;
    private int quantity;
    private long totalPrice;
    private List<String> images; // List of image URLs

    public static CartItemResponse fromEntity(CartItem cartItem){
        CartItemResponse response = new CartItemResponse();
        response.setCartItemId(cartItem.getCartItemId());
        response.setQuantity(cartItem.getQuantity());

        Product product = cartItem.getProduct();
        if (product != null) {
            response.setProductId(product.getProductId());
            response.setProductName(product.getProductName());
            response.setProductPrice(product.getPrice());
            response.setTotalPrice(product.getPrice() * cartItem.getQuantity());

            // Get product images
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                List<String> imageUrls = product.getImages().stream()
                        .sorted((img1, img2) -> Integer.compare(img1.getImageOrder(), img2.getImageOrder()))
                        .map(ProductImage::getImageUrl)
                        .collect(Collectors.toList());
                response.setImages(imageUrls);
            }
        }

        return response;
    }
}
