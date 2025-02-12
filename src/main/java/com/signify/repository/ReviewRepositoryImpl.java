package com.signify.repository;

import com.signify.entity.Review;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReviewRepositoryImpl {

    @PersistenceContext
    EntityManager entityManager;

    public List<Review> findReviewsWithFilters(LocalDate date, String store, Integer rating) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> cq = cb.createQuery(Review.class);
        Root<Review> root = cq.from(Review.class);

        List<Predicate> predicates = new ArrayList<>();
        if (date != null) {
            Instant startOfDay = date.atStartOfDay(ZoneId.of("UTC")).toInstant();
            Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant();

            predicates.add(cb.between(root.get("reviewedDate"), startOfDay, endOfDay));
        }
        if (store != null && !store.isEmpty()) {
            predicates.add(cb.equal(root.get("reviewSource"), store));
        }
        if (rating != null) {
            predicates.add(cb.equal(root.get("rating"), rating));
        }
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }
        return entityManager.createQuery(cq).getResultList();
    }

}
