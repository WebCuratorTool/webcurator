import { defineStore } from 'pinia';
import { useConfirm, useToast } from 'primevue';

const ToastLifeInfo = 3 * 1000;
const ToastLifeWarning = 5 * 1000;

const LogLevel = {
  TRACE: 1,
  DEBUG: 2,
  INFO: 3,
  WARNING: 4,
  ERROR: 5
};

export const useAlertStore = defineStore('AlertStore', () => {
  const curLogLevel = LogLevel.INFO;
  const toast = useToast();
  const confirm = useConfirm();

  const trace = (detail: string, header = 'Trace') => {
    if (curLogLevel <= LogLevel.TRACE) {
      console.trace(`${header}: ${detail}`);
    }
  };

  const debug = (detail: string, header = 'Trace') => {
    if (curLogLevel <= LogLevel.DEBUG) {
      console.debug(`${header}: ${detail}`);
    }
  };

  const info = (detail: string, header = 'Info') => {
    if (curLogLevel <= LogLevel.INFO) {
      console.info(`${header}: ${detail}`);
      toast.removeGroup('toast-info');
      toast.add({ group: 'toast-info', severity: 'info', summary: header, detail: detail, life: ToastLifeInfo });
    }
  };

  const warning = (detail: string, header = 'Warning') => {
    if (curLogLevel <= LogLevel.WARNING) {
      console.warn(`${header}: ${detail}`);
      toast.removeAllGroups();
      toast.add({ group: 'toast-error', severity: 'error', summary: header, detail: detail, life: ToastLifeWarning });
    }
  };

  const error = (detail: string, header = 'Error') => {
    if (curLogLevel <= LogLevel.ERROR) {
      console.error(`${header}: ${detail}`);
      toast.removeAllGroups();
      confirm.require({
        group: 'dlg-error',
        header: header,
        message: detail
      });
    }
  };

  return { trace, debug, info, warning, error };
});
