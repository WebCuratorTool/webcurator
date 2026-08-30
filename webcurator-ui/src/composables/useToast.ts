import {
  addToast,
  removeAllToastGroups,
  removeToastGroup,
} from "@/composables/uiBridge";
import type { ToastMessage } from "@/types/ui";

export function useToast() {
  return {
    add(message: ToastMessage) {
      addToast(message);
    },
    removeGroup(group: string) {
      removeToastGroup(group);
    },
    removeAllGroups() {
      removeAllToastGroups();
    },
  };
}
