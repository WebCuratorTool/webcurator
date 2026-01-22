<script setup lang="ts">
import type { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { inject, type Ref, ref } from "vue";

const dialogRef = inject<Ref<DynamicDialogInstance>>("dialogRef");

const payload = dialogRef?.value?.data;
if (!payload) {
  dialogRef?.value.close();
}

const { annotation, addAnnotation } = payload!;

const newAnnotation = ref(annotation);

const onSave = () => {
  addAnnotation(newAnnotation.value);
  dialogRef?.value.close();
};
</script>

<template>
  <Textarea v-model="newAnnotation.note" cols="80" rows="3" />
  <div class="flex items-start justify-between mt-4">
    <div class="flex items-center gap-2">
      <label>
        <Checkbox v-model="newAnnotation.alert" binary />
        Generate alert
      </label>
    </div>
    <Button class="wct-primary-button" label="Add" @click="onSave" />
  </div>
</template>
