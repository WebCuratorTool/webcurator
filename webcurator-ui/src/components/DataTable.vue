<script setup lang="ts">
import { computed, useSlots, type VNode } from "vue";

const getValueByPath = (object: Record<string, any>, path?: string) => {
  if (!path) {
    return "";
  }

  return path.split(".").reduce((acc: any, key) => (acc ? acc[key] : undefined), object) ?? "";
};

const flattenNodes = (nodes: any[]): any[] => {
  const out: any[] = [];

  nodes.forEach((node) => {
    if (!node) {
      return;
    }

    if (Array.isArray(node.children)) {
      out.push(...flattenNodes(node.children));
      return;
    }

    out.push(node);
  });

  return out;
};

const props = withDefaults(
  defineProps<{
    value?: Record<string, any>[];
    dataKey?: string;
    expandedRows?: Record<string, any>[];
    loading?: boolean;
  }>(),
  {
    value: () => [],
    dataKey: "id",
    expandedRows: () => [],
    loading: false,
  },
);

const emit = defineEmits<{
  "update:expandedRows": [rows: Record<string, any>[]];
  rowExpand: [event: { data: Record<string, any> }];
}>();

const slots = useSlots();

const isColumnNode = (node: VNode) => {
  if (!node?.type || typeof node.type !== "object") {
    return false;
  }

  const type = node.type as { name?: string; __name?: string };
  return type.name === "Column" || type.__name === "Column";
};

const columns = computed(() => {
  const defaultNodes = flattenNodes(slots.default?.() ?? []);
  return defaultNodes.filter((node) => isColumnNode(node as VNode));
});

const isExpanded = (row: Record<string, any>) =>
  props.expandedRows.some(
    (expanded) => expanded?.[props.dataKey] === row?.[props.dataKey],
  );

const toggleExpand = (row: Record<string, any>) => {
  if (isExpanded(row)) {
    emit(
      "update:expandedRows",
      props.expandedRows.filter(
        (expanded) => expanded?.[props.dataKey] !== row?.[props.dataKey],
      ),
    );
    return;
  }

  emit("update:expandedRows", [row]);
  emit("rowExpand", { data: row });
};

const getColumnProps = (column: any) => (column?.props ?? {}) as Record<string, any>;

const renderBody = (column: any, row: Record<string, any>, rowIndex: number) => {
  const bodySlot = column?.children?.body;
  if (typeof bodySlot === "function") {
    return bodySlot({ data: row, index: rowIndex });
  }

  return String(getValueByPath(row, getColumnProps(column).field));
};
</script>

<template>
  <div v-if="loading" class="wct-table-loading">Loading...</div>
  <div v-else class="wct-table-wrapper">
    <table class="wct-table">
      <thead>
        <tr>
          <th v-for="(column, colIndex) in columns" :key="colIndex" class="wct-th">
            {{ getColumnProps(column).header ?? "" }}
          </th>
        </tr>
      </thead>
      <tbody>
        <template v-for="(row, rowIndex) in value" :key="row?.[dataKey] ?? rowIndex">
          <tr>
            <td v-for="(column, colIndex) in columns" :key="colIndex" class="wct-td">
              <button
                v-if="getColumnProps(column).expander"
                type="button"
                @click="toggleExpand(row)"
              >
                {{ isExpanded(row) ? "-" : "+" }}
              </button>
              <template v-else>
                <component
                  :is="{ render: () => renderBody(column, row, rowIndex) }"
                />
              </template>
            </td>
          </tr>
          <tr v-if="isExpanded(row) && $slots.expansion">
            <td class="wct-td" :colspan="String(Math.max(columns.length, 1))">
              <slot name="expansion" :data="row" />
            </td>
          </tr>
        </template>
      </tbody>
    </table>
    <div v-if="$slots.footer" class="wct-table-footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<style scoped>
.wct-table-wrapper {
  width: 100%;
}

.wct-table {
  width: 100%;
  border-collapse: collapse;
}

.wct-th,
.wct-td {
  border-bottom: 1px solid #e2e8f0;
  text-align: left;
  padding: 0.5rem;
  vertical-align: top;
}

.wct-table-footer {
  margin-top: 0.5rem;
}
</style>
