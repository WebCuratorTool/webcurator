import type { Component } from "vue";

export interface ToastMessage {
  group?: string;
  severity?: string;
  summary?: string;
  detail?: string;
  life?: number;
}

export interface ConfirmOptions {
  group?: string;
  header?: string;
  message?: string;
  icon?: string;
  rejectLabel?: string;
  acceptLabel?: string;
  rejectClass?: string;
  acceptClass?: string;
  accept?: () => void;
  reject?: () => void;
}

export interface DynamicDialogOpenOptions {
  props?: Record<string, unknown>;
  data?: Record<string, unknown>;
  onClose?: () => void;
}

export interface DynamicDialogInstance {
  data?: Record<string, any>;
  close: () => void;
}

export interface DialogState {
  id: number;
  component: Component;
  options: DynamicDialogOpenOptions;
}

export interface SelectChangeEvent<T = any> {
  value: T;
}

export interface DataTableRowClickEvent {
  data: Record<string, any>;
}
