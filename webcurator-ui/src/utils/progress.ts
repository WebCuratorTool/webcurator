import { defineStore } from 'pinia';
// import { useConfirm } from 'primevue';
import { ref } from 'vue';

export const progressVisible = ref(false);

export const useProgressStore = defineStore('ProgressStore', () => {
  // const confirm = useConfirm();

  const visible = ref(false);
  const start = () => {
    visible.value = true;
    // confirm.require({
    //   group: 'progress',
    //   accept: () => {},
    //   reject: () => {}
    // });
  };

  const end = () => {
    visible.value = false;
  };

  return { visible, start, end };
});
