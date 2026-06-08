package com.knightcode.appliedstoragesorter.client.filter;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;

@OnlyIn(Dist.CLIENT)
public final class FilterEditorSupport {
    private static final String RESOURCE_ROOT = "/appliedstoragesorter/filter-editor/";
    private static final List<String> BUNDLED_FILES = List.of(
            "index.html",
            "app.js",
            "i18n.js",
            "templates.js",
            "style.css",
            "metadata.js");

    private FilterEditorSupport() {
    }

    public static URI resolveEditorUri(String configuredUrl) throws IOException {
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            return URI.create(configuredUrl.trim());
        }
        return installBundledEditor().toUri();
    }

    public static Path installBundledEditor() throws IOException {
        Path targetDir = FMLPaths.CONFIGDIR.get()
                .resolve(AppliedStorageSorter.MODID)
                .resolve("filter-editor");
        Files.createDirectories(targetDir);

        for (String fileName : BUNDLED_FILES) {
            Path destination = targetDir.resolve(fileName);
            try (InputStream input = FilterEditorSupport.class.getResourceAsStream(RESOURCE_ROOT + fileName)) {
                if (input == null) {
                    throw new IOException("Missing bundled filter editor resource: " + fileName);
                }
                Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        return targetDir.resolve("index.html");
    }
}
