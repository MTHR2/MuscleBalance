package com.gymtracker.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workouts")
@Getter
@Setter
@NoArgsConstructor
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** null = свободная тренировка (или шаблон потом удалили) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_template_id")
    private WorkoutTemplate workoutTemplate;

    @Column(nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    /** null = тренировка идёт сейчас */
    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder")
    private List<WorkoutExercise> exercises = new ArrayList<>();
}
