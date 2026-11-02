package com.gymtracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table (name = "exercises")
public class Exercise {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "load_type", nullable = false)
    private LoadType loadType = LoadType.weighted;

    @Column(name = "is_custom", nullable = false)
    private boolean custom = false;

    @Column(name = "is_favourite", nullable = false)
    private boolean favourite = false;

    @Column(name = "working_weight", precision = 6, scale = 2)
    private BigDecimal workingWeight;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseMuscle> muscles = new ArrayList<>();
}
