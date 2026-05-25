package com.knightcode.appliedstoragesorter.rule.filter;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemFilterMatcherTest {

    private static final String SAMPLE_NBT = """
            {"components":{
              "apotheosis:rarity":{"quality":1.0,"value":"epic"},
              "minecraft:enchantments":{"levels":{"minecraft:sharpness":5,"minecraft:unbreaking":3}},
              "minecraft:damage":0
            }}""";

    private static final ItemMatchContext SIMPLE_CONTEXT = new ItemMatchContext(
            "minecraft:cobblestone", "minecraft", "Cobblestone",
            Set.of("minecraft:stone_crafting_materials", "c:cobblestone"),
            false, 12000L, "");

    private static final ItemMatchContext NBT_CONTEXT = new ItemMatchContext(
            "apotheosis:gem", "apotheosis", "Rarity Gem",
            Set.of("apotheosis:gem"), true, 64L, SAMPLE_NBT);

    // ─── Basic field matching (smoke tests) ────────────────

    @Test
    void itemIdEquals() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.ITEM_ID, FilterOperator.EQUALS, "minecraft:cobblestone")));
        assertTrue(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    @Test
    void itemIdMismatch() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.ITEM_ID, FilterOperator.EQUALS, "minecraft:dirt")));
        assertFalse(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    @Test
    void modIdEquals() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.MOD_ID, FilterOperator.EQUALS, "minecraft")));
        assertTrue(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    @Test
    void totalAmountGreaterOrEqual() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.TOTAL_AMOUNT, FilterOperator.GREATER_OR_EQUAL, "4096")));
        assertTrue(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    @Test
    void totalAmountLessOrEqual() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.TOTAL_AMOUNT, FilterOperator.LESS_OR_EQUAL, "100")));
        assertFalse(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    @Test
    void hasComponentsTrue() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.HAS_COMPONENTS, FilterOperator.IS_TRUE, null)));
        assertTrue(ItemFilterMatcher.matches(filter, NBT_CONTEXT));
        assertFalse(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    // ─── REGEX on ITEM_ID ──────────────────────────────────

    @Test
    void itemIdRegexMatch() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.ITEM_ID, FilterOperator.REGEX, "minecraft:.*")));
        assertTrue(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    @Test
    void itemIdRegexNoMatch() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.ITEM_ID, FilterOperator.REGEX, "thermal:.*")));
        assertFalse(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    @Test
    void itemIdRegexInvalidPatternReturnsFalse() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.ITEM_ID, FilterOperator.REGEX, "[invalid")));
        assertFalse(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    // ─── REGEX on DISPLAY_NAME ─────────────────────────────

    @Test
    void displayNameRegex() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.DISPLAY_NAME, FilterOperator.REGEX, "Rarity.*")));
        assertTrue(ItemFilterMatcher.matches(filter, NBT_CONTEXT));
    }

    // ─── NBT_PATH + REGEX ──────────────────────────────────

    @Test
    void nbtPathRegexMatch() {
        // value = "nbtPath||regex"
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.NBT_PATH, FilterOperator.REGEX,
                        "components.apotheosis:rarity.value||epic|mythic")));
        assertTrue(ItemFilterMatcher.matches(filter, NBT_CONTEXT));
    }

    @Test
    void nbtPathRegexMatchValue() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.NBT_PATH, FilterOperator.REGEX,
                        "components.apotheosis:rarity.value||epic|mythic")));
        assertTrue(ItemFilterMatcher.matches(filter, NBT_CONTEXT));
    }

    @Test
    void nbtPathRegexNoMatch() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.NBT_PATH, FilterOperator.REGEX,
                        "components.apotheosis:rarity.value||rare|common")));
        assertFalse(ItemFilterMatcher.matches(filter, NBT_CONTEXT));
    }

    // ─── NBT_PATH + EQUALS ─────────────────────────────────

    @Test
    void nbtPathEquals() {
        // value = "nbtPath||expectedValue"
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.NBT_PATH, FilterOperator.EQUALS,
                        "components.apotheosis:rarity.value||epic")));
        assertTrue(ItemFilterMatcher.matches(filter, NBT_CONTEXT));
    }

    @Test
    void nbtPathEqualsMismatch() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.NBT_PATH, FilterOperator.EQUALS,
                        "components.apotheosis:rarity.value||rare")));
        assertFalse(ItemFilterMatcher.matches(filter, NBT_CONTEXT));
    }

    // ─── NBT_PATH empty NBT ────────────────────────────────

    @Test
    void nbtPathEmptyNbtReturnsFalse() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.NBT_PATH, FilterOperator.REGEX, ".*")));
        assertFalse(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    // ─── REGEX on TAG ──────────────────────────────────────

    @Test
    void tagRegexMatch() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.TAG, FilterOperator.REGEX, "c:.*")));
        assertTrue(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    @Test
    void tagRegexNoMatch() {
        var filter = ItemFilter.allOf("t", "t", List.of(
                new FilterCondition(FilterField.TAG, FilterOperator.REGEX, "forge:.*")));
        assertFalse(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    // ─── AND / OR combinator ───────────────────────────────

    @Test
    void andCombinatorAllMatch() {
        var filter = new ItemFilter("t", "t", "", true, FilterGroup.and(List.of(
                new FilterCondition(FilterField.ITEM_ID, FilterOperator.REGEX, "minecraft:.*"),
                new FilterCondition(FilterField.MOD_ID, FilterOperator.EQUALS, "minecraft"))));
        assertTrue(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    @Test
    void andCombinatorOneMismatch() {
        var filter = new ItemFilter("t", "t", "", true, FilterGroup.and(List.of(
                new FilterCondition(FilterField.ITEM_ID, FilterOperator.REGEX, "minecraft:.*"),
                new FilterCondition(FilterField.MOD_ID, FilterOperator.EQUALS, "thermal"))));
        assertFalse(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    @Test
    void orCombinatorOneMatch() {
        var filter = new ItemFilter("t", "t", "", true, FilterGroup.or(List.of(
                new FilterCondition(FilterField.ITEM_ID, FilterOperator.EQUALS, "minecraft:dirt"),
                new FilterCondition(FilterField.MOD_ID, FilterOperator.EQUALS, "minecraft"))));
        assertTrue(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    @Test
    void disabledFilterReturnsFalse() {
        var filter = new ItemFilter("t", "t", "", false, FilterGroup.and(List.of(
                new FilterCondition(FilterField.ITEM_ID, FilterOperator.EQUALS, "minecraft:cobblestone"))));
        assertFalse(ItemFilterMatcher.matches(filter, SIMPLE_CONTEXT));
    }

    // ─── NbtPathExtractor unit tests ───────────────────────

    @Test
    void extractSimplePath() {
        String nbt = "{\"key\":\"value\"}";
        assertEquals("value", NbtPathExtractor.extract(nbt, "key"));
    }

    @Test
    void extractNestedPath() {
        assertEquals("epic", NbtPathExtractor.extract(SAMPLE_NBT, "components.apotheosis:rarity.value"));
    }

    @Test
    void extractArrayIndex() {
        String nbt = "{\"items\":[{\"id\":\"stone\"},{\"id\":\"dirt\"}]}";
        assertEquals("stone", NbtPathExtractor.extract(nbt, "items[0].id"));
        assertEquals("dirt", NbtPathExtractor.extract(nbt, "items[1].id"));
    }

    @Test
    void extractNonexistentPathReturnsNull() {
        assertNull(NbtPathExtractor.extract(SAMPLE_NBT, "nonexistent.path"));
    }

    @Test
    void extractNullNbtReturnsNull() {
        assertNull(NbtPathExtractor.extract(null, "path"));
    }

    @Test
    void extractEmptyNbtReturnsNull() {
        assertNull(NbtPathExtractor.extract("", "path"));
    }
}
