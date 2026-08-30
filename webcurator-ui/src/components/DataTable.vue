<script lang="ts">
import { defineComponent, h } from "vue";

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

export default defineComponent({
  name: "DataTable",
  props: {
    value: { type: Array, default: () => [] },
    dataKey: { type: String, default: "id" },
    expandedRows: { type: Array, default: () => [] },
    loading: { type: Boolean, default: false },
  },
  emits: ["update:expandedRows", "rowExpand"],
  setup(props, { slots, emit }) {
    const isExpanded = (row: Record<string, any>) =>
      (props.expandedRows as Record<string, any>[]).some(
        (expanded) => expanded?.[props.dataKey] === row?.[props.dataKey],
      );

    const toggleExpand = (row: Record<string, any>) => {
      if (isExpanded(row)) {
        emit(
          "update:expandedRows",
          (props.expandedRows as Record<string, any>[]).filter(
            (expanded) => expanded?.[props.dataKey] !== row?.[props.dataKey],
          ),
        );
        return;
      }

      emit("update:expandedRows", [row]);
      emit("rowExpand", { data: row });
    };

    return () => {
      const defaultNodes = flattenNodes(slots.default?.() ?? []);
      const columns = defaultNodes.filter((node) => {
        return node?.type && typeof node.type === "object" && node.type.name === "Column";
      });

      if (props.loading) {
        return h("div", { class: "wct-table-loading" }, "Loading...");
      }

      return h("div", { class: "wct-table-wrapper" }, [
        h("table", { class: "wct-table" }, [
          h(
            "thead",
            h(
              "tr",
              columns.map((column) =>
                h("th", { class: "wct-th" }, column.props?.header ?? ""),
              ),
            ),
          ),
          h(
            "tbody",
            (props.value as Record<string, any>[]).flatMap((row, rowIndex) => {
              const baseRow = h(
                "tr",
                { key: row?.[props.dataKey] ?? rowIndex },
                columns.map((column) => {
                  if (column.props?.expander) {
                    return h("td", { class: "wct-td" }, [
                      h(
                        "button",
                        {
                          type: "button",
                          onClick: () => toggleExpand(row),
                        },
                        isExpanded(row) ? "-" : "+",
                      ),
                    ]);
                  }

                  const bodySlot = column.children?.body;
                  if (typeof bodySlot === "function") {
                    return h("td", { class: "wct-td" }, bodySlot({ data: row, index: rowIndex }));
                  }

                  return h("td", { class: "wct-td" }, String(getValueByPath(row, column.props?.field)));
                }),
              );

              if (isExpanded(row) && slots.expansion) {
                const expansion = h(
                  "tr",
                  { key: `expansion-${row?.[props.dataKey] ?? rowIndex}` },
                  h(
                    "td",
                    { class: "wct-td", colspan: String(Math.max(columns.length, 1)) },
                    slots.expansion({ data: row }),
                  ),
                );

                return [baseRow, expansion];
              }

              return [baseRow];
            }),
          ),
        ]),
        slots.footer ? h("div", { class: "wct-table-footer" }, slots.footer()) : null,
      ]);
    };
  },
});
</script>

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
