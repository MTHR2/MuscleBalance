package com.gymtracker.mapper;

import com.gymtracker.dto.bodyweight.BodyWeightDto;
import com.gymtracker.dto.bodyweight.BodyWeightPointDto;
import com.gymtracker.dto.profile.ProfileDto;
import com.gymtracker.entity.BodyWeight;
import com.gymtracker.entity.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class BodyWeightMapper {

    public BodyWeightDto toDto(BodyWeight bodyWeight) {
        return new BodyWeightDto(bodyWeight.getId(), bodyWeight.getMeasuredOn(), bodyWeight.getWeight());
    }

    public BodyWeightPointDto toPointDto(BodyWeight bodyWeight, BigDecimal avg7) {
        return new BodyWeightPointDto(bodyWeight.getId(), bodyWeight.getMeasuredOn(), bodyWeight.getWeight(), avg7);
    }

    public BodyWeight create(LocalDate date, BigDecimal weight) {
        var bodyWeight = new BodyWeight();
        bodyWeight.setMeasuredOn(date);
        bodyWeight.setWeight(weight);
        return bodyWeight;
    }

    public ProfileDto toProfileDto(Profile profile) {
        return new ProfileDto(profile.getTargetBodyWeight());
    }
}
