package com.musclebalance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table (name = "exercise_muscles")
public class ExerciseMuscle {
    @EmbeddedId
    private ExerciseMuscleId id = new ExerciseMuscleId();

    @MapsId("exerciseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    @MapsId("muscleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "muscle_id")
    private Muscle muscle;

    /** secondary в статистике считается как 0.5 подхода */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MuscleRole muscleRole;
}
