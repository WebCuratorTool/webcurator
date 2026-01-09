<script setup lang="ts">
import { useTargetAccessDTO } from '@/stores/target';
import { ref } from 'vue';

import WctFormField from '@/components/WctFormField.vue';
import WctTabViewPanel from '@/components/WctTabViewPanel.vue';
import { Select } from 'primevue';

const data = useTargetAccessDTO();
const targetAccess = data.targetAccess;

defineProps<{
  editing: boolean;
}>();

const accessZones = ref(['Public', 'Onsite', 'Restricted']);
</script>

<template>
  <WctTabViewPanel>
    <div class="flex items-start justify-between gap-4">
      <div class="w-full">
        <WctFormField label="Display Target" inputId="display-target">
          <Checkbox v-if="editing" v-model="targetAccess.displayTarget" :binary="true" :disabled="!editing" inputId="display-target" />
          <p v-else class="font-semibold">{{ targetAccess.displayTarget ? 'Yes' : 'No' }}</p>
        </WctFormField>
        <WctFormField label="Access Zone">
          <Select v-if="editing" v-model="targetAccess.accessZoneText" :options="accessZones" />
          <p v-else class="font-semibold">{{ targetAccess.accessZoneText }}</p>
        </WctFormField>
        <WctFormField label="Target Introductory Display Note">
          <Textarea v-if="editing" v-model="targetAccess.displayNote" :disabled="!editing" />
          <p v-else class="font-semibold">{{ targetAccess.displayNote }}</p>
        </WctFormField>
      </div>
      <div class="w-full">
        <WctFormField label="Reason for Display Change">
          <Textarea v-if="editing" v-model="targetAccess.displayChangeReason" :disabled="!editing" />
          <p v-else class="font-semibold">{{ targetAccess.displayChangeReason }}</p>
        </WctFormField>
      </div>
    </div>
  </WctTabViewPanel>
</template>

<style></style>
