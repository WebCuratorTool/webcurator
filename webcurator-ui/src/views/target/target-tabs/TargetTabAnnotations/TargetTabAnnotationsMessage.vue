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
    <template #subtitle>
      <div class="flex justify-between">
        <div>
          <div v-if="item.targetInstanceId">Target Instance {{ item.targetInstanceId }}</div>
          <div>{{ item.user }} {{ formatDate(item.date) }}</div>
        </div>
        <div v-if="editing">
          <Button class="p-button-text" style="width: 2rem;" icon="pi pi-trash" v-tooltip.bottom="'Delete Annotation'" text @click="" />
          <Button class="p-button-text" style="width: 2rem" icon="pi pi-pencil" v-tooltip.bottom="'Edit Annotation'" text @click="" />
        </div>  
      </div>
    </template>
    <template #content>
      <Textarea v-if="editing" v-model="item.note" autoResize rows="3" class="w-full" />
      <p v-else>{{ item.note }}</p>
    </template>
  </Card>
</template>
  
