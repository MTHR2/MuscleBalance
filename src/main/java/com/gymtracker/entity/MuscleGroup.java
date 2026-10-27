package com.gymtracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table (name = "muscle_groups")
public class MuscleGroup {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private Short sortOrder = 0;

    @OneToMany(mappedBy = "muscleGroup")
    @OrderBy ("sortOrder")
    private List<Muscle> muscleList = new ArrayList<>();
}
