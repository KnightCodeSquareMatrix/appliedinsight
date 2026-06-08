package com.knightcode.appliedstoragesorter.client.gui.style;

import appeng.client.gui.style.ScreenStyle;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parallel to AE2 {@link appeng.client.gui.style.StyleManager}, loading screen JSON from
 * {@code AppliedStorageSorter:screens/...} instead of {@code ae2:screens/...}.
 */
public final class SorterStyleManager {
    private static final Map<String, ScreenStyle> STYLE_CACHE = new HashMap<>();
    public static final String PROP_INCLUDES = "includes";

    private static ResourceManager resourceManager;

    private SorterStyleManager() {
    }

    public static ScreenStyle loadStyleDoc(String path) {
        try {
            ScreenStyle style = loadStyleDocInternal(path);
            style.validate();
            return style;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to find Screen JSON file: " + path + ": " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Screen JSON file: " + path, e);
        }
    }

    public static void initialize(ResourceManager manager) {
        if (manager instanceof ReloadableResourceManager reloadable) {
            reloadable.registerReloadListener(new ReloadListener());
        }
        setResourceManager(manager);
    }

    private static void setResourceManager(ResourceManager manager) {
        resourceManager = manager;
        STYLE_CACHE.clear();
    }

    private static ScreenStyle loadStyleDocInternal(String path) throws IOException {
        ScreenStyle cached = STYLE_CACHE.get(path);
        if (cached != null) {
            return cached;
        }

        Set<String> resourcePacks = new HashSet<>();
        JsonObject document = loadMergedJsonTree(path, new HashSet<>(), resourcePacks);
        ScreenStyle style = ScreenStyle.GSON.fromJson(document, ScreenStyle.class);
        style.validate();
        STYLE_CACHE.put(path, style);
        return style;
    }

    private static JsonObject loadMergedJsonTree(String path, Set<String> loadedFiles, Set<String> resourcePacks)
            throws IOException {
        Preconditions.checkArgument(path.startsWith("/"), "Path needs to start with slash");

        if (path.contains("..")) {
            path = URI.create(path).normalize().toString();
        }

        if (!loadedFiles.add(path)) {
            throw new IllegalStateException("Recursive style includes: " + loadedFiles);
        }

        if (resourceManager == null) {
            throw new IllegalStateException("ResourceManager was not set. Was initialize called?");
        }

        String basePath = getBasePath(path);
        ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, path.substring(1));
        var resource = resourceManager.getResource(resourceId)
                .orElseThrow(() -> new FileNotFoundException(resourceId.toString()));
        resourcePacks.add(resource.sourcePackId());

        JsonObject document;
        try (var reader = resourceManager.openAsReader(resourceId)) {
            document = ScreenStyle.GSON.fromJson(reader, JsonObject.class);
        }

        if (document.has(PROP_INCLUDES)) {
            String[] includes = ScreenStyle.GSON.fromJson(document.get(PROP_INCLUDES), String[].class);
            List<JsonObject> layers = new ArrayList<>();
            for (String include : includes) {
                layers.add(loadMergedJsonTree(basePath + include, loadedFiles, resourcePacks));
            }
            layers.add(document);
            document = combineLayers(layers);
        }

        return document;
    }

    private static JsonObject combineLayers(List<JsonObject> layers) {
        JsonObject result = new JsonObject();
        for (JsonObject layer : layers) {
            for (Map.Entry<String, JsonElement> entry : layer.entrySet()) {
                result.add(entry.getKey(), entry.getValue());
            }
        }

        mergeObjectKeys("slots", layers, result);
        mergeObjectKeys("text", layers, result);
        mergeObjectKeys("palette", layers, result);
        mergeObjectKeys("images", layers, result);
        mergeObjectKeys("terminalStyle", layers, result);
        mergeObjectKeys("widgets", layers, result);
        return result;
    }

    private static void mergeObjectKeys(String propertyName, List<JsonObject> layers, JsonObject target)
            throws JsonParseException {
        JsonObject mergedObject = null;
        for (JsonObject layer : layers) {
            JsonElement layerEl = layer.get(propertyName);
            if (layerEl != null) {
                if (!layerEl.isJsonObject()) {
                    throw new JsonParseException("Expected " + propertyName + " to be an object, but was: " + layerEl);
                }
                JsonObject layerObj = layerEl.getAsJsonObject();
                if (mergedObject == null) {
                    mergedObject = new JsonObject();
                }
                for (Map.Entry<String, JsonElement> entry : layerObj.entrySet()) {
                    mergedObject.add(entry.getKey(), entry.getValue());
                }
            }
        }
        if (mergedObject != null) {
            target.add(propertyName, mergedObject);
        }
    }

    private static String getBasePath(String path) {
        int lastSep = path.lastIndexOf('/');
        if (lastSep == -1) {
            return "";
        }
        return path.substring(0, lastSep + 1);
    }

    private static class ReloadListener implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(ResourceManager manager) {
            setResourceManager(manager);
        }
    }
}
