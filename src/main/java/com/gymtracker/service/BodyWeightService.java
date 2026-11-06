package com.gymtracker.service;

import com.gymtracker.dto.bodyweight.BodyWeightChartDto;
import com.gymtracker.dto.bodyweight.BodyWeightDto;
import com.gymtracker.dto.bodyweight.BodyWeightPointDto;
import com.gymtracker.dto.bodyweight.SaveBodyWeightRequestDto;
import com.gymtracker.dto.profile.ProfileDto;
import com.gymtracker.dto.profile.UpdateProfileRequestDto;
import com.gymtracker.dto.stats.StatsPeriod;
import com.gymtracker.entity.BodyWeight;
import com.gymtracker.entity.Profile;
import com.gymtracker.mapper.BodyWeightMapper;
import com.gymtracker.repository.BodyWeightRepository;
import com.gymtracker.repository.ProfileRepository;
import com.gymtracker.service.TimeWindows.Window;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BodyWeightService {
    private static final int AVG_DAYS = 7;
    private static final int CHANGE_DAYS = 30;

    private final BodyWeightRepository bodyWeightRepository;
    private final ProfileRepository profileRepository;
    private final BodyWeightMapper bodyWeightMapper;
    private final TimeWindows timeWindows;

    public record SaveResult(BodyWeightDto bodyWeight, boolean created) {
    }

    @Transactional(readOnly = true)
    public BodyWeightChartDto getChart(StatsPeriod period) {
        Window window = timeWindows.period(period);
        List<BodyWeight> all = bodyWeightRepository.findByMeasuredOnBetweenOrderByMeasuredOnAsc(
                window.firstDay().minusDays(AVG_DAYS - 1), window.lastDay());

        List<BodyWeightPointDto> points = new ArrayList<>();
        for (BodyWeight bw : all) {
            if (bw.getMeasuredOn().isBefore(window.firstDay())) {
                continue;
            }
            points.add(bodyWeightMapper.toPointDto(bw, average7(all, bw.getMeasuredOn())));
        }

        LocalDate today = timeWindows.today();
        BigDecimal current = bodyWeightRepository.findFirstByMeasuredOnLessThanEqualOrderByMeasuredOnDesc(today)
                .map(BodyWeight::getWeight).orElse(null);
        BigDecimal monthAgo = bodyWeightRepository
                .findFirstByMeasuredOnLessThanEqualOrderByMeasuredOnDesc(today.minusDays(CHANGE_DAYS))
                .map(BodyWeight::getWeight).orElse(null);
        BigDecimal target = profileRepository.getProfile().getTargetBodyWeight();

        return new BodyWeightChartDto(points,
                current,
                current != null && monthAgo != null ? current.subtract(monthAgo) : null,
                target,
                current != null && target != null ? target.subtract(current) : null);
    }

    @Transactional
    public SaveResult save(SaveBodyWeightRequestDto request) {
        LocalDate date = request.date() != null ? request.date() : timeWindows.today();
        return bodyWeightRepository.findByMeasuredOn(date)
                .map(existing -> {
                    existing.setWeight(request.weight());
                    return new SaveResult(bodyWeightMapper.toDto(existing), false);
                })
                .orElseGet(() -> {
                    BodyWeight created = bodyWeightRepository.save(bodyWeightMapper.create(date, request.weight()));
                    return new SaveResult(bodyWeightMapper.toDto(created), true);
                });
    }

    @Transactional
    public void delete(Long id) {
        BodyWeight bodyWeight = bodyWeightRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Замер не найден"));
        bodyWeightRepository.delete(bodyWeight);
    }

    @Transactional(readOnly = true)
    public ProfileDto getProfile() {
        return bodyWeightMapper.toProfileDto(profileRepository.getProfile());
    }

    @Transactional
    public ProfileDto updateProfile(UpdateProfileRequestDto request) {
        Profile profile = profileRepository.getProfile();
        profile.setTargetBodyWeight(request.targetBodyWeight());
        return bodyWeightMapper.toProfileDto(profile);
    }

    private BigDecimal average7(List<BodyWeight> sorted, LocalDate day) {
        LocalDate from = day.minusDays(AVG_DAYS - 1);
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (BodyWeight bw : sorted) {
            LocalDate d = bw.getMeasuredOn();
            if (!d.isBefore(from) && !d.isAfter(day)) {
                sum = sum.add(bw.getWeight());
                count++;
            }
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }
}
