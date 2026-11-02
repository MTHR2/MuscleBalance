package com.gymtracker.repository;

import com.gymtracker.entity.Muscle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MuscleRepository extends JpaRepository<Muscle,Short> {


}
