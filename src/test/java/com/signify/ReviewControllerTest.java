package com.signify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signify.controller.ReviewController;
import com.signify.entity.Review;
import com.signify.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    public void testAddReview() throws Exception {
        Review review = new Review("test review", "test author", "test source", 4, "test title", "test product", Instant.now());
        when(reviewService.save(review)).thenReturn(review); // Mock service call

        mockMvc.perform(MockMvcRequestBuilders.post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.review").value("test review"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.author").value("test author"));
    }

    @Test
    public void testGetReviews_noFilters() throws Exception {
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("review1", "author1", "source1", 4, "title1", "product1", Instant.now()));
        when(reviewService.findAllReviews(null, null, null)).thenReturn(reviews);

        mockMvc.perform(MockMvcRequestBuilders.get("/reviews"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].review").value("review1"));
    }

    @Test
    public void testGetReviews_withDateFilter() throws Exception {
        LocalDate date = LocalDate.now();
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("review1", "author1", "source1", 4, "title1", "product1", Instant.now()));
        when(reviewService.findAllReviews(date, null, null)).thenReturn(reviews);

        mockMvc.perform(MockMvcRequestBuilders.get("/reviews")
                        .param("date", date.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].review").value("review1"));
    }

    @Test
    public void testGetAverageMonthlyRatings() throws Exception {
        Map<String, Map<String, Double>> averages = new HashMap<>();
        Map<String, Double> storeAverages = new HashMap<>();
        storeAverages.put("source1", 4.5);
        averages.put("2024-10", storeAverages);

        when(reviewService.averageMonthlyRatings()).thenReturn(averages);

        mockMvc.perform(MockMvcRequestBuilders.get("/reviews/average_monthly_ratings"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.'2024-10'.source1").value(4.5));
    }

    @Test
    public void testGetRatingCounts() throws Exception {
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("review1", "author1", "source1", 4, "title1", "product1", Instant.now()));
        reviews.add(new Review("review2", "author2", "source2", 5, "title2", "product2", Instant.now()));

        when(reviewService.findAllReviews()).thenReturn(reviews);

        mockMvc.perform(MockMvcRequestBuilders.get("/reviews/rating_counts"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.4").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.5").value(1));
    }
}