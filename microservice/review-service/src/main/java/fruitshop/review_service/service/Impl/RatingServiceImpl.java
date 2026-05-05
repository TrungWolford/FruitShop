package fruitshop.review_service.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import fruitshop.review_service.feign.AccountClient;
import fruitshop.review_service.feign.ProductClient;
import fruitshop.review_service.feign.dto.AccountSummaryDto;
import fruitshop.review_service.feign.dto.ProductSummaryDto;
import fruitshop.review_service.dto.request.Rating.CreateRatingRequest;
import fruitshop.review_service.dto.request.Rating.UpdateRatingRequest;
import fruitshop.review_service.dto.response.Rating.RatingResponse;
import fruitshop.review_service.dto.response.Rating.RatingDetailResponse;
import fruitshop.review_service.entity.Rating;
import fruitshop.review_service.repository.RatingRepository;
import fruitshop.review_service.service.RatingService;
import fruitshop.review_service.event.RatingCreatedEvent;
import fruitshop.review_service.event.RatingUpdatedEvent;
import fruitshop.review_service.exception.DownstreamServiceException;
import fruitshop.review_service.exception.ResourceNotFoundException;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private StreamBridge streamBridge;

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
            return ratingsPage.map(this::toDetailResponse);
        } catch (Exception e) {
            System.err.println("Error fetching all detailed ratings: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<RatingDetailResponse> getRatingsByAccountId(String accountId, Pageable pageable) {
        try {
            ensureAccountExists(accountId);

            Page<Rating> ratingsPage = ratingRepository.findByAccountId(accountId, pageable);
            if (ratingsPage == null || ratingsPage.isEmpty()) {
                return Page.empty(pageable);
            }
            return ratingsPage.map(this::toDetailResponse);
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
            ensureProductExists(productId);

            Page<Rating> ratingsPage = ratingRepository.findByProductIdAndStatus(productId, 1, pageable);
            if (ratingsPage == null || ratingsPage.isEmpty()) {
                return Page.empty(pageable);
            }
            return ratingsPage.map(this::toDetailResponse);
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
            ensureAccountExists(accountId);
            ensureProductExists(productId);

            List<Rating> ratings = ratingRepository.findByAccountIdAndProductId(accountId, productId);
            if (ratings == null || ratings.isEmpty()) {
                return List.of();
            }
            return ratings.stream().map(this::toDetailResponse).toList();
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
            ensureAccountExists(request.getAccountId());
            ensureProductExists(request.getProductId());

            if (request.getOrderItemId() != null && !request.getOrderItemId().isEmpty()) {
                if (ratingRepository.findByOrderItemId(request.getOrderItemId()).isPresent()) {
                    throw new RuntimeException("This order item has already been rated");
                }
            }

            Rating rating = new Rating();
            rating.setAccountId(request.getAccountId());
            rating.setProductId(request.getProductId());
            rating.setOrderItemId(request.getOrderItemId());
            rating.setComment(request.getComment());
            rating.setRatingStar(request.getRatingStar());
            rating.setStatus(1);

            Rating savedRating = ratingRepository.save(rating);
            
            // Publish Event
            RatingCreatedEvent event = new RatingCreatedEvent(
                    savedRating.getRatingId(),
                    savedRating.getProductId(),
                    savedRating.getRatingStar(),
                    savedRating.getComment()
            );
            streamBridge.send("ratingCreatedSupplier-out-0", event);
            
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

        streamBridge.send("ratingUpdatedSupplier-out-0", new RatingUpdatedEvent(
                updatedRating.getRatingId(),
                updatedRating.getProductId(),
                updatedRating.getRatingStar(),
                updatedRating.getStatus()
        ));

        return RatingResponse.fromEntity(updatedRating);
    }

    @Override
    public RatingResponse changeStatus(String ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with id: " + ratingId));

        rating.setStatus(rating.getStatus() == 1 ? 0 : 1);

        Rating updatedRating = ratingRepository.save(rating);

        streamBridge.send("ratingUpdatedSupplier-out-0", new RatingUpdatedEvent(
                updatedRating.getRatingId(),
                updatedRating.getProductId(),
                updatedRating.getRatingStar(),
                updatedRating.getStatus()
        ));

        return RatingResponse.fromEntity(updatedRating);
    }

    @Override
    public double calculateRatingStarByProductId(String productId) {
        try {
            ensureProductExists(productId);

            List<Rating> ratings = ratingRepository.findByProductId(productId);
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

    @Override
    public RatingResponse getRatingById(String ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with id: " + ratingId));
        return RatingResponse.fromEntity(rating);
    }

    @Override
    public void deleteRating(String ratingId) {
        if (!ratingRepository.existsById(ratingId)) {
            throw new ResourceNotFoundException("Rating not found with id: " + ratingId);
        }
        ratingRepository.deleteById(ratingId);
    }

    @Override
    public long countRatingsByProductId(String productId) {
        try {
            ensureProductExists(productId);
            List<Rating> ratings = ratingRepository.findByProductId(productId);
            if (ratings == null || ratings.isEmpty()) {
                return 0L;
            }
            return ratings.stream().filter(r -> r.getStatus() == 1).count();
        } catch (Exception e) {
            System.err.println("Error counting ratings for productId " + productId + ": " + e.getMessage());
            return 0L;
        }
    }

    private void ensureAccountExists(String accountId) {
        try {
            AccountSummaryDto account = accountClient.getAccountById(accountId);
            if (account == null || account.getAccountId() == null) {
                throw new ResourceNotFoundException("Account not found with id: " + accountId);
            }
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DownstreamServiceException("Account service unavailable while validating accountId: " + accountId);
        }
    }

    private void ensureProductExists(String productId) {
        try {
            ProductSummaryDto product = productClient.getProductById(productId);
            if (product == null || product.getProductId() == null) {
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            }
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DownstreamServiceException("Catalog service unavailable while validating productId: " + productId);
        }
    }

    private RatingDetailResponse toDetailResponse(Rating rating) {
        RatingDetailResponse response = RatingDetailResponse.fromEntity(rating);

        try {
            AccountSummaryDto account = accountClient.getAccountById(rating.getAccountId());
            if (account != null && response.getAccount() != null) {
                response.getAccount().setAccountName(account.getAccountName());
                response.getAccount().setAccountPhone(account.getAccountPhone());
            }
        } catch (Exception ignored) {
            // Keep response resilient even when account-service is unavailable.
        }

        try {
            ProductSummaryDto product = productClient.getProductById(rating.getProductId());
            if (product != null && response.getProduct() != null) {
                response.getProduct().setProductName(product.getProductName());
            }
        } catch (Exception ignored) {
            // Keep response resilient even when catalog-service is unavailable.
        }

        return response;
    }
}
