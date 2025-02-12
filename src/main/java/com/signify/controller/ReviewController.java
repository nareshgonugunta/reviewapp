package com.signify.controller;

import com.signify.entity.Review;
import com.signify.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    public ResponseEntity<List<Review>> getReviews(@RequestParam(required = false) LocalDate date,
                                   @RequestParam(required = false) String store,
                                   @RequestParam(required = false) Integer rating) {

        List<Review> allReviews = reviewService.findAllReviews(date, store, rating);
        return new ResponseEntity<>(allReviews, HttpStatus.OK);
    }

    @GetMapping("/average_monthly_ratings")
    public ResponseEntity<Map<String, Map<String, Double>>> getAverageMonthlyRatings() {
        Map<String, Map<String, Double>> averages  = reviewService.averageMonthlyRatings();
        return new ResponseEntity<>(averages, HttpStatus.OK);
    }

    @GetMapping("/rating_counts")
    public ResponseEntity<Map<Integer, Long>> getRatingCounts() {
        Map<Integer, Long> result = reviewService.getRatingCount();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
