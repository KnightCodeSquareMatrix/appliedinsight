package com.knightcode.appliedstoragesorter.rule.route;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.neoforged.fml.loading.FMLPaths;

public final class RoutingProfileRepository {
    private static final Path PROFILE_DIR = FMLPaths.CONFIGDIR.get()
            .resolve("appliedstoragesorter")
            .resolve("profiles");

    public List<StoredProfile> listProfiles() throws IOException {
        Files.createDirectories(PROFILE_DIR);

        try (var stream = Files.list(PROFILE_DIR)) {
            return stream
                    .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .map(this::loadStoredProfile)
                    .flatMap(Optional::stream)
                    .sorted(Comparator.comparing((StoredProfile profile) -> profile.profile().name())
                            .thenComparing(profile -> profile.profile().id()))
                    .toList();
        }
    }

    public Optional<StoredProfile> findById(String profileId) throws IOException {
        Objects.requireNonNull(profileId, "profileId");
        return listProfiles().stream()
                .filter(profile -> profile.profile().id().equals(profileId))
                .findFirst();
    }

    public StoredProfile getByNumber(int number) throws IOException {
        List<StoredProfile> profiles = listProfiles();
        if (number < 1 || number > profiles.size()) {
            throw new IllegalArgumentException("Profile number out of range: " + number);
        }
        return profiles.get(number - 1);
    }

    public Path profileDirectory() {
        return PROFILE_DIR;
    }

    private Optional<StoredProfile> loadStoredProfile(Path path) {
        try {
            RoutingProfile profile = RoutingProfileJsonCodec.load(path);
            return Optional.of(new StoredProfile(path, profile));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public record StoredProfile(Path path, RoutingProfile profile) {
        public StoredProfile {
            Objects.requireNonNull(path, "path");
            Objects.requireNonNull(profile, "profile");
        }
    }
}
