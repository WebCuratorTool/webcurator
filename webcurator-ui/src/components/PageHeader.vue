<script setup lang="ts">
defineProps<{
  title: string;
}>();
</script>

<template>
  <header class="page-header">
    <h1>{{ title }}</h1>
  </header>
</template>

<style scoped>
.page-header h1 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
}
</style>
