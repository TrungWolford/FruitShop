package fruitshop.review_service.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import fruitshop.review_service.dto.request.Rating.CreateRatingRequest;
import fruitshop.review_service.dto.request.Rating.UpdateRatingRequest;
import fruitshop.review_service.dto.response.Rating.RatingResponse;
import fruitshop.review_service.dto.response.Rating.RatingDetailResponse;
import fruitshop.review_service.entity.Account;
import fruitshop.review_service.entity.OrderItem;
import fruitshop.review_service.entity.Product;
import fruitshop.review_service.entity.Rating;
import fruitshop.review_service.repository.AccountRepository;
import fruitshop.review_service.repository.OrderItemRepository;
import fruitshop.review_service.repository.ProductRepository;
import fruitshop.review_service.repository.RatingRepository;
import fruitshop.review_service.service.RatingService;
import fruitshop.review_service.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public Page<RatingResponse> getAllRating(Pageable pageable) {
        try {
            Page<Rating> ratingsPage = ratingRepository.findAll(pageable);
            if (ratingsPage == null || ratingsPage.isEmpty()) {
                return Page.empty(pageable);
            }
            return ratingsPage.map(RatingResponse::fromEntity);
        } catch (Exception e) {
            System.err.println("Error fetching all ratings: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<RatingDetailResponse> getAllRatingDetailed(Pageable pageable) {
        try {
            Page<Rating> ratingsPage = ratingRepository.findAll(pageable);
            if (ratingsPage == null || ratingsPage.isEmpty()) {
                return Page.empty(pageable);
            }
            return ratingsPage.map(RatingDetailResponse::fromEntity);
        } catch (Exception e) {
            System.err.println("Error fetching all detailed ratings: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<RatingDetailResponse> getRatingsByAccountId(String accountId, Pageable pageable) {
        try {
            accountRepository.findById(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

            Page<Rating> ratingsPage = ratingRepository.findByAccountAccountId(pageable, accountId);
            if (ratingsPage == null || ratingsPage.isEmpty()) {
                return Page.empty(pageable);
            }
            return ratingsPage.map(RatingDetailResponse::fromEntity);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error fetching ratings for accountId " + accountId + ": " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<RatingDetailResponse> getRatingsByProductId(String productId, Pageable pageable) {
        try {
            productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

            Page<Rating> ratingsPage = ratingRepository.findByProductProductIdAndStatus(productId, 1, pageable);
            if (ratingsPage == null || ratingsPage.isEmpty()) {
                return Page.empty(pageable);
            }
            return ratingsPage.map(RatingDetailResponse::fromEntity);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error fetching ratings for productId " + productId + ": " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    @Override
    public List<RatingDetailResponse> getRatingsByAccountIdAndProductId(String accountId, String productId) {
        try {
            accountRepository.findById(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
            productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

            List<Rating> ratings = ratingRepository.findByAccountAccountIdAndProductProductId(accountId, productId);
            if (ratings == null || ratings.isEmpty()) {
                return List.of();
            }
            return ratings.stream().map(RatingDetailResponse::fromEntity).toList();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error fetching ratings for accountId " + accountId + " and productId " + productId + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public RatingResponse createRating(CreateRatingRequest request) {
        try {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + request.getAccountId()));

            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

            OrderItem orderItem = null;
            if (request.getOrderItemId() != null && !request.getOrderItemId().isEmpty()) {
                orderItem = orderItemRepository.findById(request.getOrderItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + request.getOrderItemId()));

                if (!orderItem.getOrder().getAccount().getAccountId().equals(request.getAccountId())) {
                    throw new RuntimeException("OrderItem does not belong to this account");
                }

                if (!orderItem.getProduct().getProductId().equals(request.getProductId())) {
                    throw new RuntimeException("OrderItem is not for this product");
                }

                if (ratingRepository.findByOrderItemOrderDetailId(request.getOrderItemId()).isPresent()) {
                    throw new RuntimeException("This order item has already been rated");
                }
            }

            Rating rating = new Rating();
            rating.setAccount(account);
            rating.setProduct(product);
            rating.setOrderItem(orderItem);
            rating.setComment(request.getComment());
            rating.setRatingStar(request.getRatingStar());
            rating.setStatus(1);

            Rating savedRating = ratingRepository.save(rating);
            return RatingResponse.fromEntity(savedRating);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error creating rating: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create rating: " + e.getMessage(), e);
        }
    }

    @Override
    public RatingResponse updateRating(UpdateRatingRequest request, String ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with id: " + ratingId));

        if (request.getComment() != null) {
            rating.setComment(request.getComment());
        }
        if (request.getRatingStar() > 0) {
            rating.setRatingStar(request.getRatingStar());
        }
        rating.setStatus(request.getStatus());

        Rating updatedRating = ratingRepository.save(rating);
        return RatingResponse.fromEntity(updatedRating);
    }

    @Override
    public RatingResponse changeStatus(String ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with id: " + ratingId));

        rating.setStatus(rating.getStatus() == 1 ? 0 : 1);

        Rating updatedRating = ratingRepository.save(rating);
        return RatingResponse.fromEntity(updatedRating);
    }

    @Override
    public double calculateRatingStarByProductId(String productId) {
        try {
            productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

            List<Rating> ratings = ratingRepository.findByProductProductId(productId);
            if (ratings == null || ratings.isEmpty()) {
                return 0.0;
            }

            List<Rating> activeRatings = ratings.stream()
                    .filter(rating -> rating.getStatus() == 1)
                    .toList();

            if (activeRatings.isEmpty()) {
                return 0.0;
            }

            double totalStars = activeRatings.stream().mapToDouble(Rating::getRatingStar).sum();
            double averageRating = totalStars / activeRatings.size();
            return Math.round(averageRating * 10.0) / 10.0;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error calculating rating for productId " + productId + ": " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
}