package com.knightcode.appliedstoragesorter.profilegen;

import java.util.List;
import java.util.Objects;

import com.knightcode.appliedstoragesorter.rule.route.RoutingProfile;

public record ProfileGenerationResult(
        RoutingProfile generatedProfile,
        List<String> notes) {
    public ProfileGenerationResult {
        generatedProfile = Objects.requireNonNull(generatedProfile, "generatedProfile");
        notes = List.copyOf(Objects.requireNonNull(notes, "notes"));
    }
}
