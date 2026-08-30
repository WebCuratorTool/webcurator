<script setup lang="ts">
defineProps<{ severity?: string }>();
</script>

<template>
  <div class="wct-message" :class="severity ? `wct-message--${severity}` : ''">
    <slot />
  </div>
</template>

<style scoped>
.wct-message {
  border-radius: 0.5rem;
  padding: 0.75rem;
  border: 1px solid #93c5fd;
  background: #eff6ff;
  color: #1e3a8a;
}

.wct-message--error,
.wct-message--danger {
  border-color: #fca5a5;
  background: #fef2f2;
  color: #991b1b;
}

.wct-message--warn,
.wct-message--warning {
  border-color: #fcd34d;
  background: #fffbeb;
  color: #92400e;
}
</style>
