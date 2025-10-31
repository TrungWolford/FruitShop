package server.FruitShop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.FruitShop.dto.request.Rating.CreateRatingRequest;
import server.FruitShop.dto.request.Rating.UpdateRatingRequest;
import server.FruitShop.dto.response.Rating.RatingResponse;
import server.FruitShop.dto.response.Rating.RatingDetailResponse;
import server.FruitShop.service.RatingService;

import java.util.List;

@RestController
@RequestMapping("/api/rating")
@CrossOrigin(origins = "*")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    /**
     * Get all ratings with pagination
     * GET /api/rating?page=0&size=10
     * Returns RatingDetailResponse with full account and product info for admin
     */
    @GetMapping
    public ResponseEntity<Page<RatingDetailResponse>> getAllRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        // Use a new method that returns detailed responses
        Page<RatingDetailResponse> ratings = ratingService.getAllRatingDetailed(pageable);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Get ratings by account ID
     * GET /api/rating/account/{accountId}?page=0&size=10
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<RatingDetailResponse>> getRatingsByAccountId(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RatingDetailResponse> ratings = ratingService.getRatingsByAccountId(accountId, pageable);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Get ratings by product ID
     * GET /api/rating/product/{productId}?page=0&size=10
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<RatingDetailResponse>> getRatingsByProductId(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RatingDetailResponse> ratings = ratingService.getRatingsByProductId(productId, pageable);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Get ratings by account ID and product ID
     * Returns list of all ratings (can be multiple if user purchased and rated multiple times)
     * GET /api/rating/account/{accountId}/product/{productId}
     */
    @GetMapping("/account/{accountId}/product/{productId}")
    public ResponseEntity<List<RatingDetailResponse>> getRatingByAccountAndProduct(
            @PathVariable String accountId,
            @PathVariable String productId) {
        
        List<RatingDetailResponse> ratings = ratingService.getRatingsByAccountIdAndProductId(accountId, productId);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Create new rating
     * POST /api/rating
     */
    @PostMapping
    public ResponseEntity<RatingResponse> createRating(@RequestBody CreateRatingRequest request) {
        RatingResponse rating = ratingService.createRating(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }

    /**
     * Update existing rating
     * PUT /api/rating/{ratingId}
     */
    @PutMapping("/{ratingId}")
    public ResponseEntity<RatingResponse> updateRating(
            @PathVariable String ratingId,
            @RequestBody UpdateRatingRequest request) {
        
        RatingResponse rating = ratingService.updateRating(request, ratingId);
        return ResponseEntity.ok(rating);
    }

    /**
     * Change rating status (toggle active/inactive)
     * PATCH /api/rating/{ratingId}/status
     */
    @PatchMapping("/{ratingId}/status")
    public ResponseEntity<RatingResponse> changeStatus(@PathVariable String ratingId) {
        RatingResponse rating = ratingService.changeStatus(ratingId);
        return ResponseEntity.ok(rating);
    }

    /**
     * Delete rating (soft delete by changing status)
     * DELETE /api/rating/{ratingId}
     */
    @DeleteMapping("/{ratingId}")
    public ResponseEntity<RatingResponse> deleteRating(@PathVariable String ratingId) {
        RatingResponse rating = ratingService.changeStatus(ratingId);
        return ResponseEntity.ok(rating);
    }

    /**
     * Calculate average rating for a product
     * GET /api/rating/product/{productId}/average
     */
    @GetMapping("/product/{productId}/average")
    public ResponseEntity<Double> calculateAverageRating(@PathVariable String productId) {
        double averageRating = ratingService.calculateRatingStarByProductId(productId);
        return ResponseEntity.ok(averageRating);
    }
}
