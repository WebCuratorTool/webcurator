import { defineStore } from "pinia";
import { useConfirm, useToast } from "primevue";

const ToastLifeInfo = 3 * 1000;
const ToastLifeWarning = 5 * 1000;

const LogLevel = {
  TRACE: 1,
  DEBUG: 2,
  INFO: 3,
  WARNING: 4,
  ERROR: 5,
  FATAL: 6,
};

export const useAlertStore = defineStore("AlertStore", () => {
  const curLogLevel = LogLevel.INFO;
  const toast = useToast();
  const confirm = useConfirm();

  const trace = (detail: string, header = "Trace") => {
    if (curLogLevel <= LogLevel.TRACE) {
      console.trace(`${header}: ${detail}`);
    }
  };

  const debug = (detail: string, header = "Debug") => {
    if (curLogLevel <= LogLevel.DEBUG) {
      console.debug(`${header}: ${detail}`);
    }
  };

  const info = (
    message: string,
    detail: string | null = null,
    header = "Info",
  ) => {
    if (curLogLevel <= LogLevel.INFO) {
      if (!detail) {
        detail = message;
      }
      console.info(`${header}: ${detail}`);
      toast.removeGroup("toast-info");
      toast.add({
        group: "toast-info",
        severity: "info",
        summary: header,
        detail: detail,
        life: ToastLifeInfo,
      });
    }
  };

  const warning = (
    message: string,
    detail: string | null = null,
    header = "Warning",
  ) => {
    if (curLogLevel <= LogLevel.WARNING) {
      if (!detail) {
        detail = message;
      }
      console.warn(`${header}: ${detail}`);
      toast.removeAllGroups();
      toast.add({
        group: "toast-error",
        severity: "warn",
        summary: header,
        detail: detail,
        life: ToastLifeWarning,
      });
    }
  };

  const _error = (
    message: string,
    detail: string | null = null,
    header = "Error",
  ) => {
    if (curLogLevel <= LogLevel.ERROR) {
      if (!detail) {
        detail = message;
      }
      console.error(`${header}: ${detail}`);
      toast.removeAllGroups();
      return new Promise((resolve: any) => {
        confirm.require({
          group: "dlg-error",
          header: header,
          message: message,
          accept: () => resolve(true),
          reject: () => resolve(false),
        });
      });
    }
  };

  const error = async (
    message: string,
    detail: string | null = null,
    header = "Error",
  ) => {
    await _error(message, detail, header);
  };

  return { trace, debug, info, warning, error };
});
