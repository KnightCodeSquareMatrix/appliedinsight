package com.knightcode.appliedstoragesorter.rule.filter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从序列化 NBT JSON 字符串中按点分隔路径提取值。
 *
 * <p>路径格式示例：
 * <ul>
 *   <li>{@code components.apotheosis:rarity.value} → 嵌套对象字段</li>
 *   <li>{@code components.minecraft:enchantments.levels.sharpness} → 深层嵌套</li>
 *   <li>{@code items[0].id} → 数组索引访问</li>
 * </ul>
 */
public final class NbtPathExtractor {

    private static final Pattern ARRAY_INDEX = Pattern.compile("^(.*?)\\[(\\d+)]$");

    private NbtPathExtractor() {
    }

    /**
     * 从 NBT JSON 字符串中提取指定路径的值。
     *
     * @param nbtJson 序列化的 NBT JSON 字符串
     * @param path    点分隔路径
     * @return 路径对应的字符串值，路径不存在返回 null
     */
    public static String extract(String nbtJson, String path) {
        if (nbtJson == null || nbtJson.isBlank() || path == null || path.isBlank()) {
            return null;
        }

        try {
            JsonElement root = JsonParser.parseString(nbtJson);
            return navigate(root, path);
        } catch (Exception e) {
            return null;
        }
    }

    private static String navigate(JsonElement element, String path) {
        String[] segments = path.split("\\.", -1);
        JsonElement current = element;

        for (String segment : segments) {
            if (current == null || current.isJsonNull()) {
                return null;
            }

            Matcher matcher = ARRAY_INDEX.matcher(segment);
            if (matcher.matches()) {
                // segment = "items[0]" → name="items", index=0
                String name = matcher.group(1);
                int index = Integer.parseInt(matcher.group(2));

                if (!name.isEmpty()) {
                    if (!current.isJsonObject()) return null;
                    current = current.getAsJsonObject().get(name);
                }

                if (current == null || !current.isJsonArray()) return null;
                JsonArray array = current.getAsJsonArray();
                if (index < 0 || index >= array.size()) return null;
                current = array.get(index);
            } else {
                if (!current.isJsonObject()) return null;
                current = current.getAsJsonObject().get(segment);
            }
        }

        if (current == null || current.isJsonNull()) {
            return null;
        }

        // 对基本类型返回字符串表示，对复合类型返回完整 JSON
        if (current.isJsonPrimitive()) {
            return current.getAsString();
        }
        return current.toString();
    }
}
