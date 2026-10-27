package com.gymtracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class ExerciseMuscleId implements Serializable {

    @Column(name = "exercise_id")
    private Long exerciseId;

    @Column(name = "muscle_id")
    private Short muscleId;
}
