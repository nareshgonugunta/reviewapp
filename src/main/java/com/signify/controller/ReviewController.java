package com.signify.controller;

import com.signify.entity.Review;
import com.signify.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody Review review) {
        Review savedEntity = reviewService.save(review);
        return new ResponseEntity<>(savedEntity, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) LocalDate date,
                                   @RequestParam(required = false) String store,
                                   @RequestParam(required = false) Integer rating) {

        return reviewService.findAllReviews(date, store, rating);
    }

    @GetMapping("/average_monthly_ratings")
    public Map<String, Map<String, Double>> getAverageMonthlyRatings() {
        Map<String, Map<String, Double>> averages  = reviewService.averageMonthlyRatings();
        return averages;
    }

    @GetMapping("/rating_counts")
    public Map<Integer, Long> getRatingCounts() {
        List<Review> reviews = reviewService.findAllReviews();
        return reviews.stream()
                .collect(Collectors.groupingBy(
                        Review::getRating, Collectors.counting()));
    }

}
