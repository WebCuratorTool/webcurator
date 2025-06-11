<template>
  <div class="card p-fluid wct-tabview-panel" style="width: calc(80vw - 3em)">
    <p v-if="label" class="text-xl text-900 font-bold">{{ label }}</p>
    <div :class="{ grid: columns }" id="grid-form">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label?: string;
  columns?: boolean;
}>();
</script>

<style lang="scss">
.wct-tabview-panel {
  border: 1px solid var(--surface-border);
}
</style>
