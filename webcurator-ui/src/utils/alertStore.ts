import { defineStore } from 'pinia';
import { useConfirm, useToast } from 'primevue';

const ToastLifeInfo = 3 * 1000;
const ToastLifeWarning = 5 * 1000;

export const useAlertStore = defineStore('AlertStore', () => {
  const toast = useToast();
  const confirm = useConfirm();

  const trace = (detail: string, header = 'Trace') => {
    console.trace(`${header}: ${detail}`);
  };

  const info = (detail: string, header = 'Info') => {
    console.info(`${header}: ${detail}`);
    toast.removeGroup('toast-info');
    toast.add({ group: 'toast-info', severity: 'info', summary: header, detail: detail, life: ToastLifeInfo });
  };

  const warning = (detail: string, header = 'Warning') => {
    console.warn(`${header}: ${detail}`);
    toast.removeAllGroups();
    toast.add({ group: 'toast-error', severity: 'error', summary: header, detail: detail, life: ToastLifeWarning });
  };

  const error = (detail: string, header = 'Error') => {
    console.error(`${header}: ${detail}`);
    toast.removeAllGroups();
    confirm.require({
      group: 'dlg-error',
      header: header,
      message: detail
    });
  };

  return { trace, info, warning, error };
});
