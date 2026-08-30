<script setup lang="ts">
defineProps<{ value?: any[] }>();
</script>

<template>
  <UTimeline :items="value || []" class="wct-timeline">
    <template #title="{ item }">
      <slot name="opposite" :item="item" />
    </template>
    <template #description="{ item }">
      <slot name="content" :item="item" />
    </template>
  </UTimeline>
</template>
