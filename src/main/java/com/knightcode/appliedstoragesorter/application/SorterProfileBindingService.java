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
                        Component.translatable("sorter.command.profile.error.no_profiles",
                                PROFILE_REPOSITORY.profileDirectory()));
            }

            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("sorter.command.profile.list_header"));
            for (int i = 0; i < profiles.size(); i++) {
                var storedProfile = profiles.get(i);
                lines.add(SorterComponentHelper.line((i + 1) + ". " + storedProfile.profile().name()
                        + " [id=" + storedProfile.profile().id()
                        + ", version=" + storedProfile.profile().version()
                        + "]"));
            }
            return SorterFeedbackResult.success(lines);
        } catch (IOException exception) {
            return SorterFeedbackResult.failure(Component.translatable("sorter.command.profile.error.list_failed"));
        }
    }

    public static SorterFeedbackResult bindProfile(CommandSourceStack source, int profileNumber) {
        if (!Config.ENABLE_SORTER.get()) {
            return SorterFeedbackResult.failure(Component.translatable("sorter.command.error.disabled"));
        }
        if (source.getEntity() == null) {
            return SorterFeedbackResult.failure(Component.translatable("sorter.command.error.player_only"));
        }
        if (source.getServer() == null) {
            return SorterFeedbackResult.failure(Component.translatable("sorter.command.error.no_server"));
        }

        var gridTarget = Ae2ControllerTargetResolver.resolveGridTarget(source);
        if (!gridTarget.success()) {
            return SorterFeedbackResult.failure(gridTarget.userMessage());
        }

        if (gridTarget.controllerPos() == null || gridTarget.dimensionId() == null) {
            return SorterFeedbackResult.failure(Component.translatable("sorter.command.profile.error.no_controller"));
        }

        try {
            var storedProfile = PROFILE_REPOSITORY.getByNumber(profileNumber);
            NetworkBindingKey bindingKey = new NetworkBindingKey(gridTarget.dimensionId(), gridTarget.controllerPos());
            new NetworkProfileBindingStore(source.getServer())
                    .bindProfile(bindingKey, storedProfile.profile().id());

            return SorterFeedbackResult.success(List.of(
                    Component.translatable("sorter.command.profile.bind_success", storedProfile.profile().name()),
                    SorterComponentHelper.keyValue("profileId", storedProfile.profile().id()),
                    SorterComponentHelper.keyValue("controller", gridTarget.controllerPos().toShortString()),
                    SorterComponentHelper.keyValue("dimension", gridTarget.dimensionId())));
        } catch (IllegalArgumentException exception) {
            return SorterFeedbackResult.failure(exception.getMessage());
        } catch (IOException exception) {
            return SorterFeedbackResult.failure(Component.translatable("sorter.command.profile.error.save_failed"));
        }
    }

    public static SorterFeedbackResult showBoundProfile(CommandSourceStack source) {
        if (!Config.ENABLE_SORTER.get()) {
            return SorterFeedbackResult.failure(Component.translatable("sorter.command.error.disabled"));
        }
        if (source.getEntity() == null) {
            return SorterFeedbackResult.failure(Component.translatable("sorter.command.error.player_only"));
        }
        if (source.getServer() == null) {
            return SorterFeedbackResult.failure(Component.translatable("sorter.command.error.no_server"));
        }

        var gridTarget = Ae2ControllerTargetResolver.resolveGridTarget(source);
        if (!gridTarget.success()) {
            return SorterFeedbackResult.failure(gridTarget.userMessage());
        }

        if (gridTarget.controllerPos() == null || gridTarget.dimensionId() == null) {
            return SorterFeedbackResult.failure(Component.translatable("sorter.command.profile.error.no_controller"));
        }

        try {
            NetworkBindingKey bindingKey = new NetworkBindingKey(gridTarget.dimensionId(), gridTarget.controllerPos());
            var profileId = new NetworkProfileBindingStore(source.getServer()).findProfileId(bindingKey).orElse(null);
            if (profileId == null) {
                return SorterFeedbackResult.failure(Component.translatable("sorter.command.profile.error.not_bound"));
            }

            var storedProfile = PROFILE_REPOSITORY.findById(profileId).orElse(null);
            if (storedProfile == null) {
                return SorterFeedbackResult.failure(
                        Component.translatable("sorter.command.profile.error.binding_missing", profileId));
            }

            return SorterFeedbackResult.success(List.of(
                    Component.translatable("sorter.command.profile.show_header"),
                    SorterComponentHelper.keyValue("name", storedProfile.profile().name()),
                    SorterComponentHelper.keyValue("id", storedProfile.profile().id()),
                    SorterComponentHelper.keyValue("version", String.valueOf(storedProfile.profile().version()))));
        } catch (IOException exception) {
            return SorterFeedbackResult.failure(Component.translatable("sorter.command.profile.error.read_failed"));
        }
    }
}
