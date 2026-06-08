const i18n = window.FilterEditorI18n;

const MAX_DEPTH = 32;
const MAX_CONDITIONS = 256;

let metadata = null;

/** @type {{ type: 'group', combinator: string, rules: any[] } | null} */
let rootGroup = null;

const els = {
  rulesContainer: document.getElementById("rules-container"),
  jsonPreview: document.getElementById("json-preview"),
  validationBadge: document.getElementById("validation-badge"),
  statusMessage: document.getElementById("status-message"),
  btnAddRule: document.getElementById("btn-add-rule"),
  btnAddGroup: document.getElementById("btn-add-group"),
  btnImport: document.getElementById("btn-import"),
  btnFormat: document.getElementById("btn-format"),
  btnCopy: document.getElementById("btn-copy"),
  btnLang: document.getElementById("btn-lang"),
  manualPaste: document.getElementById("manual-paste"),
  btnManualApply: document.getElementById("btn-manual-apply"),
  templatesContainer: document.getElementById("templates-container"),
};

function loadMetadata() {
  if (!window.__FILTER_EDITOR_METADATA__) {
    throw new Error(i18n.t("errors.metadataMissing"));
  }
  metadata = window.__FILTER_EDITOR_METADATA__;
}

function fieldDef(name) {
  return metadata.fields.find((field) => field.name === name) ?? metadata.fields[0];
}

function operatorDef(name) {
  return metadata.operators.find((operator) => operator.name === name);
}

function defaultCondition() {
  const field = metadata.fields[0];
  return {
    type: "condition",
    field: field.name,
    operator: field.supportedOperators[0],
    value: "",
  };
}

function defaultGroup(combinator = "AND") {
  return {
    type: "group",
    combinator,
    rules: [defaultCondition()],
  };
}

function isCondition(node) {
  return node?.type === "condition" || ("field" in node && "operator" in node);
}

function isGroup(node) {
  return node?.type === "group" || ("combinator" in node && "rules" in node);
}

function parseNode(raw, depth = 0) {
  if (depth > MAX_DEPTH) {
    throw new Error(i18n.t("errors.maxDepth", { max: MAX_DEPTH }));
  }

  if ("field" in raw && "operator" in raw) {
    return {
      type: "condition",
      field: String(raw.field),
      operator: String(raw.operator),
      value: raw.value == null ? "" : String(raw.value),
    };
  }

  if ("combinator" in raw && Array.isArray(raw.rules)) {
    return {
      type: "group",
      combinator: String(raw.combinator),
      rules: raw.rules.map((child) => parseNode(child, depth + 1)),
    };
  }

  throw new Error(i18n.t("errors.unsupportedNode"));
}

function serializeNode(node) {
  if (node.type === "condition") {
    const op = operatorDef(node.operator);
    const payload = {
      field: node.field,
      operator: node.operator,
    };
    if (!op?.unary) {
      payload.value = node.value ?? "";
    }
    return payload;
  }

  return {
    combinator: node.combinator,
    rules: node.rules.map(serializeNode),
  };
}

function validateNode(node, depth = 0, conditionCount = { value: 0 }) {
  if (depth > MAX_DEPTH) {
    throw new Error(i18n.t("errors.maxDepth", { max: MAX_DEPTH }));
  }

  if (node.type === "condition") {
    conditionCount.value += 1;
    if (conditionCount.value > MAX_CONDITIONS) {
      throw new Error(i18n.t("errors.tooManyConditions", { max: MAX_CONDITIONS }));
    }

    const field = fieldDef(node.field);
    if (!field) {
      throw new Error(i18n.t("errors.unknownField", { field: node.field }));
    }
    if (!field.supportedOperators.includes(node.operator)) {
      throw new Error(i18n.t("errors.operatorNotAllowed", { field: node.field, operator: node.operator }));
    }
    const op = operatorDef(node.operator);
    if (!op?.unary && (node.value == null || String(node.value).trim() === "")) {
      throw new Error(i18n.t("errors.valueRequired", { field: node.field, operator: node.operator }));
    }
    return;
  }

  if (!metadata.combinators.some((item) => item.name === node.combinator)) {
    throw new Error(i18n.t("errors.unknownCombinator", { combinator: node.combinator }));
  }

  if (!Array.isArray(node.rules) || node.rules.length === 0) {
    throw new Error(i18n.t("errors.groupNeedsRules"));
  }

  for (const child of node.rules) {
    validateNode(child, depth + 1, conditionCount);
  }
}

function buildExpression() {
  return serializeNode(rootGroup);
}

function renderAll() {
  els.rulesContainer.replaceChildren();
  renderGroup(els.rulesContainer, rootGroup, { isRoot: true });
}

function renderGroup(container, group, options = {}) {
  const { isRoot = false, onRemove = null } = options;
  const box = document.createElement("div");
  box.className = isRoot ? "group-box root-group" : "group-box nested-group";

  const header = document.createElement("div");
  header.className = "group-header";

  const title = document.createElement("span");
  title.className = "group-title";
  title.textContent = isRoot ? i18n.t("rootGroup") : i18n.t("nestedGroup");

  const combinatorLabelEl = document.createElement("label");
  combinatorLabelEl.className = "combinator-label";
  combinatorLabelEl.textContent = i18n.t("match");
  const combinatorSelect = document.createElement("select");
  for (const item of metadata.combinators) {
    const option = document.createElement("option");
    option.value = item.name;
    option.textContent = i18n.combinatorLabel(item.name, `${item.label.toUpperCase()} (${item.name})`);
    combinatorSelect.append(option);
  }
  combinatorSelect.value = group.combinator;
  combinatorSelect.addEventListener("change", () => {
    group.combinator = combinatorSelect.value;
    refreshPreview();
  });
  combinatorLabelEl.append(combinatorSelect);

  header.append(title, combinatorLabelEl);

  if (!isRoot && onRemove) {
    const removeGroupButton = document.createElement("button");
    removeGroupButton.type = "button";
    removeGroupButton.className = "remove-group";
    removeGroupButton.textContent = i18n.t("removeGroup");
    removeGroupButton.addEventListener("click", onRemove);
    header.append(removeGroupButton);
  }

  const body = document.createElement("div");
  body.className = "group-body";

  group.rules.forEach((child, index) => {
    if (child.type === "condition") {
      body.append(renderConditionRow(child, () => {
        group.rules.splice(index, 1);
        ensureGroupNotEmpty(group);
        renderAll();
        refreshPreview();
      }));
    } else {
      body.append(renderGroupWrapper(child, () => {
        group.rules.splice(index, 1);
        ensureGroupNotEmpty(group);
        renderAll();
        refreshPreview();
      }));
    }
  });

  const actions = document.createElement("div");
  actions.className = "group-actions";

  const addRuleButton = document.createElement("button");
  addRuleButton.type = "button";
  addRuleButton.className = "ghost small";
  addRuleButton.textContent = i18n.t("addRule");
  addRuleButton.addEventListener("click", () => {
    group.rules.push(defaultCondition());
    renderAll();
    refreshPreview();
  });

  const addSubgroupButton = document.createElement("button");
  addSubgroupButton.type = "button";
  addSubgroupButton.className = "ghost small";
  addSubgroupButton.textContent = i18n.t("addSubgroup");
  addSubgroupButton.addEventListener("click", () => {
    group.rules.push(defaultGroup(group.combinator === "AND" ? "OR" : "AND"));
    renderAll();
    refreshPreview();
  });

  actions.append(addRuleButton, addSubgroupButton);

  box.append(header, body, actions);
  container.append(box);
}

function renderGroupWrapper(group, onRemove) {
  const wrapper = document.createElement("div");
  wrapper.className = "group-wrapper";
  renderGroup(wrapper, group, { isRoot: false, onRemove });
  return wrapper;
}

function renderConditionRow(condition, onRemove) {
  const field = fieldDef(condition.field);
  const row = document.createElement("div");
  row.className = "rule-row";

  const fieldLabelEl = document.createElement("label");
  fieldLabelEl.textContent = i18n.t("field");
  const fieldSelect = document.createElement("select");
  for (const item of metadata.fields) {
    const option = document.createElement("option");
    option.value = item.name;
    option.textContent = i18n.fieldLabel(item);
    fieldSelect.append(option);
  }
  fieldSelect.value = condition.field;
  fieldSelect.addEventListener("change", () => {
    const nextField = fieldDef(fieldSelect.value);
    condition.field = nextField.name;
    if (!nextField.supportedOperators.includes(condition.operator)) {
      condition.operator = nextField.supportedOperators[0];
    }
    renderAll();
    refreshPreview();
  });
  fieldLabelEl.append(fieldSelect);

  const operatorLabelEl = document.createElement("label");
  operatorLabelEl.textContent = i18n.t("operator");
  const operatorSelect = document.createElement("select");
  for (const operatorName of field.supportedOperators) {
    const def = operatorDef(operatorName);
    const option = document.createElement("option");
    option.value = operatorName;
    option.textContent = def ? `${def.label} (${operatorName})` : operatorName;
    operatorSelect.append(option);
  }
  operatorSelect.value = condition.operator;
  operatorSelect.addEventListener("change", () => {
    condition.operator = operatorSelect.value;
    renderAll();
    refreshPreview();
  });
  operatorLabelEl.append(operatorSelect);

  const valueLabelEl = document.createElement("label");
  valueLabelEl.className = "value-cell";
  valueLabelEl.textContent = i18n.t("value");
  const valueInput = document.createElement("input");
  valueInput.type = field.valueEditorType === "number" ? "number" : "text";
  valueInput.value = condition.value ?? "";
  valueInput.placeholder = field.description;
  valueInput.addEventListener("input", () => {
    condition.value = valueInput.value;
    refreshPreview();
  });
  valueLabelEl.append(valueInput);

  const removeButton = document.createElement("button");
  removeButton.type = "button";
  removeButton.className = "remove-rule";
  removeButton.textContent = i18n.t("remove");
  removeButton.addEventListener("click", onRemove);

  const op = operatorDef(condition.operator);
  if (op?.unary) {
    row.classList.add("unary");
  }

  row.append(fieldLabelEl, operatorLabelEl, valueLabelEl, removeButton);
  return row;
}

function ensureGroupNotEmpty(group) {
  if (group.rules.length === 0) {
    group.rules.push(defaultCondition());
  }
}

function unwrapExpression(raw) {
  if (!raw || typeof raw !== "object") {
    throw new Error(i18n.t("errors.jsonMustBeObject"));
  }
  if (raw.root && typeof raw.root === "object") {
    return raw.root;
  }
  return raw;
}

function refreshPreview() {
  let message = "";
  let ok = true;

  try {
    buildExpression();
    validateNode(rootGroup);
    els.jsonPreview.textContent = JSON.stringify(buildExpression(), null, 2);
    els.validationBadge.textContent = i18n.t("valid");
    els.validationBadge.className = "badge ok";
  } catch (error) {
    ok = false;
    message = error instanceof Error ? error.message : String(error);
    els.jsonPreview.textContent = message;
    els.validationBadge.textContent = i18n.t("invalid");
    els.validationBadge.className = "badge error";
  }

  els.statusMessage.textContent = message;
  els.statusMessage.className = `status-message ${ok ? "" : "error"}`.trim();
}

function importExpression(raw) {
  const expression = unwrapExpression(raw);
  if (!isGroup(expression)) {
    throw new Error(i18n.t("errors.expectedFilterGroup"));
  }
  rootGroup = parseNode(expression);
  if (rootGroup.type !== "group") {
    throw new Error(i18n.t("errors.rootMustBeGroup"));
  }
  renderAll();
  refreshPreview();
}

async function readClipboardText() {
  if (window.isSecureContext && navigator.clipboard?.readText) {
    return navigator.clipboard.readText();
  }
  return null;
}

function writeClipboardText(text) {
  if (window.isSecureContext && navigator.clipboard?.writeText) {
    return navigator.clipboard.writeText(text);
  }

  const helper = document.createElement("textarea");
  helper.value = text;
  helper.setAttribute("readonly", "");
  helper.style.position = "fixed";
  helper.style.left = "-9999px";
  document.body.appendChild(helper);
  helper.select();
  const copied = document.execCommand("copy");
  document.body.removeChild(helper);
  if (!copied) {
    throw new Error(i18n.t("errors.clipboardOffline"));
  }
  return Promise.resolve();
}

async function importClipboard() {
  try {
    let text = await readClipboardText();
    if (text == null || !text.trim()) {
      text = els.manualPaste.value;
    }
    if (!text || !text.trim()) {
      throw new Error(i18n.t("errors.clipboardEmpty"));
    }
    importExpression(JSON.parse(text));
    setStatus(i18n.t("statusImported"), "ok");
  } catch (error) {
    setStatus(error instanceof Error ? error.message : String(error), "error");
  }
}

async function copyForGame() {
  try {
    const expression = buildExpression();
    validateNode(rootGroup);
    const text = JSON.stringify(expression);
    await writeClipboardText(text);
    els.manualPaste.value = text;
    setStatus(i18n.t("statusCopied"), "ok");
  } catch (error) {
    setStatus(error instanceof Error ? error.message : String(error), "error");
  }
}

function formatJsonPreview() {
  try {
    const expression = buildExpression();
    validateNode(rootGroup);
    els.jsonPreview.textContent = JSON.stringify(expression, null, 2);
    setStatus(i18n.t("statusFormatted"), "ok");
  } catch (error) {
    setStatus(error instanceof Error ? error.message : String(error), "error");
  }
}

function setStatus(message, tone) {
  els.statusMessage.textContent = message;
  els.statusMessage.className = `status-message ${tone}`.trim();
}

function relocalizeUi() {
  i18n.applyStaticI18n();
  renderTemplates();
  renderAll();
  refreshPreview();
}

function renderTemplates() {
  const templates = window.FILTER_EDITOR_TEMPLATES;
  if (!templates || !els.templatesContainer) {
    return;
  }

  els.templatesContainer.replaceChildren();
  for (const template of templates) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "template-button";
    button.textContent = i18n.t(template.nameKey);
    button.title = i18n.t(template.descKey);
    button.addEventListener("click", () => applyTemplate(template));
    els.templatesContainer.append(button);
  }
}

function applyTemplate(template) {
  importExpression(template.expression);
  setStatus(i18n.t("statusTemplateApplied", { name: i18n.t(template.nameKey) }), "ok");
}

function toggleLanguage() {
  i18n.setLang(i18n.getLang() === "zh" ? "en" : "zh");
  relocalizeUi();
}

function bindActions() {
  els.btnLang.addEventListener("click", toggleLanguage);
  els.btnAddRule.addEventListener("click", () => {
    rootGroup.rules.push(defaultCondition());
    renderAll();
    refreshPreview();
  });
  els.btnAddGroup.addEventListener("click", () => {
    rootGroup.rules.push(defaultGroup(rootGroup.combinator === "AND" ? "OR" : "AND"));
    renderAll();
    refreshPreview();
  });
  els.btnImport.addEventListener("click", () => {
    importClipboard();
  });
  els.btnFormat.addEventListener("click", formatJsonPreview);
  els.btnCopy.addEventListener("click", () => {
    copyForGame();
  });
  els.btnManualApply.addEventListener("click", () => {
    try {
      importExpression(JSON.parse(els.manualPaste.value));
      setStatus(i18n.t("statusManualApplied"), "ok");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : String(error), "error");
    }
  });
}

function bootstrap() {
  try {
    document.documentElement.lang = i18n.getLang() === "zh" ? "zh-CN" : "en";
    loadMetadata();
    rootGroup = defaultGroup("AND");
    i18n.applyStaticI18n();
    renderTemplates();
    renderAll();
    refreshPreview();
    bindActions();
  } catch (error) {
    els.jsonPreview.textContent = error instanceof Error ? error.message : String(error);
    els.validationBadge.textContent = i18n.t("error");
    els.validationBadge.className = "badge error";
  }
}

bootstrap();
