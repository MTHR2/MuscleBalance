package com.gymtracker.repository;

import com.gymtracker.entity.MuscleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MuscleGroupRepository extends JpaRepository<MuscleGroup, Short> {
}
