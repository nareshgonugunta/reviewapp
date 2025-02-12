package com.signify.service;

import com.signify.entity.Review;
import com.signify.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review save(Review review) {
      return reviewRepository.save(review);
    }

    public List<Review> findAllReviews() {
        return reviewRepository.findAll();
    }

    public List<Review> findAllReviews(LocalDate date, String store, Integer rating) {
        return reviewRepository.findReviewsWithFilters(date, store, rating);
    }

    public Map<String, Map<String, Double>> averageMonthlyRatings() {
        List<Review> reviews = reviewRepository.findAll();

        Map<String, Map<String, List<Integer>>> monthlyRatings = new HashMap<>();

        for (Review review : reviews) {

            LocalDate localDate = review.getReviewedDate().atZone(ZoneId.of("UTC")).toLocalDate();
            YearMonth yearMonth = YearMonth.from(localDate);
            String monthYear = yearMonth.toString();
            String store = review.getReviewSource();

            monthlyRatings.computeIfAbsent(monthYear, k -> new HashMap<>())
                    .computeIfAbsent(store, k -> new ArrayList<>())
                    .add(review.getRating());
        }

        Map<String, Map<String, Double>> averages = new HashMap<>();
        for (Map.Entry<String, Map<String, List<Integer>>> entry : monthlyRatings.entrySet()) {
            String monthYear = entry.getKey();
            Map<String, List<Integer>> storeRatings = entry.getValue();
            averages.put(monthYear, new HashMap<>());
            for (Map.Entry<String, List<Integer>> storeEntry : storeRatings.entrySet()) {
                String store = storeEntry.getKey();
                List<Integer> ratings = storeEntry.getValue();
                double average = ratings.stream().mapToInt(Integer::intValue).average().orElse(0);
                averages.get(monthYear).put(store, average);
            }
        }
        return averages;
    }

    public Map<Integer, Long> getRatingCount() {
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                .collect(Collectors.groupingBy(
                        Review::getRating, Collectors.counting()));
    }
}
