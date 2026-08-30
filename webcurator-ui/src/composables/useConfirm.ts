import { requireConfirm } from "@/composables/uiBridge";
import type { ConfirmOptions } from "@/types/ui";

export function useConfirm() {
  return {
    require(options: ConfirmOptions) {
      requireConfirm(options);
    },
  };
}
