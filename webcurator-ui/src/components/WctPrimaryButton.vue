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
  <Button :icon="icon" :label="label" :iconPos="iconPos" @click="onClick" class="wct-primary-button" />
</template>

<style scoped></style>
