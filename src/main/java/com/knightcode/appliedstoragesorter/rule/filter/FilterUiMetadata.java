package com.knightcode.appliedstoragesorter.rule.filter;

import java.util.List;
import java.util.Objects;

public record FilterUiMetadata(
        int schemaVersion,
        List<FilterFieldDefinition> fields,
        List<FilterOperatorDefinition> operators,
        List<FilterCombinatorDefinition> combinators) {
    public FilterUiMetadata {
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("schemaVersion must be >= 1");
        }
        fields = List.copyOf(Objects.requireNonNull(fields, "fields"));
        operators = List.copyOf(Objects.requireNonNull(operators, "operators"));
        combinators = List.copyOf(Objects.requireNonNull(combinators, "combinators"));
    }

    public static FilterUiMetadata createDefault() {
        return new FilterUiMetadata(
                1,
                List.of(
                        new FilterFieldDefinition(
                                FilterField.ITEM_ID.name(),
                                "Item ID",
                                "Full Minecraft item id such as ae2:sky_stone_block.",
                                FilterValueType.STRING,
                                "text",
                                List.of(FilterOperator.EQUALS, FilterOperator.CONTAINS, FilterOperator.IN, FilterOperator.REGEX)),
                        new FilterFieldDefinition(
                                FilterField.MOD_ID.name(),
                                "Mod ID",
                                "Minecraft namespace / mod id such as ae2 or mekanism.",
                                FilterValueType.STRING,
                                "text",
                                List.of(FilterOperator.EQUALS, FilterOperator.CONTAINS, FilterOperator.IN)),
                        new FilterFieldDefinition(
                                FilterField.TAG.name(),
                                "Tag",
                                "Item tag such as c:ingots/iron or forge:storage_blocks.",
                                FilterValueType.STRING,
                                "text",
                                List.of(FilterOperator.EQUALS, FilterOperator.CONTAINS, FilterOperator.IN, FilterOperator.REGEX)),
                        new FilterFieldDefinition(
                                FilterField.DISPLAY_NAME.name(),
                                "Display Name",
                                "Localized item display name from the dump.",
                                FilterValueType.STRING,
                                "text",
                                List.of(FilterOperator.EQUALS, FilterOperator.CONTAINS, FilterOperator.IN, FilterOperator.REGEX)),
                        new FilterFieldDefinition(
                                FilterField.HAS_COMPONENTS.name(),
                                "Has Components",
                                "Whether the item stack carries non-empty component patch data.",
                                FilterValueType.BOOLEAN,
                                "none",
                                List.of(FilterOperator.IS_TRUE, FilterOperator.IS_FALSE)),
                        new FilterFieldDefinition(
                                FilterField.TOTAL_AMOUNT.name(),
                                "Total Amount",
                                "Total amount seen in the analyzed network dump.",
                                FilterValueType.NUMBER,
                                "number",
                                List.of(FilterOperator.EQUALS, FilterOperator.GREATER_OR_EQUAL, FilterOperator.LESS_OR_EQUAL)),
                        new FilterFieldDefinition(
                                FilterField.NBT_PATH.name(),
                                "NBT Path",
                                "Extract a value from item NBT using dot-separated path (e.g. components.apotheosis:rarity.value). Use REGEX for roll-value filtering.",
                                FilterValueType.STRING,
                                "text",
                                List.of(FilterOperator.REGEX, FilterOperator.EQUALS, FilterOperator.CONTAINS))),
                List.of(
                        new FilterOperatorDefinition(FilterOperator.EQUALS.name(), "=", "Exact equality match.", false),
                        new FilterOperatorDefinition(FilterOperator.CONTAINS.name(), "contains", "Case-insensitive substring match.", false),
                        new FilterOperatorDefinition(FilterOperator.IN.name(), "in", "Comma-separated candidate list match.", false),
                        new FilterOperatorDefinition(FilterOperator.GREATER_OR_EQUAL.name(), ">=", "Numeric greater-than-or-equal comparison.", false),
                        new FilterOperatorDefinition(FilterOperator.LESS_OR_EQUAL.name(), "<=", "Numeric less-than-or-equal comparison.", false),
                        new FilterOperatorDefinition(FilterOperator.IS_TRUE.name(), "is true", "Unary boolean true check.", true),
                        new FilterOperatorDefinition(FilterOperator.IS_FALSE.name(), "is false", "Unary boolean false check.", true),
                        new FilterOperatorDefinition(FilterOperator.REGEX.name(), "~", "Case-insensitive regex pattern match (Java Pattern). Use for NBT roll-value filtering.", false)),
                FilterCombinatorDefinition.defaults());
    }
}
