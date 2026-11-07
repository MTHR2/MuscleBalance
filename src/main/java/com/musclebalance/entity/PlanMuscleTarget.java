package com.musclebalance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table (name = "plan_muscle_targets")
public class PlanMuscleTarget {
    @EmbeddedId
    private PlanMuscleTargetId id = new PlanMuscleTargetId();

    @MapsId("planId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @MapsId("muscleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "muscle_id")
    private Muscle muscle;

    /** Перекрывает общую цель плана; 0 = мышцу в плане не качаем */
    @Column(name = "weekly_sets", nullable = false)
    private Short weeklySets;
}
