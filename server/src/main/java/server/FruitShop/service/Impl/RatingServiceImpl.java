package server.FruitShop.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import server.FruitShop.dto.request.Rating.CreateRatingRequest;
import server.FruitShop.dto.request.Rating.UpdateRatingRequest;
import server.FruitShop.dto.response.Rating.RatingResponse;
import server.FruitShop.dto.response.Rating.RatingDetailResponse;
import server.FruitShop.entity.Account;
import server.FruitShop.entity.Product;
import server.FruitShop.entity.Rating;
import server.FruitShop.repository.AccountRepository;
import server.FruitShop.repository.ProductRepository;
import server.FruitShop.repository.RatingRepository;
import server.FruitShop.service.RatingService;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Page<RatingResponse> getAllRating(Pageable pageable) {
        return ratingRepository.findAll(pageable)
                .map(RatingResponse::fromEntity);
    }

    @Override
    public Page<RatingDetailResponse> getAllRatingDetailed(Pageable pageable) {
        return ratingRepository.findAll(pageable)
                .map(RatingDetailResponse::fromEntity);
    }

    @Override
    public Page<RatingDetailResponse> getRatingsByAccountId(String accountId, Pageable pageable) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        String id = account.getAccountId();

        return ratingRepository.findByAccountAccountId(pageable, id)
                .map(RatingDetailResponse::fromEntity);
    }

    @Override
    public Page<RatingDetailResponse> getRatingsByProductId(String productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        String id = product.getProductId();

        // Only return active ratings (status = 1)
        return ratingRepository.findByProductProductIdAndStatus(pageable, id, 1)
                .map(RatingDetailResponse::fromEntity);
    }

    @Override
    public RatingDetailResponse getRatingsByAccountIdAndProductId(String accountId, String productId) {
        // Validate account exists
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Find rating by account and product
        Rating savedRating = ratingRepository.findByAccountAccountIdAndProductProductId(accountId, productId);
        
        // Return null if rating doesn't exist (user hasn't rated this product yet)
        if (savedRating == null) {
            return null;
        }
        
        return RatingDetailResponse.fromEntity(savedRating);
    }

    @Override
    public RatingResponse createRating(CreateRatingRequest request) {
        // Validate account exists
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + request.getAccountId()));

        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        // Check if user has already rated this product
        Rating existingRating = ratingRepository.findByAccountAccountIdAndProductProductId(
            request.getAccountId(), 
            request.getProductId()
        );
        
        if (existingRating != null) {
            throw new RuntimeException("You have already rated this product. Please update your existing rating instead.");
        }

        // Create new rating
        Rating rating = new Rating();
        rating.setAccount(account);
        rating.setProduct(product);
        rating.setComment(request.getComment());
        rating.setRatingStar(request.getRatingStar());
        rating.setStatus(1); // Default status is active

        // Save rating
        Rating savedRating = ratingRepository.save(rating);

        return RatingResponse.fromEntity(savedRating);
    }

    @Override
    public RatingResponse updateRating(UpdateRatingRequest request, String ratingId) {
        // Find existing rating
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found with id: " + ratingId));

        // Update fields
        if (request.getComment() != null) {
            rating.setComment(request.getComment());
        }
        if (request.getRatingStar() > 0) {
            rating.setRatingStar(request.getRatingStar());
        }
        rating.setStatus(request.getStatus());

        // Save updated rating
        Rating updatedRating = ratingRepository.save(rating);

        return RatingResponse.fromEntity(updatedRating);
    }

    @Override
    public RatingResponse changeStatus(String ratingId) {
        // Find existing rating
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found with id: " + ratingId));

        // Toggle status: 1 (active) <-> 0 (inactive)
        rating.setStatus(rating.getStatus() == 1 ? 0 : 1);

        // Save updated rating
        Rating updatedRating = ratingRepository.save(rating);

        return RatingResponse.fromEntity(updatedRating);
    }

    @Override
    public double calculateRatingStarByProductId(String productId) {
        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Get all ratings for this product (only active ones with status = 1)
        List<Rating> ratings = ratingRepository.findByProductProductId(productId);
        
        // Filter only active ratings
        List<Rating> activeRatings = ratings.stream()
                .filter(rating -> rating.getStatus() == 1)
                .toList();

        // If no ratings found, return 0
        if (activeRatings.isEmpty()) {
            return 0.0;
        }

        // Calculate average rating
        double totalStars = activeRatings.stream()
                .mapToDouble(Rating::getRatingStar)
                .sum();

        double averageRating = totalStars / activeRatings.size();

        // Round to 1 decimal place
        return Math.round(averageRating * 10.0) / 10.0;
    }
}
