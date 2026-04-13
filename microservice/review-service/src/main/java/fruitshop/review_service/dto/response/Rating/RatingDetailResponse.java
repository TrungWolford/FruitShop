package fruitshop.review_service.dto.response.Rating;

import lombok.Data;
import fruitshop.review_service.entity.Rating;

import java.time.LocalDateTime;

@Data
public class RatingDetailResponse {
    private String ratingId;
    private AccountInfo account;
    private ProductInfo product;
    private String orderItemId;
    private String comment;
    private int status;
    private double ratingStar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class AccountInfo {
        private String accountId;
        private String accountName;
        private String accountPhone;
    }

    @Data
    public static class ProductInfo {
        private String productId;
        private String productName;
    }

    public static RatingDetailResponse fromEntity(Rating rating){
        RatingDetailResponse response = new RatingDetailResponse();
        response.setRatingId(rating.getRatingId());

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setAccountId(rating.getAccount().getAccountId());
        accountInfo.setAccountName(rating.getAccount().getAccountName());
        accountInfo.setAccountPhone(rating.getAccount().getAccountPhone());
        response.setAccount(accountInfo);

        ProductInfo productInfo = new ProductInfo();
        productInfo.setProductId(rating.getProduct().getProductId());
        productInfo.setProductName(rating.getProduct().getProductName());
        response.setProduct(productInfo);

        response.setOrderItemId(rating.getOrderItem() != null ? rating.getOrderItem().getOrderDetailId() : null);
        response.setComment(rating.getComment());
        response.setStatus(rating.getStatus());
        response.setRatingStar(rating.getRatingStar());
        response.setCreatedAt(rating.getCreatedAt());
        response.setUpdatedAt(rating.getUpdatedAt());
        return response;
    }
}