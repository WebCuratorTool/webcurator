<script setup lang="ts">
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import WctTabViewPanel from '@/components/WctTabViewPanel.vue';
import TargetTabAnnotationsMessage from './TargetTabAnnotationsMessage.vue';

import { useTargetAnnotationsDTO, useTargetGeneralDTO } from '@/stores/target';
import { useTargetInstanceListStore } from '@/stores/targetInstanceList';


const route = useRoute();
const targetId = route.params.id as string;

defineProps<{
  editing: boolean;
}>();

const annotations = ref([]);
const targetAnnotations = useTargetAnnotationsDTO();
const loading = ref(true);
// targetInstanceAnnotations.value = useTargetInstanceListStore().getTargetInstanceAnnotations(targetId);

// if (!useTargetInstanceListStore().loadingTargetInstanceList && targetInstanceAnnotations) {
//   targetInstanceAnnotations.forEach((annotation: any) => {
//     console.log('s',annotation);
//   })
// }

async function fetchTIAnnotations() {
  const targetInstances = await useTargetInstanceListStore().getTargetInstanceAnnotations(targetId);
  console.log(targetInstances);
  
  targetInstances.forEach((targetInstance: any) => {
    targetInstance.annotations.forEach((annotation: any) => {
      annotation.targetInstanceId = targetInstance.id;
      annotations.value.push(annotation);
    })
  })

  targetAnnotations.targetAnnotations.annotations.forEach((annotation: any) => {
    annotations.value.push(annotation);
  })

  annotations.value.sort((a, b) => new Date(a.date) - new Date(b.date));

  loading.value = false;
}

fetchTIAnnotations();	

</script>

<template>
  <h4 class="mt-4">Annotations</h4>
  <WctTabViewPanel>
    <Loading v-if="loading" />
    <div v-else>    
      <div class="flex justify-between">
        <h4>Target</h4>
        <h4>Target Instances</h4>
      </div>
      <Timeline :value="annotations">
        <template #content="slotProps">
          <TargetTabAnnotationsMessage v-if="slotProps.item.targetInstanceId" :item="slotProps.item" :editing="editing" />
        </template>
        <template #opposite="slotProps">
          <TargetTabAnnotationsMessage v-if="!slotProps.item.targetInstanceId" :item="slotProps.item" :editing="editing" />
        </template>
      </Timeline>
    </div>
  </WctTabViewPanel>
</template>

<style>

</style>