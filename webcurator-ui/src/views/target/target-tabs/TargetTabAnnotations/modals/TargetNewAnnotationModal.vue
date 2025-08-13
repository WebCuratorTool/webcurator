<script setup lang="ts">
import { inject, ref } from 'vue';

const dialogRef: any = inject('dialogRef');

const { annotation, addAnnotation } = dialogRef.value.data
const newAnnotation = ref(annotation);

const onSave = () => {
  addAnnotation(newAnnotation.value);
  dialogRef.value.close();
}

</script>

<template>
  <Textarea v-model="newAnnotation.note" cols="80" rows="3"/>
  <div class="flex items-start justify-between mt-4">
    <div class="flex items-center gap-2">
      <Checkbox v-model="newAnnotation.alert" binary />
      <label>Generate alert</label>
    </div>
    <Button class="wct-primary-button" label="Add" @click="onSave" />
  </div>
</template>