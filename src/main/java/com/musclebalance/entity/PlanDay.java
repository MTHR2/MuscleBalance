package com.musclebalance.entity;

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

/** День плана: какая тренировка из библиотеки стоит в этот день недели. Дни без записи = дни отдыха. */
@Entity
@Table(name = "plan_days",
        uniqueConstraints = @UniqueConstraint(name = "uq_plan_days_day", columnNames = {"plan_id", "day_of_week"}))
@Getter
@Setter
@NoArgsConstructor
public class PlanDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_template_id", nullable = false)
    private WorkoutTemplate workoutTemplate;

    /** 1 = понедельник ... 7 = воскресенье */
    @Column(name = "day_of_week", nullable = false)
    private Short dayOfWeek;
}
