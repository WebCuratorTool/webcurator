<script setup lang="ts">
import { computed, h, useSlots, type VNode } from "vue";

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

const getColumnProps = (column: any) => (column?.props ?? {}) as Record<string, any>;

const renderBody = (column: any, row: Record<string, any>, rowIndex: number) => {
  const bodySlot = column?.children?.body;
  if (typeof bodySlot === "function") {
    return bodySlot({ data: row, index: rowIndex });
  }

  return String(getValueByPath(row, getColumnProps(column).field));
};

const tableColumns = computed(() => {
  return columns.value.map((column, index) => {
    const columnProps = getColumnProps(column);
    if (columnProps.expander) {
      return {
        id: `expander_${index}`,
        header: columnProps.header ?? "",
        cell: ({ row }: any) =>
          h(
            "button",
            {
              type: "button",
              onClick: () => row.toggleExpanded(!row.getIsExpanded()),
            },
            row.getIsExpanded() ? "-" : "+",
          ),
      };
    }

    return {
      id: columnProps.field ?? `col_${index}`,
      accessorKey: columnProps.field,
      header: columnProps.header ?? "",
      cell: ({ row }: any) => renderBody(column, row.original, row.index),
    };
  });
});

const expanded = computed({
  get() {
    return props.expandedRows.reduce(
      (acc: Record<string, boolean>, row) => {
        const key = String(row?.[props.dataKey]);
        if (key && key !== "undefined") {
          acc[key] = true;
        }
        return acc;
      },
      {},
    );
  },
  set(nextExpanded: Record<string, boolean>) {
    const previousKeys = new Set(
      props.expandedRows.map((row) => String(row?.[props.dataKey])),
    );
    const nextKeys = Object.keys(nextExpanded).filter((key) => nextExpanded[key]);

    const firstExpandedKey = nextKeys[0];
    const rows = firstExpandedKey
      ? props.value.filter(
          (row) => String(row?.[props.dataKey]) === firstExpandedKey,
        )
      : [];

    emit("update:expandedRows", rows);

    if (firstExpandedKey && !previousKeys.has(firstExpandedKey) && rows[0]) {
      emit("rowExpand", { data: rows[0] });
    }
  },
});
</script>

<template>
  <div class="wct-table-wrapper">
    <UTable
      :data="value"
      :columns="tableColumns"
      :loading="loading"
      :get-row-id="(row: Record<string, any>) => String(row?.[dataKey])"
      v-model:expanded="expanded"
      class="wct-table"
    >
      <template #expanded="{ row }">
        <slot v-if="$slots.expansion" name="expansion" :data="row.original" />
      </template>
    </UTable>
    <div v-if="$slots.footer" class="wct-table-footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<style scoped>
.wct-table-wrapper {
  width: 100%;
}

.wct-table :deep(table) {
  width: 100%;
}

.wct-table-footer {
  margin-top: 0.5rem;
}
</style>
