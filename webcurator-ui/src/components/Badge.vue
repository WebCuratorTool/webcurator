<script setup lang="ts">
defineProps<{ value?: string | number }>();
</script>

<template>
  <span class="wct-badge"><slot>{{ value }}</slot></span>
</template>

<style scoped>
.wct-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 1.25rem;
  height: 1.25rem;
  border-radius: 9999px;
  background: #e2e8f0;
  color: #0f172a;
  font-size: 0.75rem;
  padding: 0 0.35rem;
}
</style>
