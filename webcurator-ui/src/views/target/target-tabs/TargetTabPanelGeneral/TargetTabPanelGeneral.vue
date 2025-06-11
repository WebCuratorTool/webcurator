<script setup lang="ts">
import { formatTargetState, useNextStateStore, useTargetGeneralDTO } from '@/stores/target';
import { useUsersStore } from '@/stores/users';

import WctFormField from '@/components/WctFormField.vue';
import WctTabViewPanel from '@/components/WctTabViewPanel.vue';
import TargetTabPanelGeneralGroups from './TargetTabPanelGeneralGroups.vue';
import TargetTabPanelGeneralSeeds from './TargetTabPanelGeneralSeeds.vue';

defineProps<{
  editing: boolean;
}>();

const targetGeneral = useTargetGeneralDTO();
const users = useUsersStore();
const nextStates = useNextStateStore();
</script>

<template>
  <!-- References -->
  <h4 class="mt-4">References</h4>
  <WctTabViewPanel columns>
    <div class="flex items-start justify-between gap-8 w-full">
      <div class="flex flex-col items-start gap-2 w-full">
        <WctFormField label="Id">
          <InputText v-if="editing" v-model="targetGeneral.id" :disabled="true" />
          <p v-else class="font-semibold">{{ targetGeneral.id }}</p>
        </WctFormField>

        <WctFormField label="Name(*)">
          <InputText v-if="editing" v-model="targetGeneral.name" :disabled="!editing" />
          <p v-else class="font-semibold">{{ targetGeneral.name }}</p>
        </WctFormField>

        <WctFormField label="Description">
          <Textarea v-if="editing" v-model="targetGeneral.description" autoResize rows="6" :disabled="!editing" />
          <p v-else class="font-semibold">{{ targetGeneral.description }}</p>
        </WctFormField>
      </div>
      <div class="flex flex-col items-start gap-2 w-full">
        <WctFormField label="Owner">
          <Select
            v-if="editing"
            id="user"
            v-model="targetGeneral.selectedUser"
            :options="users.userListWithEmptyItem"
            optionLabel="name"
            placeholder="Select a User"
            class="w-full md:w-18rem"
            :disabled="!editing"
          />
          <p v-else class="font-semibold">{{ targetGeneral.selectedUser.code }}</p>
        </WctFormField>

        <WctFormField label="Reference Number">
          <InputText v-if="editing" v-model="targetGeneral.referenceNumber" :disabled="!editing" />
          <p v-else class="font-semibold">{{ targetGeneral.referenceNumber }}</p>
        </WctFormField>

        <WctFormField label="Run on Approval" inputId="run-on-approval">
          <Checkbox v-if="editing" id="checkOption1" name="option1" value="Run on Approval" v-model="targetGeneral.runOnApproval" :binary="true" :disabled="!editing" inputId="run-on-approval" />
          <p v-else class="font-semibold">{{ targetGeneral.runOnApproval ? 'Yes' : 'No' }}</p>
        </WctFormField>

        <WctFormField label="State">
          <Select
            v-if="editing"
            id="state"
            v-model="targetGeneral.selectedState"
            :options="nextStates.nextStateList"
            optionLabel="name"
            optionGroupLabel="name"
            optionGroupChildren="items"
            class="w-full md:w-18rem"
            :disabled="!editing"
          />
          <p v-else class="font-semibold">{{ formatTargetState(targetGeneral.selectedState) }}</p>
        </WctFormField>
      </div>
    </div>
  </WctTabViewPanel>

  <!-- Groups -->
  <TargetTabPanelGeneralGroups :editing="editing" />

  <!-- Seeds -->
  <TargetTabPanelGeneralSeeds :editing="editing" />

  <!-- Archive options -->
  <h4>Archive options</h4>
  <WctTabViewPanel columns>
    <div class="flex items-start justify-between gap-8 w-full">
      <div class="flex flex-col items-start gap-2 w-full">
        <WctFormField checkbox label="Use Automated QA" inputId="automated-qa">
          <Checkbox v-if="editing" v-model="targetGeneral.automatedQA" :binary="true" :disabled="!editing" inputId="automated-qa" />
          <p v-else class="font-semibold">{{ targetGeneral.automatedQA ? 'Yes' : 'No' }}</p>
        </WctFormField>
        <WctFormField checkbox label="Auto-prune" inputId="auto-prune">
          <Checkbox v-if="editing" v-model="targetGeneral.autoPrune" :binary="true" :disabled="!editing" inputId="auto-prune" />
          <p v-else class="font-semibold">{{ targetGeneral.autoPrune ? 'Yes' : 'No' }}</p>
        </WctFormField>
        <WctFormField checkbox label="Reference Crawl" inputId="reference-crawl">
          <Checkbox v-if="editing" v-model="targetGeneral.referenceCrawl" :binary="true" :disabled="!editing" inputId="reference-crawl" />
          <p v-else class="font-semibold">{{ targetGeneral.referenceCrawl ? 'Yes' : 'No' }}</p>
        </WctFormField>
      </div>

      <div class="flex flex-col items-start gap-2 w-full">
        <WctFormField label="Request to Archivists">
          <Textarea v-if="editing" v-model="targetGeneral.requestToArchivists" autoResize rows="6" :disabled="!editing" />
          <p v-else class="font-semibold">{{ targetGeneral.requestToArchivists }}</p>
        </WctFormField>
      </div>
    </div>
  </WctTabViewPanel>
</template>

<style></style>
