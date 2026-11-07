package com.musclebalance.controller;

import com.musclebalance.dto.bodyweight.BodyWeightChartDto;
import com.musclebalance.dto.bodyweight.BodyWeightDto;
import com.musclebalance.dto.bodyweight.SaveBodyWeightRequestDto;
import com.musclebalance.dto.stats.StatsPeriod;
import com.musclebalance.service.BodyWeightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/body-weights")
public class BodyWeightController {
    private final BodyWeightService bodyWeightService;

    @GetMapping
    public BodyWeightChartDto getChart(@RequestParam(defaultValue = "QUARTER") StatsPeriod period) {
        return bodyWeightService.getChart(period);
    }

    @PostMapping
    public ResponseEntity<BodyWeightDto> save(@Valid @RequestBody SaveBodyWeightRequestDto request) {
        BodyWeightService.SaveResult result = bodyWeightService.save(request);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(result.bodyWeight());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bodyWeightService.delete(id);
    }
}
