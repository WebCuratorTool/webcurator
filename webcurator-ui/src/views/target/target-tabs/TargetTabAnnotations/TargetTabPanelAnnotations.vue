<script setup lang="ts">
import WctTabViewPanel from '@/components/WctTabViewPanel.vue';
import TargetTabAnnotationsMessage from './TargetTabAnnotationsMessage.vue';

import { useTargetAnnotationsDTO, useTargetGeneralDTO } from '@/stores/target';

defineProps<{
  editing: boolean;
}>();

const targetAnnotations = useTargetAnnotationsDTO();

</script>

<template>
  <h4 class="mt-4">Annotations</h4>
  <WctTabViewPanel>
    <div class="flex justify-between">
      <h4>Target</h4>
      <h4>Target Instances</h4>
    </div>
    <Timeline :value="targetAnnotations.targetAnnotations.annotations">
      <template #content="slotProps">
        <TargetTabAnnotationsMessage v-if="slotProps.item.note == 'ghbb'" :item="slotProps.item" :editing="editing" />
      </template>
      <template #opposite="slotProps">
        <TargetTabAnnotationsMessage v-if="slotProps.item.note != 'ghbb'" :item="slotProps.item" :editing="editing" />
      </template>
    </Timeline>
  </WctTabViewPanel>
</template>

<style>

</style>