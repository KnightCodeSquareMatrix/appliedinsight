package com.knightcode.appliedstoragesorter.application;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

public final class NetworkProfileBindingStore {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final String FILE_NAME = "network-profile-bindings.json";

    private final Path filePath;

    public NetworkProfileBindingStore(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        this.filePath = server.getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve("AppliedStorageSorter")
                .resolve(FILE_NAME);
    }

    public Optional<String> findProfileId(NetworkBindingKey key) throws IOException {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(readBindings().get(key.asStorageKey()));
    }

    public void bindProfile(NetworkBindingKey key, String profileId) throws IOException {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(profileId, "profileId");

        Map<String, String> bindings = new LinkedHashMap<>(readBindings());
        bindings.put(key.asStorageKey(), profileId);
        writeBindings(bindings);
    }

    private Map<String, String> readBindings() throws IOException {
        if (!Files.exists(filePath)) {
            return Map.of();
        }

        try (Reader reader = Files.newBufferedReader(filePath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject bindingsObject = root.getAsJsonObject("bindings");
            if (bindingsObject == null) {
                return Map.of();
            }

            Map<String, String> bindings = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : bindingsObject.entrySet()) {
                if (!entry.getValue().isJsonNull()) {
                    bindings.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
            return bindings;
        }
    }

    private void writeBindings(Map<String, String> bindings) throws IOException {
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        JsonObject bindingsObject = new JsonObject();
        bindings.forEach(bindingsObject::addProperty);

        JsonObject root = new JsonObject();
        root.add("bindings", bindingsObject);

        try (Writer writer = Files.newBufferedWriter(filePath)) {
            GSON.toJson(root, writer);
        }
    }
}
