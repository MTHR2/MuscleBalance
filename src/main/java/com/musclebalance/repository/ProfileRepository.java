package com.musclebalance.repository;

import com.musclebalance.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Short> {

    default Profile getProfile() {
        return findById(Profile.SINGLE_ID).orElseThrow(() -> new IllegalStateException("Нет строки профиля"));
    }
}
