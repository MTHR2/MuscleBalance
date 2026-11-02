package com.gymtracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workout_template_exercises",
        uniqueConstraints = @UniqueConstraint(name = "uq_workout_template_exercises", columnNames = {"workout_template_id", "exercise_id"}))
@Getter
@Setter
@NoArgsConstructor
public class WorkoutTemplateExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_template_id", nullable = false)
    private WorkoutTemplate workoutTemplate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "sort_order", nullable = false)
    private Short sortOrder = 0;

    /** Число подходов по умолчанию; при старте тренировки копируется в planned_sets и target_sets */
    @Column(name = "target_sets", nullable = false)
    private Short targetSets;

    @Column(name = "reps_min")
    private Short repsMin;

    @Column(name = "reps_max")
    private Short repsMax;
}
