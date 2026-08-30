<script setup lang="ts">
import { inject, type Ref, ref } from "vue";

import type { Annotation } from "@/types/annotation";
import type { DynamicDialogInstance } from "@/types/ui";

const dialogRef = inject<Ref<DynamicDialogInstance>>("dialogRef");

type NewAnnotationDialogPayload = {
  annotation: Annotation;
  addAnnotation: (annotation: Annotation) => void;
};

const payload = dialogRef?.value?.data as NewAnnotationDialogPayload | undefined;
if (!payload) {
  dialogRef?.value.close();
}

const { annotation, addAnnotation } = payload as NewAnnotationDialogPayload;

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
