(function () {
"use strict";

const LANG_STORAGE_KEY = "appliedinsight.filterEditor.lang";

const MESSAGES = {
  en: {
    pageTitle: "Applied Energistics: Insight · Filter Editor",
    title: "Smart Bus Filter Editor",
    subtitle:
        "Works offline — double-click to edit. When done, click <strong>Copy for Game</strong>, "
        + "then in Minecraft Smart Bus GUI click <strong>Paste</strong> → <strong>Save</strong>.",
    btnImport: "Import Clipboard",
    btnFormat: "Format JSON",
    btnCopy: "Copy for Game",
    ruleBuilder: "Rule Builder",
    builderHint: "Groups can nest — e.g. (A AND B) OR (C AND D)",
    btnAddRule: "+ Add Rule",
    btnAddGroup: "+ Add Group",
    jsonPreview: "JSON Preview",
    valid: "Valid",
    invalid: "Invalid",
    error: "Error",
    manualPasteLabel: "Manual JSON paste (when clipboard is blocked offline)",
    manualPastePlaceholder: "Paste FilterExpression JSON here, then click Apply",
    btnManualApply: "Apply Manual Paste",
    footer:
        "No network required. Dev sources live in <code>tools/filter-editor/</code>; "
        + "in-game the mod extracts to <code>config/appliedinsight/filter-editor/</code> "
        + "and opens as a local file.",
    rootGroup: "Root Group",
    nestedGroup: "Nested Group",
    match: "Match",
    removeGroup: "Remove Group",
    addRule: "+ Rule",
    addSubgroup: "+ Subgroup",
    field: "Field",
    operator: "Operator",
    value: "Value",
    remove: "Remove",
    langSwitch: "中文",
    statusImported: "Imported JSON.",
    statusCopied: "Copied FilterExpression JSON. Paste it into the in-game Smart Bus GUI.",
    statusFormatted: "Formatted JSON preview.",
    statusManualApplied: "Applied manual paste.",
    templatesTitle: "Quick templates",
    templates: {
      allItems: "All items",
      allItemsDesc: "Match every item (ITEM_ID matches any non-empty id).",
      durabilityItems: "Durability items",
      durabilityItemsDesc: "Items with minecraft:damage or minecraft:max_damage components.",
      ores: "Ores",
      oresDesc: "Ore tags (c:ores), namespace ore paths, or item ids ending with _ore.",
    },
    statusTemplateApplied: "Loaded template: {name}",
    errors: {
      metadataMissing:
          "metadata.js failed to load. Ensure metadata.js sits next to index.html "
          + "(run ./gradlew exportFilterEditorMetadata).",
      maxDepth: "Filter nesting exceeds max depth ({max})",
      unsupportedNode: "Unsupported filter node",
      unknownField: "Unknown field: {field}",
      operatorNotAllowed: "Operator {operator} is not allowed for field {field}",
      valueRequired: "Value required for {field} {operator}",
      unknownCombinator: "Unknown combinator: {combinator}",
      groupNeedsRules: "Each group must contain at least one rule",
      tooManyConditions: "Too many conditions (max {max})",
      jsonMustBeObject: "JSON must be an object",
      expectedFilterGroup: "Expected a FilterGroup with combinator and rules",
      rootMustBeGroup: "Root node must be a group",
      clipboardOffline: "Clipboard unavailable offline. Select JSON preview and press Ctrl+C.",
      clipboardEmpty: "Clipboard is empty. Paste JSON into the manual box below, then Apply.",
    },
    fields: {
      ITEM_ID: "Item ID",
      MOD_ID: "Mod ID",
      TAG: "Tag",
      DISPLAY_NAME: "Display Name",
      HAS_COMPONENTS: "Has Components",
      TOTAL_AMOUNT: "Total Amount",
      NBT_PATH: "NBT Path",
    },
    combinators: {
      AND: "AND",
      OR: "OR",
    },
  },
  zh: {
    pageTitle: "Applied Energistics: Insight · 过滤器编辑器",
    title: "智能总线过滤器编辑器",
    subtitle:
        "离线可用：双击打开即可编辑。完成后点 <strong>复制到游戏</strong>，"
        + "回到游戏 Smart Bus 界面点 <strong>粘贴</strong> → <strong>保存</strong>。",
    btnImport: "导入剪贴板",
    btnFormat: "格式化 JSON",
    btnCopy: "复制到游戏",
    ruleBuilder: "规则构建器",
    builderHint: "支持嵌套组合 — 例如 (A AND B) OR (C AND D)",
    btnAddRule: "+ 添加规则",
    btnAddGroup: "+ 添加组",
    jsonPreview: "JSON 预览",
    valid: "有效",
    invalid: "无效",
    error: "错误",
    manualPasteLabel: "手动粘贴 JSON（离线时剪贴板可能被浏览器拦截）",
    manualPastePlaceholder: "在此粘贴 FilterExpression JSON，然后点击应用",
    btnManualApply: "应用手动粘贴",
    footer:
        "无需联网。开发源文件位于 <code>tools/filter-editor/</code>；"
        + "游戏内 mod 会解压到 <code>config/appliedinsight/filter-editor/</code> 并以本地文件打开。",
    rootGroup: "根组",
    nestedGroup: "嵌套组",
    match: "匹配",
    removeGroup: "删除组",
    addRule: "+ 规则",
    addSubgroup: "+ 子组",
    field: "字段",
    operator: "操作符",
    value: "值",
    remove: "删除",
    langSwitch: "English",
    statusImported: "已导入 JSON。",
    statusCopied: "已复制 FilterExpression JSON。请在游戏 Smart Bus 界面粘贴。",
    statusFormatted: "已格式化 JSON 预览。",
    statusManualApplied: "已应用手动粘贴。",
    templatesTitle: "快速样板",
    templates: {
      allItems: "所有物品",
      allItemsDesc: "匹配任意物品（ITEM_ID 非空即可）。",
      durabilityItems: "有耐久的物品",
      durabilityItemsDesc: "含 minecraft:damage 或 minecraft:max_damage 组件的物品。",
      ores: "矿石",
      oresDesc: "匹配 c:ores 标签、*:ores/* 路径，或物品 ID 以 _ore 结尾。",
    },
    statusTemplateApplied: "已加载样板：{name}",
    errors: {
      metadataMissing:
          "metadata.js 加载失败。请确保 metadata.js 与 index.html 在同一目录 "
          + "（运行 ./gradlew exportFilterEditorMetadata）。",
      maxDepth: "过滤器嵌套超过最大深度（{max}）",
      unsupportedNode: "不支持的过滤器节点",
      unknownField: "未知字段：{field}",
      operatorNotAllowed: "字段 {field} 不允许使用操作符 {operator}",
      valueRequired: "{field} {operator} 需要填写值",
      unknownCombinator: "未知组合符：{combinator}",
      groupNeedsRules: "每个组至少包含一条规则",
      tooManyConditions: "条件过多（最多 {max} 条）",
      jsonMustBeObject: "JSON 必须是对象",
      expectedFilterGroup: "需要包含 combinator 和 rules 的 FilterGroup",
      rootMustBeGroup: "根节点必须是组",
      clipboardOffline: "离线模式下无法访问剪贴板。请选中 JSON 预览后按 Ctrl+C。",
      clipboardEmpty: "剪贴板为空。请在下方手动粘贴框输入 JSON 后点击应用。",
    },
    fields: {
      ITEM_ID: "物品 ID",
      MOD_ID: "模组 ID",
      TAG: "标签",
      DISPLAY_NAME: "显示名称",
      HAS_COMPONENTS: "含组件数据",
      TOTAL_AMOUNT: "总数量",
      NBT_PATH: "NBT 路径",
    },
    combinators: {
      AND: "且 (AND)",
      OR: "或 (OR)",
    },
  },
};

let currentLang = detectLang();

function detectLang() {
  const stored = localStorage.getItem(LANG_STORAGE_KEY);
  if (stored === "en" || stored === "zh") {
    return stored;
  }
  const browser = (navigator.language || "en").toLowerCase();
  return browser.startsWith("zh") ? "zh" : "en";
}

function getLang() {
  return currentLang;
}

function setLang(lang) {
  if (lang !== "en" && lang !== "zh") {
    return;
  }
  currentLang = lang;
  localStorage.setItem(LANG_STORAGE_KEY, lang);
  document.documentElement.lang = lang === "zh" ? "zh-CN" : "en";
}

function t(key, params = {}) {
  const parts = key.split(".");
  let value = MESSAGES[currentLang];
  for (const part of parts) {
    value = value?.[part];
  }
  if (value == null) {
    value = MESSAGES.en;
    for (const part of parts) {
      value = value?.[part];
    }
  }
  if (typeof value !== "string") {
    return key;
  }
  return value.replace(/\{(\w+)\}/g, (_, name) => String(params[name] ?? ""));
}

function fieldLabel(field) {
  return t(`fields.${field.name}`, {}) !== `fields.${field.name}`
      ? t(`fields.${field.name}`)
      : field.label;
}

function combinatorLabel(name, fallback) {
  return t(`combinators.${name}`, {}) !== `combinators.${name}`
      ? t(`combinators.${name}`)
      : fallback;
}

function applyStaticI18n(root = document) {
  document.title = t("pageTitle");

  root.querySelectorAll("[data-i18n]").forEach((element) => {
    const key = element.getAttribute("data-i18n");
    const text = t(key);
    if (element.hasAttribute("data-i18n-html")) {
      element.innerHTML = text;
    } else {
      element.textContent = text;
    }
  });

  root.querySelectorAll("[data-i18n-placeholder]").forEach((element) => {
    element.placeholder = t(element.getAttribute("data-i18n-placeholder"));
  });

  const langBtn = root.querySelector("#btn-lang");
  if (langBtn) {
    langBtn.textContent = t("langSwitch");
  }
}

window.FilterEditorI18n = {
  detectLang,
  getLang,
  setLang,
  t,
  fieldLabel,
  combinatorLabel,
  applyStaticI18n,
};
})();
