<script setup lang="ts">
// libraries
import { defineAsyncComponent, ref, Text } from 'vue';
import { useDialog } from 'primevue/usedialog'; 
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
import { Textarea } from 'primevue';

const NewAnnotationModal = defineAsyncComponent(() => import('./modals/TargetNewAnnotationModal.vue'));
const newAnnotationModal = useDialog();

const route = useRoute();
const targetId = Number(route.params.id);

const showNewAnnotationModal = () => {
  newAnnotationModal.open( NewAnnotationModal, {
    props: { header: 'New Annotation', modal: true, dismissableMask: true },
    data: {
      annotation: newAnnotation,
      addAnnotation: addAnnotation
    },
    onClose: () => {
      newAnnotation.value = <Annotation>{ alert: false, user: userProfile.name };
    }
  });
}

defineProps<{
  editing: boolean;
}>();

const annotations = ref<Array<Annotation>>([]);
const sortDirection = ref('desc');
const targetAnnotations = useTargetAnnotationsDTO().targetAnnotations;
const selectionTypes = ["Area", "Collection", "Other collections", "Producer type", "Publication type"];
const harvestTypes = ["Event", "Subject", "Theme"];
const userProfile = useUserProfileStore();
const newAnnotation = ref(<Annotation>{ alert: false, user: userProfile.name });
const loading = ref(false);

async function prepareAnnotations() {
  loading.value = true;
  
  const targetInstanceAnnotations = await useTargetInstanceListStore().getTargetInstanceAnnotations(targetId);

  targetInstanceAnnotations.forEach((annotation: Annotation) => {
    annotations.value.push(annotation);
  });

  targetAnnotations.annotations.forEach((annotation: Annotation) => {
    annotations.value.push(annotation);
  });

  annotations.value.sort((a, b) => new Date(a.date).valueOf() - new Date(b.date).valueOf());

  loading.value = false;
}

const sortAnnotaions = () => {
  if (sortDirection.value === 'asc') {
    annotations.value.sort((a, b) => new Date(a.date).valueOf() - new Date(b.date).valueOf());
    sortDirection.value = 'desc';
  } else {
    annotations.value.sort((a, b) => new Date(b.date).valueOf() - new Date(a.date).valueOf());
    sortDirection.value = 'asc';
  }
}

const addAnnotation = () => {
  newAnnotation.value.date = new Date().toISOString();
  targetAnnotations.annotations.push(newAnnotation.value);
  if (sortDirection.value === 'asc') {
    annotations.value.unshift(newAnnotation.value);
  } else {    
    annotations.value.push(newAnnotation.value);
  }
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

if (targetId) {
  prepareAnnotations();
} 

</script>

<template>
  <Loading v-if="loading" />
  <div v-else>
    <h4 class="mt-4">Selection</h4>
    <WctTabViewPanel columns>
      <div class="flex items-start justify-between gap-8 w-full">
        <div class="flex flex-col items-start gap-2 w-full">
          <WctFormField label="Selection date">
            <p class="font-semibold">{{ targetAnnotations.selection.date && formatDate(targetAnnotations.selection.date) }}</p>
          </WctFormField>
          <WctFormField label="Selection type">
            <Select v-if="editing" v-model="targetAnnotations.selection.type" :options="selectionTypes" showClear  />
            <p v-else class="font-semibold">{{ targetAnnotations.selection.type }}</p>
          </WctFormField>
          <WctFormField label="Selection note">
            <Textarea v-if="editing" v-model="targetAnnotations.selection.note" :disabled="!editing"  />
            <p v-else class="font-semibold">{{ targetAnnotations.selection.note }}</p>
          </WctFormField>
        </div>
        <div class="flex flex-col items-start gap-2 w-full">
          <WctFormField label="Evaluation note">
            <Textarea v-if="editing" v-model="targetAnnotations.evaluationNote" :disabled="!editing" />
            <p v-else class="font-semibold">{{ targetAnnotations.evaluationNote }}</p>
          </WctFormField>
          <WctFormField label="Harvest type">
            <Select v-if="editing" v-model="targetAnnotations.harvestType" :options="harvestTypes" showClear />
            <p v-else class="font-semibold">{{ targetAnnotations.harvestType }}</p>
          </WctFormField>
        </div>
      </div>
    </WctTabViewPanel>
    <div class="flex justify-between"> 
      <h4 class="mt-4">Annotations</h4>
      <Button v-if="editing" icon="pi pi-plus" label="Add" text @click="showNewAnnotationModal()" />
    </div>
    <WctTabViewPanel>
      <div v-if="annotations.length > 0">
        <div class="flex justify-between items-center">
          <h4>Target</h4>
          <h4 class="!mt-0">Target Instances</h4>
        </div>
        <div class="flex justify-center">
          <Button 
            icon="pi pi-arrow-right-arrow-left"
            style="transform: rotate(90deg); display: inline-block;" 
            v-tooltip.top="'Change sort direction'" 
            text
            @click="sortAnnotaions()" 
          />
        </div>
        <Timeline :value="annotations">
          <!-- Target instance annotations -->
          <template #content="slotProps">
            <TargetTabAnnotationsMessage 
              v-if="slotProps.item.targetInstanceId" 
              :annotation="slotProps.item"
              :editing="editing" 
              @deleteAnnotation="deleteAnnotation"
            />
          </template>
          <!-- Target annotations -->
          <template #opposite="slotProps">
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