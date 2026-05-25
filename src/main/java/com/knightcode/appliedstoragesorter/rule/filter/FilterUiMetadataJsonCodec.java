package com.knightcode.appliedstoragesorter.rule.filter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class FilterUiMetadataJsonCodec {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private FilterUiMetadataJsonCodec() {
    }

    public static FilterUiMetadata load(Path inputFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(inputFile)) {
            return fromJson(JsonParser.parseReader(reader).getAsJsonObject());
        }
    }

    public static void write(FilterUiMetadata metadata, Path outputFile) throws IOException {
        if (outputFile.getParent() != null) {
            Files.createDirectories(outputFile.getParent());
        }

        try (Writer writer = Files.newBufferedWriter(outputFile)) {
            GSON.toJson(toJson(metadata), writer);
        }
    }

    public static JsonObject toJson(FilterUiMetadata metadata) {
        return GSON.toJsonTree(metadata).getAsJsonObject();
    }

    public static FilterUiMetadata fromJson(JsonObject root) {
        return GSON.fromJson(root, FilterUiMetadata.class);
    }
}
