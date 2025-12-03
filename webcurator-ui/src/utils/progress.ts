import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useProgressStore = defineStore('ProgressStore', () => {
  const timer = ref();
  const _visible = ref(false);
  const visible = ref(false);
  const start = () => {
    end();
    _visible.value = true;
    timer.value = setTimeout(() => {
      visible.value = _visible.value;
    }, 100);
  };

  const end = () => {
    _visible.value = false;
    visible.value = false;
    if (timer.value) {
      clearTimeout(timer.value);
    }
    timer.value = undefined;
  };

  return { visible, start, end };
});
