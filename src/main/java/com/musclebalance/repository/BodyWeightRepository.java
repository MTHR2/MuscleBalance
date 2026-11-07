package com.musclebalance.repository;

import com.musclebalance.entity.BodyWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BodyWeightRepository extends JpaRepository<BodyWeight, Long> {

    Optional<BodyWeight> findByMeasuredOn(LocalDate measuredOn);

    List<BodyWeight> findByMeasuredOnBetweenOrderByMeasuredOnAsc(LocalDate from, LocalDate to);

    Optional<BodyWeight> findFirstByMeasuredOnLessThanEqualOrderByMeasuredOnDesc(LocalDate date);
}
