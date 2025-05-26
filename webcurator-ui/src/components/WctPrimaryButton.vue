<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  icon?: string;
  label?: string;
  class?: string;
  iconPos?: string;
}>();

const customizedClass = computed(() => {
  if (!props.iconPos || props.iconPos == 'left') {
    return 'flex justify-start gap-1 wct-secondary-button ' + props.class;
  } else {
    return 'flex flex-row-reverse justify-start gap-1 wct-secondary-button ' + props.class;
  }
});

const emit = defineEmits(['click']);
const onClick = (event: any) => {
  emit('click', props);
};
</script>

<template>
  <Button :icon="icon" :label="label" :iconPos="iconPos" @click="onClick" class="wct-secondary-button" />
</template>

<style scoped>
.wct-secondary-button {
  border: 1px solid #3f51b5;
  background: #3f51b5;
  color: white;
  /* padding: 1em; */
}

.wct-secondary-button:hover {
  border: 1px solid #6f7699;
  background: #425cde;
  color: white;
  /* padding: 1em; */
}
</style>
