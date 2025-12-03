<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  label?: string;
  class?: string;
}>();

const customizedClass = computed(() => {
  return 'flex flex-col justify-start gap-1 ' + props.class;
});
</script>

<template>
  <div :class="customizedClass">
    <span>{{ label }}</span>
    <slot></slot>
  </div>
</template>

<style>
#wct-input input {
  padding: 0.5rem;
  border-radius: 0;
}
</style>
