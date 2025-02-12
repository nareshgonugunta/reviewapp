package com.signify;

import com.signify.entity.Review;
import com.signify.repository.ReviewRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<Review> criteriaQuery;

    @Mock
    private Root<Review> root;

    @Mock
    private TypedQuery<Review> typedQuery;

    @InjectMocks
    private ReviewRepositoryImpl reviewRepository;

    @Test
    public void testFindReviewsWithFilters_noFilters() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Review.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Review.class)).thenReturn(root);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(entityManager.createQuery(criteriaQuery).getResultList()).thenReturn(loadReviews());

        List<Review> reviews = reviewRepository.findReviewsWithFilters(null, null, null);

        assertFalse(reviews.isEmpty());
        verify(entityManager, times(1)).getCriteriaBuilder();
        verify(criteriaBuilder, times(1)).createQuery(Review.class);
        verify(criteriaQuery, times(1)).from(Review.class);
        verify(entityManager, times(2)).createQuery(criteriaQuery);
    }

    @Test
    public void testFindReviewsWithFilters_withDateFilter() {
        LocalDate date = LocalDate.now();
        Instant startOfDay = date.atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant();

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Review.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Review.class)).thenReturn(root);

        when(criteriaBuilder.between(root.get("reviewedDate"), startOfDay, endOfDay)).thenReturn(mock(Predicate.class));
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);

        when(entityManager.createQuery(criteriaQuery)).thenReturn(mock(jakarta.persistence.TypedQuery.class));
        when(entityManager.createQuery(criteriaQuery).getResultList()).thenReturn(new ArrayList<>());


        List<Review> reviews = reviewRepository.findReviewsWithFilters(date, null, null);

        assertTrue(reviews.isEmpty());
        verify(criteriaBuilder, times(1)).between(root.get("reviewedDate"), startOfDay, endOfDay);
        verify(criteriaQuery, times(1)).where(any(Predicate[].class));

    }

    @Test
    public void testFindReviewsWithFilters_withAllFilters() {
        LocalDate date = LocalDate.now();
        Instant startOfDay = date.atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant();
        String store = "testStore";
        Integer rating = 4;
        Predicate datePredicate = mock(Predicate.class);
        Predicate storePredicate = mock(Predicate.class);
        Predicate ratingPredicate = mock(Predicate.class);
        Predicate[] predicatesArray = new Predicate[3];

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Review.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Review.class)).thenReturn(root);

        when(criteriaBuilder.between(root.get("reviewedDate"), startOfDay, endOfDay)).thenReturn(datePredicate);
        when(criteriaBuilder.equal(root.get("reviewSource"), store)).thenReturn(storePredicate);
        when(criteriaBuilder.equal(root.get("rating"), rating)).thenReturn(ratingPredicate);

        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(entityManager.createQuery(criteriaQuery).getResultList()).thenReturn(loadReviews());

        List<Review> reviews = reviewRepository.findReviewsWithFilters(date, store, rating);

        assertFalse(reviews.isEmpty());
        verify(criteriaBuilder, times(1)).between(root.get("reviewedDate"), startOfDay, endOfDay);
        verify(criteriaBuilder, times(1)).equal(root.get("reviewSource"), store);
        verify(criteriaBuilder, times(1)).equal(root.get("rating"), rating);
        verify(criteriaQuery, times(1)).where(any(Predicate[].class));
    }

    //Load Review Records for mocking
    private List<Review> loadReviews() {
        List<Review> reviewList = new ArrayList<>();
        reviewList.add(new Review("review1", "author1", "itunes", 4, "title1", "Alexa", Instant.now()));
        reviewList.add(new Review("review2", "author2", "itunes", 5, "title2", "Alexa", Instant.now()));
        reviewList.add(new Review("review3", "author3", "google", 5, "title3", "Alexa", Instant.now()));
        reviewList.add(new Review("review4", "author4", "google", 3, "title4", "Alexa", Instant.now()));
        return reviewList;
    }
}
