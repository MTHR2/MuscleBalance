package com.musclebalance.entity;

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
public class PlanMuscleTargetId implements Serializable {

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "muscle_id")
    private Short muscleId;
}
