package com.knightcode.appliedstoragesorter.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.knightcode.appliedstoragesorter.Config;
import com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver;
import com.knightcode.appliedstoragesorter.application.result.SorterFeedbackResult;
import com.knightcode.appliedstoragesorter.rule.route.RoutingProfileRepository;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public final class SorterProfileBindingService {
    private static final RoutingProfileRepository PROFILE_REPOSITORY = new RoutingProfileRepository();

    private SorterProfileBindingService() {
    }

    public static SorterFeedbackResult listProfiles() {
        try {
            var profiles = PROFILE_REPOSITORY.listProfiles();
            if (profiles.isEmpty()) {
                return SorterFeedbackResult.failure(
                        "No global profiles found in " + PROFILE_REPOSITORY.profileDirectory());
            }

            List<Component> lines = new ArrayList<>();
            lines.add(SorterComponentHelper.line("Available global profiles:"));
            for (int i = 0; i < profiles.size(); i++) {
                var storedProfile = profiles.get(i);
                lines.add(SorterComponentHelper.line((i + 1) + ". " + storedProfile.profile().name()
                        + " [id=" + storedProfile.profile().id()
                        + ", version=" + storedProfile.profile().version()
                        + "]"));
            }
            return SorterFeedbackResult.success(lines);
        } catch (IOException exception) {
            return SorterFeedbackResult.failure("Failed to list global profiles. Check server logs.");
        }
    }

    public static SorterFeedbackResult bindProfile(CommandSourceStack source, int profileNumber) {
        if (!Config.ENABLE_SORTER.get()) {
            return SorterFeedbackResult.failure("Applied Storage Sorter is disabled in the server config.");
        }
        if (source.getEntity() == null) {
            return SorterFeedbackResult.failure("This command must be run by a player.");
        }
        if (source.getServer() == null) {
            return SorterFeedbackResult.failure("Server is not available.");
        }

        var gridTarget = Ae2ControllerTargetResolver.resolveGridTarget(source);
        if (!gridTarget.success()) {
            return SorterFeedbackResult.failure(gridTarget.userMessage());
        }

        if (gridTarget.controllerPos() == null || gridTarget.dimensionId() == null) {
            return SorterFeedbackResult.failure("Could not resolve controller identity for this ME network.");
        }

        try {
            var storedProfile = PROFILE_REPOSITORY.getByNumber(profileNumber);
            NetworkBindingKey bindingKey = new NetworkBindingKey(gridTarget.dimensionId(), gridTarget.controllerPos());
            new NetworkProfileBindingStore(source.getServer())
                    .bindProfile(bindingKey, storedProfile.profile().id());

            return SorterFeedbackResult.success(List.of(
                    SorterComponentHelper.line("Bound current ME network to global profile: " + storedProfile.profile().name()),
                    SorterComponentHelper.keyValue("profileId", storedProfile.profile().id()),
                    SorterComponentHelper.keyValue("controller", gridTarget.controllerPos().toShortString()),
                    SorterComponentHelper.keyValue("dimension", gridTarget.dimensionId())));
        } catch (IllegalArgumentException exception) {
            return SorterFeedbackResult.failure(exception.getMessage());
        } catch (IOException exception) {
            return SorterFeedbackResult.failure("Failed to save network profile binding. Check server logs.");
        }
    }

    public static SorterFeedbackResult showBoundProfile(CommandSourceStack source) {
        if (!Config.ENABLE_SORTER.get()) {
            return SorterFeedbackResult.failure("Applied Storage Sorter is disabled in the server config.");
        }
        if (source.getEntity() == null) {
            return SorterFeedbackResult.failure("This command must be run by a player.");
        }
        if (source.getServer() == null) {
            return SorterFeedbackResult.failure("Server is not available.");
        }

        var gridTarget = Ae2ControllerTargetResolver.resolveGridTarget(source);
        if (!gridTarget.success()) {
            return SorterFeedbackResult.failure(gridTarget.userMessage());
        }

        if (gridTarget.controllerPos() == null || gridTarget.dimensionId() == null) {
            return SorterFeedbackResult.failure("Could not resolve controller identity for this ME network.");
        }

        try {
            NetworkBindingKey bindingKey = new NetworkBindingKey(gridTarget.dimensionId(), gridTarget.controllerPos());
            var profileId = new NetworkProfileBindingStore(source.getServer()).findProfileId(bindingKey).orElse(null);
            if (profileId == null) {
                return SorterFeedbackResult.failure("Current ME network is not bound to any global profile.");
            }

            var storedProfile = PROFILE_REPOSITORY.findById(profileId).orElse(null);
            if (storedProfile == null) {
                return SorterFeedbackResult.failure(
                        "Current ME network is bound to missing profile id: " + profileId);
            }

            return SorterFeedbackResult.success(List.of(
                    SorterComponentHelper.line("Current ME network profile:"),
                    SorterComponentHelper.keyValue("name", storedProfile.profile().name()),
                    SorterComponentHelper.keyValue("id", storedProfile.profile().id()),
                    SorterComponentHelper.keyValue("version", String.valueOf(storedProfile.profile().version()))));
        } catch (IOException exception) {
            return SorterFeedbackResult.failure("Failed to read current network profile binding. Check server logs.");
        }
    }
}
