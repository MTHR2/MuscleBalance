package com.musclebalance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
public class Profile {
    public static final short SINGLE_ID = 1;

    @Id
    private Short id;

    @Column(name = "target_body_weight", precision = 5, scale = 2)
    private BigDecimal targetBodyWeight;
}
