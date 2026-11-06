package com.gymtracker.controller;

import com.gymtracker.dto.profile.ProfileDto;
import com.gymtracker.dto.profile.UpdateProfileRequestDto;
import com.gymtracker.service.BodyWeightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile")
public class ProfileController {
    private final BodyWeightService bodyWeightService;

    @GetMapping
    public ProfileDto getProfile() {
        return bodyWeightService.getProfile();
    }

    @PutMapping
    public ProfileDto updateProfile(@Valid @RequestBody UpdateProfileRequestDto request) {
        return bodyWeightService.updateProfile(request);
    }
}
