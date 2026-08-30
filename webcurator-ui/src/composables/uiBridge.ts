import type { Component } from "vue";
import { reactive, ref } from "vue";

import type {
  ConfirmOptions,
  DialogState,
  DynamicDialogOpenOptions,
  ToastMessage,
} from "@/types/ui";

let dialogId = 0;
let toastId = 0;

export const toastState = reactive<{ items: Array<ToastMessage & { id: number }> }>({
  items: [],
});

export const confirmState = reactive<{ active?: ConfirmOptions }>({
  active: undefined,
});

export const dialogState = ref<DialogState | null>(null);

export function addToast(message: ToastMessage) {
  const id = ++toastId;
  toastState.items.push({ ...message, id });

  if (message.life && message.life > 0) {
    window.setTimeout(() => {
      toastState.items = toastState.items.filter((item) => item.id !== id);
    }, message.life);
  }
}

export function removeToastGroup(group: string) {
  toastState.items = toastState.items.filter((item) => item.group !== group);
}

export function removeAllToastGroups() {
  toastState.items = [];
}

export function requireConfirm(options: ConfirmOptions) {
  confirmState.active = options;
}

export function resolveConfirm(accepted: boolean) {
  const active = confirmState.active;
  confirmState.active = undefined;

  if (!active) {
    return;
  }

  if (accepted) {
    active.accept?.();
  } else {
    active.reject?.();
  }
}

export function openDialog(
  component: Component,
  options: DynamicDialogOpenOptions = {},
) {
  dialogState.value = {
    id: ++dialogId,
    component,
    options,
  };
}

export function closeDialog() {
  const current = dialogState.value;
  dialogState.value = null;
  current?.options.onClose?.();
}
