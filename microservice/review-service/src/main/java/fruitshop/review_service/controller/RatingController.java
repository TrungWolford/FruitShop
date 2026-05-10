package fruitshop.review_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import fruitshop.review_service.dto.request.Rating.CreateRatingRequest;
import fruitshop.review_service.dto.request.Rating.UpdateRatingRequest;
import fruitshop.review_service.dto.response.Rating.RatingResponse;
import fruitshop.review_service.dto.response.Rating.RatingDetailResponse;
import fruitshop.review_service.service.RatingService;

import java.util.List;

@RestController
@RequestMapping("/api/rating")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @GetMapping
    public ResponseEntity<Page<RatingDetailResponse>> getAllRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RatingDetailResponse> ratings = ratingService.getAllRatingDetailed(pageable);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/{ratingId}")
    public ResponseEntity<RatingResponse> getRatingById(@PathVariable String ratingId) {
        return ResponseEntity.ok(ratingService.getRatingById(ratingId));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<RatingDetailResponse>> getRatingsByAccountId(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RatingDetailResponse> ratings = ratingService.getRatingsByAccountId(accountId, pageable);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<RatingDetailResponse>> getRatingsByProductId(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RatingDetailResponse> ratings = ratingService.getRatingsByProductId(productId, pageable);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/account/{accountId}/product/{productId}")
    public ResponseEntity<List<RatingDetailResponse>> getRatingByAccountAndProduct(
            @PathVariable String accountId,
            @PathVariable String productId) {
        List<RatingDetailResponse> ratings = ratingService.getRatingsByAccountIdAndProductId(accountId, productId);
        return ResponseEntity.ok(ratings);
    }

    @PostMapping
    public ResponseEntity<RatingResponse> createRating(@RequestBody CreateRatingRequest request) {
        RatingResponse rating = ratingService.createRating(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }

    @PutMapping("/{ratingId}")
    public ResponseEntity<RatingResponse> updateRating(
            @PathVariable String ratingId,
            @RequestBody UpdateRatingRequest request) {
        RatingResponse rating = ratingService.updateRating(request, ratingId);
        return ResponseEntity.ok(rating);
    }

    @PatchMapping("/{ratingId}/status")
    public ResponseEntity<RatingResponse> changeStatus(@PathVariable String ratingId) {
        RatingResponse rating = ratingService.changeStatus(ratingId);
        return ResponseEntity.ok(rating);
    }

    @DeleteMapping("/{ratingId}")
    public ResponseEntity<Void> deleteRating(@PathVariable String ratingId) {
        ratingService.deleteRating(ratingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}/average")
    public ResponseEntity<Double> calculateAverageRating(@PathVariable String productId) {
        double averageRating = ratingService.calculateRatingStarByProductId(productId);
        return ResponseEntity.ok(averageRating);
    }

    @GetMapping("/product/{productId}/count")
    public ResponseEntity<Long> countRatingsByProductId(@PathVariable String productId) {
        long count = ratingService.countRatingsByProductId(productId);
        return ResponseEntity.ok(count);
    }
}
