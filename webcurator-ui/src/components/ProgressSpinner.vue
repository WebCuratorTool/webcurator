<script setup lang="ts"></script>

<template>
  <UIcon name="i-lucide-loader-circle" class="wct-spinner" aria-label="loading" />
</template>

<style scoped>
.wct-spinner {
  display: inline-flex;
  width: 2rem;
  height: 2rem;
  color: #2563eb;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
