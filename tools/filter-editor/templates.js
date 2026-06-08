window.FILTER_EDITOR_TEMPLATES = [
  {
    id: "all_items",
    nameKey: "templates.allItems",
    descKey: "templates.allItemsDesc",
    expression: {
      combinator: "AND",
      rules: [
        {
          field: "ITEM_ID",
          operator: "REGEX",
          value: ".+",
        },
      ],
    },
  },
  {
    id: "durability_items",
    nameKey: "templates.durabilityItems",
    descKey: "templates.durabilityItemsDesc",
    expression: {
      combinator: "OR",
      rules: [
        {
          field: "NBT_PATH",
          operator: "REGEX",
          value: "components.minecraft:damage||.*",
        },
        {
          field: "NBT_PATH",
          operator: "REGEX",
          value: "components.minecraft:max_damage||.*",
        },
      ],
    },
  },
  {
    id: "ores",
    nameKey: "templates.ores",
    descKey: "templates.oresDesc",
    expression: {
      combinator: "OR",
      rules: [
        {
          field: "TAG",
          operator: "CONTAINS",
          value: "c:ores",
        },
        {
          field: "TAG",
          operator: "REGEX",
          value: ".+:ores/.*",
        },
        {
          field: "ITEM_ID",
          operator: "REGEX",
          value: ".*_ore$",
        },
      ],
    },
  },
];
