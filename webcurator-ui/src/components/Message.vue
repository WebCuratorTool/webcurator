<script setup lang="ts">
import { computed, useAttrs } from "vue";

const props = defineProps<{ severity?: string }>();
const attrs = useAttrs();

const color = computed(() => {
  if (props.severity === "error" || props.severity === "danger") {
    return "error";
  }
  if (props.severity === "warn" || props.severity === "warning") {
    return "warning";
  }
  if (props.severity === "success") {
    return "success";
  }
  return "info";
});
</script>

<template>
  <UAlert v-bind="attrs" :color="color" variant="soft">
    <template #description>
      <slot />
    </template>
  </UAlert>
</template>
