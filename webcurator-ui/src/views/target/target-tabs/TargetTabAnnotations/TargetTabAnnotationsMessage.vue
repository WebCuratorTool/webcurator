<script setup lang="ts">
// libraries
import { defineAsyncComponent, ref } from 'vue';

// types
import type { Annotation } from '@/types/annotation';
// utils
import { formatDate } from '@/utils/helper';

const props = defineProps<{
  annotation: Annotation,
  editing: boolean,
}>();

const emit = defineEmits(['deleteAnnotation']);

const editingAnnotation = ref(false);
const previousAnnotationNote = ref();
const previousAnnotationAlert = ref();

const editAnnotation = () => {
  editingAnnotation.value = true;
  previousAnnotationNote.value = props.annotation.note;
  previousAnnotationAlert.value = props.annotation.alert;
} 

const cancelEditAnnotation = () => {
  props.annotation.note = previousAnnotationNote.value;
  props.annotation.alert = previousAnnotationAlert.value;
  editingAnnotation.value = false;
}

</script>

<template>
  <Card class="mt-3">
    <template #subtitle>
      <div class="flex justify-between items-center">
        <div class="flex justify-between w-full">
          <div class="flex gap-2 items-center">
            <div>{{ annotation.user }} {{ formatDate(annotation.date) }}</div>
            <i v-if="annotation.alert && !editingAnnotation" class="pi pi-exclamation-triangle" />
            <div v-if="editingAnnotation" class="flex items-center gap-2">
              <label>
                <Checkbox v-model="annotation.alert" binary />
                Generate alert
              </label>
            </div>
          </div>
          <div v-if="annotation.targetInstanceId">Target Instance {{ annotation.targetInstanceId }}</div>
        </div>
        <div v-if="editing && !annotation.targetInstanceId && !editingAnnotation" class="flex">
          <Button 
            style="width: 2rem;" 
            icon="pi pi-trash" 
            v-tooltip.bottom="'Delete Annotation'" 
            text 
            @click="$emit('deleteAnnotation', annotation)" 
          />
          <Button 
            style="width: 2rem" 
            icon="pi pi-pencil"
            v-tooltip.bottom="'Edit Annotation'" 
            text 
            @click="editAnnotation()" 
          />
        </div>
        <div v-else-if="editing && !annotation.targetInstanceId && editingAnnotation" class="flex">
          <Button class="p-button-text" style="width: 2rem" icon="pi pi-save" v-tooltip.bottom="'Save'" text @click="editingAnnotation = false" />
          <Button class="p-button-text" style="width: 2rem" icon="pi pi-times" v-tooltip.bottom="'Cancel'" text @click="cancelEditAnnotation()" />
        </div>  
      </div>
    </template>
    <template #content>
      <Textarea v-if="editing && editingAnnotation && !annotation.targetInstanceId" v-model="annotation.note" autoResize rows="3" class="w-full" />
      <p v-else>{{ annotation.note }}</p>
    </template>
  </Card>
</template>
  
