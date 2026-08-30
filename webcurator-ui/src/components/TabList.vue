<script setup lang="ts"></script>

<template>
  <div class="wct-tab-list">
    <slot />
  </div>
</template>

<style scoped>
.wct-tab-list {
  display: flex;
  gap: 0.5rem;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 0.75rem;
}
</style>
