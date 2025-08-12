<script setup lang="ts">
// librarys
import { ref } from 'vue';
import { useRoute } from 'vue-router';

// components
import TargetTabAnnotationsMessage from './TargetTabAnnotationsMessage.vue';
import WctTabViewPanel from '@/components/WctTabViewPanel.vue';
// stores
import { useTargetAnnotationsDTO } from '@/stores/target';
import { useTargetInstanceListStore } from '@/stores/targetInstanceList';
import { useUserProfileStore } from '@/stores/users';
// types
import type { Annotation } from '@/types/annotation';
// utils
import { formatDate } from '@/utils/helper';

const route = useRoute();
const targetId = Number(route.params.id);

defineProps<{
  editing: boolean;
}>();

const annotations = ref<Array<Annotation>>([]);
const targetAnnotations = useTargetAnnotationsDTO().targetAnnotations;
const selectionTypes = ["Area", "Collection", "Other collections", "Producer type", "Publication type"];
const harvestTypes = ["Event", "Subject", "Theme"];
const userProfile = useUserProfileStore();
const newAnnotation = ref(<Annotation>{ alert: false, user: userProfile.name });
const loading = ref(true);

async function prepareAnnotations() {
  const targetInstanceAnnotations = await useTargetInstanceListStore().getTargetInstanceAnnotations(targetId);

  targetInstanceAnnotations.forEach((annotation: Annotation) => {
    annotations.value.push(annotation);
  });

  targetAnnotations.annotations.forEach((annotation: Annotation) => {
    annotations.value.push(annotation);
  })

  annotations.value.sort((a, b) => new Date(a.date).valueOf() - new Date(b.date).valueOf());

  loading.value = false;
}

const addAnnotation = () => {
  newAnnotation.value.date = new Date().toISOString();
  targetAnnotations.annotations.push(newAnnotation.value);
  annotations.value.push(newAnnotation.value);
  newAnnotation.value = <Annotation>{ alert: false, user: userProfile.name };
}

const deleteAnnotation = (annotation: Annotation) => {
  targetAnnotations.annotations = targetAnnotations.annotations.filter((a) => 
    !(a.date === annotation.date && a.user === annotation.user && a.note === annotation.note)
  );
  annotations.value = annotations.value.filter((a) =>
    !(a.date === annotation.date && a.user === annotation.user && a.note === annotation.note)
  );
}

prepareAnnotations();

</script>

<template>
  <Loading v-if="loading" />
  <div v-else>
    <h4 class="mt-4">Selection</h4>
    <WctTabViewPanel>
      <div class="grid grid-cols-5 p-1">
        <p>Selection date</p>
        <p class="font-semibold">{{ formatDate(targetAnnotations.selection.date) }}</p>
      </div>
      <div class="grid grid-cols-5 p-1">
        <p>Selection type</p>
        <Select v-if="editing" v-model="targetAnnotations.selection.type" :options="selectionTypes" />
        <p v-else class="font-semibold">{{ targetAnnotations.selection.type }}</p>
      </div>
      <div class="grid grid-cols-5 p-1">
        <p>Selection note</p>
        <InputText v-if="editing" v-model="targetAnnotations.selection.note" :disabled="!editing" />
        <p v-else class="font-semibold">{{ targetAnnotations.selection.note }}</p>
      </div>
      <div class="grid grid-cols-5 p-1">
        <p>Evaluation Note</p>
        <InputText v-if="editing" v-model="targetAnnotations.evaluationNote" :disabled="!editing" />
        <p v-else class="font-semibold">{{ targetAnnotations.evaluationNote }}</p>
      </div>
      <div class="grid grid-cols-5 p-1">
        <p>Harvest Type</p>
        <Select v-if="editing" v-model="targetAnnotations.harvestType" :options="harvestTypes" />
        <p v-else class="font-semibold">{{ targetAnnotations.harvestType }}</p>
      </div>
    </WctTabViewPanel>

    <h4 class="mt-4">Annotations</h4>
    <WctTabViewPanel>
      <div v-if="editing" class="flex">
        <FloatLabel variant="on" class="w-1/2  mr-4">
          <Textarea v-model="newAnnotation.note" class="w-full" rows="3"/>
          <label for="on_label">New target annotation</label>
        </FloatLabel>
        <div>
          <div class="flex flex-col items-start justify-between gap-4">
            <div class="flex items-center gap-2">
              <Checkbox v-model="newAnnotation.alert" binary />
              <label>Generate alert</label>
            </div>
            <Button class="wct-primary-button" label="Add" @click="addAnnotation" />
          </div>
        </div>
      </div>

      <div v-if="annotations.length > 0" class="mt-8">
        <div class="flex justify-between items-center mb-8">
          <h4>Target</h4>
          <h4 class="!mt-0">Target Instances</h4>
        </div>
        <Timeline :value="annotations">
          <template #content="slotProps">
            <!-- Target instance annotations -->
            <TargetTabAnnotationsMessage 
              v-if="slotProps.item.targetInstanceId" 
              :annotation="slotProps.item"
              :editing="editing" 
              @deleteAnnotation="deleteAnnotation"
            />
          </template>
          <template #opposite="slotProps">
            <!-- Target annotations -->
            <TargetTabAnnotationsMessage 
              v-if="!slotProps.item.targetInstanceId" 
              :annotation="slotProps.item"
              :editing="editing"
              @deleteAnnotation="deleteAnnotation"
            />
          </template>
        </Timeline>
      </div>
      <div v-else-if="!editing" class="text-center">
        <p class="text-500">No annotations to display</p>
      </div>
    </WctTabViewPanel>
  </div>
</template>

<style>

</style>