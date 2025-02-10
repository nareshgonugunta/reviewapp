package com.signify;

import com.signify.entity.Review;
import com.signify.repository.ReviewRepository;
import com.signify.repository.ReviewRepositoryImpl;
import com.signify.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepositoryImpl reviewRepositoryImpl;

    @Mock
    private ReviewRepository reviewRepository;

    @Test
    public void testGetReviews_noFilters() {
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("review1", "author1", "source1", 4, "title1", "product1", Instant.now()));
        reviews.add(new Review("review2", "author2", "source2", 5, "title2", "product2", Instant.now()));

        when(reviewRepositoryImpl.findReviewsWithFilters(null, null, null)).thenReturn(reviews);

        List<Review> result = reviewService.findAllReviews(null, null, null);

        assertEquals(2, result.size());
        assertEquals("review1", result.get(0).getReview());
        assertEquals("review2", result.get(1).getReview());
    }

    @Test
    public void testGetReviews_withDateFilter() {
        LocalDate date = LocalDate.now();
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("review1", "author1", "source1", 4, "title1", "product1", Instant.now()));

        when(reviewRepositoryImpl.findReviewsWithFilters(date, null, null)).thenReturn(reviews);

        List<Review> result = reviewService.findAllReviews(date, null, null);

        assertEquals(1, result.size());
        assertEquals("review1", result.get(0).getReview());
    }


    @Test
    public void testGetReviews_withStoreFilter() {
        String store = "source1";
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("review1", "author1", "source1", 4, "title1", "product1", Instant.now()));

        when(reviewRepositoryImpl.findReviewsWithFilters(null, store, null)).thenReturn(reviews);

        List<Review> result = reviewService.findAllReviews(null, store, null);

        assertEquals(1, result.size());
        assertEquals("review1", result.get(0).getReview());
    }

    @Test
    public void testGetReviews_withRatingFilter() {
        int rating = 4;
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("review1", "author1", "source1", 4, "title1", "product1", Instant.now()));

        when(reviewRepositoryImpl.findReviewsWithFilters(null, null, rating)).thenReturn(reviews);

        List<Review> result = reviewService.findAllReviews(null, null, rating);

        assertEquals(1, result.size());
        assertEquals("review1", result.get(0).getReview());
    }

    @Test
    public void testGetReviews_noReviewsFound() {
        when(reviewRepositoryImpl.findReviewsWithFilters(any(), any(), any())).thenReturn(new ArrayList<>()); // Empty list

        List<Review> result = reviewService.findAllReviews(LocalDate.now(), "test", 5);

        assertTrue(result.isEmpty());
    }


    @Test
    public void testAverageMonthlyRatings_emptyReviews() {
        when(reviewRepository.findAll()).thenReturn(new ArrayList<>());

        Map<String, Map<String, Double>> averages = reviewService.averageMonthlyRatings();

        assertTrue(averages.isEmpty());
    }

    @Test
    public void testAverageMonthlyRatings_singleReview() {
        List<Review> reviews = new ArrayList<>();
        Instant now = Instant.now();

        reviews.add(new Review("review1", "author1", "source1", 4, "title1", "product1", now));
        when(reviewRepository.findAll()).thenReturn(reviews);

        Map<String, Map<String, Double>> averages = reviewService.averageMonthlyRatings();

        YearMonth yearMonth = YearMonth.from(now);
        String monthYear = yearMonth.toString();

        assertEquals(1, averages.size());
        assertTrue(averages.containsKey(monthYear));
        assertEquals(1, averages.get(monthYear).size());
        assertTrue(averages.get(monthYear).containsKey("source1"));
        assertEquals(4.0, averages.get(monthYear).get("source1"));
    }

    @Test
    public void testAverageMonthlyRatings_multipleReviews() {
        List<Review> reviews = new ArrayList<>();
        Instant now = Instant.now();
        Instant past = Instant.now().minusSeconds(60 * 60 * 24 * 30);

        reviews.add(new Review("review1", "author1", "source1", 4, "title1", "product1", now));
        reviews.add(new Review("review2", "author2", "source2", 5, "title2", "product2", now));
        reviews.add(new Review("review3", "author1", "source1", 3, "title3", "product3", past));

        when(reviewRepository.findAll()).thenReturn(reviews);

        Map<String, Map<String, Double>> averages = reviewService.averageMonthlyRatings();

        YearMonth currentMonth = YearMonth.from(now);
        String currentMonthYear = currentMonth.toString();
        YearMonth pastMonth = YearMonth.from(past);
        String pastMonthYear = pastMonth.toString();

        assertEquals(2, averages.size());
        assertTrue(averages.containsKey(currentMonthYear));
        assertTrue(averages.containsKey(pastMonthYear));

        assertEquals(2, averages.get(currentMonthYear).size());
        assertTrue(averages.get(currentMonthYear).containsKey("source1"));
        assertTrue(averages.get(currentMonthYear).containsKey("source2"));
        assertEquals(3.5, averages.get(currentMonthYear).get("source1"));
        assertEquals(5.0, averages.get(currentMonthYear).get("source2"));

        assertEquals(1, averages.get(pastMonthYear).size());
        assertTrue(averages.get(pastMonthYear).containsKey("source1"));
        assertEquals(3.0, averages.get(pastMonthYear).get("source1"));
    }

    @Test
    public void testAverageMonthlyRatings_noReviewsForMonth() {
        List<Review> reviews = new ArrayList<>();
        Instant now = Instant.now();
        reviews.add(new Review("review1", "author1", "source1", 4, "title1", "product1", now));

        when(reviewRepository.findAll()).thenReturn(reviews);

        Map<String, Map<String, Double>> averages = reviewService.averageMonthlyRatings();

        YearMonth nextMonth = YearMonth.from(now).plusMonths(1);
        String nextMonthYear = nextMonth.toString();

        assertFalse(averages.containsKey(nextMonthYear));
    }

    @Test
    public void mainTest() {
        Instant instant = Instant.now();
        LocalDate localDate = instant.atZone(ZoneId.of("UTC")).toLocalDate();
        YearMonth yearMonth = YearMonth.from(localDate);
        System.out.println(yearMonth);
    }

    @Test
    public void testSaveReview() {
        Review review = new Review("test review", "test author", "test source", 4, "test title", "test product", Instant.now());
        when(reviewRepository.save(review)).thenReturn(review);

        Review savedReview = reviewService.save(review);

        assertNotNull(savedReview);
        assertEquals("test review", savedReview.getReview());
        assertEquals("test author", savedReview.getAuthor());
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    public void testFindAllReviews_emptyReviews() {
        when(reviewRepository.findAll()).thenReturn(new ArrayList<>());
        List<Review> reviews = reviewService.findAllReviews();

        assertTrue(reviews.isEmpty());
        verify(reviewRepository, times(1)).findAll();
    }

    @Test
    public void testFindAllReviews_multipleReviews() {
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("review1", "author1", "source1", 4, "title1", "product1", Instant.now()));
        reviews.add(new Review("review2", "author2", "source2", 5, "title2", "product2", Instant.now()));
        when(reviewRepository.findAll()).thenReturn(reviews);

        List<Review> foundReviews = reviewService.findAllReviews();

        assertEquals(2, foundReviews.size());
        assertEquals("review1", foundReviews.get(0).getReview());
        assertEquals("review2", foundReviews.get(1).getReview());
        verify(reviewRepository, times(1)).findAll();
    }
}
