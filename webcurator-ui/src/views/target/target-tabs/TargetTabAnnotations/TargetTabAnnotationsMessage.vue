<script setup lang="ts">
import { formatDate } from '@/utils/helper';

defineProps<{
  item: {
    user: string;
    date: number;
    note: string;
  },
  editing: boolean,
}>();

</script>

<template>
  <Card class="mt-3">
    <template #title>
      <div class="flex justify-between">
        <div>{{ item.user }}</div>
        <Button v-if="editing" class="p-button-text" style="width: 2rem" icon="pi pi-pencil" v-tooltip.bottom="'Edit Seed'" text @click="" />
      </div>
    </template>
    <template #subtitle>
      {{ formatDate(item.date) }}
    </template>
    <template #content>
      <p>{{ item.note }}</p>
    </template>
  </Card>
</template>
  
