import type { Component } from "vue";

import { openDialog } from "@/composables/uiBridge";
import type { DynamicDialogOpenOptions } from "@/types/ui";

export function useDialog() {
  return {
    open(component: Component, options?: DynamicDialogOpenOptions) {
      openDialog(component, options);
    },
  };
}
