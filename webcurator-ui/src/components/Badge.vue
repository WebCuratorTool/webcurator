<script setup lang="ts">
import { useAttrs } from "vue";

defineProps<{ value?: string | number }>();
const attrs = useAttrs();
</script>

<template>
  <UBadge v-bind="attrs">
    <slot>{{ value }}</slot>
  </UBadge>
</template>
