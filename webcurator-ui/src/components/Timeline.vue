<script setup lang="ts">
defineProps<{ value?: any[] }>();
</script>

<template>
  <div class="wct-timeline">
    <div v-for="(item, index) in value || []" :key="index" class="wct-timeline__item">
      <div class="wct-timeline__left">
        <slot name="opposite" :item="item" />
      </div>
      <div class="wct-timeline__dot" />
      <div class="wct-timeline__right">
        <slot name="content" :item="item" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.wct-timeline__item {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 1rem;
  align-items: start;
  margin-bottom: 1rem;
}

.wct-timeline__dot {
  width: 0.65rem;
  height: 0.65rem;
  border-radius: 9999px;
  background: #64748b;
  margin-top: 0.45rem;
}
</style>
