<script setup lang="ts">
import { useUsersStore, getPresentationUserName } from '@/stores/users'
import { useTargetGeneralDTO, formatTargetState, useNextStateStore } from '@/stores/target'

import WctFormField from '@/components/WctFormField.vue';
import WctTabViewPanel from '@/components/WctTabViewPanel.vue';

import TargetTabPanelGeneralGroups from './TargetTabPanelGeneralGroups.vue';
import TargetTabPanelGeneralSeeds from './TargetTabPanelGeneralSeeds.vue';

defineProps<{
    editing: boolean
}>()

const targetGeneral = useTargetGeneralDTO();
const users = useUsersStore();
const nextStates = useNextStateStore();
</script>

<template>
  <!-- References -->
  <h4 class="mt-4">References</h4>
  <WctTabViewPanel columns>
    <div class="col">
      <WctFormField label="Id">
        <InputText v-if="editing" v-model="targetGeneral.id" :disabled="true" />
        <p v-else class="font-semibold">{{ targetGeneral.id }}</p>
      </WctFormField>
  
      <WctFormField label="Name(*)">
        <InputText v-if="editing" v-model="targetGeneral.name" :disabled="!editing" />
        <p v-else class="font-semibold">{{  targetGeneral.name }}</p>
      </WctFormField>
  
      <WctFormField label="Description">
        <Textarea v-if="editing" v-model="targetGeneral.description" autoResize rows="6" :disabled="!editing" />
        <p v-else class="font-semibold">{{ targetGeneral.description }}</p>
      </WctFormField>
    </div>
    <div class="col vertical-align-top">
      <WctFormField label="Owner">
        <Dropdown v-if="editing" id="user" v-model="targetGeneral.selectedUser" :options="users.userList" placeholder="Select a User" checkmark
          class="w-full md:w-18rem" :disabled="!editing">
          <template #value="slotProps">
            <div v-if="slotProps.value" class="flex align-items-center">
              <div>{{ getPresentationUserName(targetGeneral.selectedUser) }}</div>
            </div>
            <span v-else>
              {{ slotProps.placeholder }}
            </span>
          </template>
          <template #option="slotProps">
            <div class="flex align-items-center">
              <div>{{ slotProps.option.name }}</div>
            </div>
          </template>
        </Dropdown>
        <p v-else class="font-semibold">{{ targetGeneral.selectedUser.code }}</p>
      </WctFormField>
      
      <WctFormField label="Reference Number">
        <InputText v-if="editing" v-model="targetGeneral.referenceNumber" :disabled="!editing" />
        <p v-else class="font-semibold">{{ targetGeneral.referenceNumber }}</p>
      </WctFormField>

      <WctFormField label="Run on Approval">
        <Checkbox v-if="editing" id="checkOption1" name="option1" value="Run on Approval" v-model="targetGeneral.runOnApproval" :binary="true"
          :disabled="!editing" />
        <p v-else class="font-semibold">{{ targetGeneral.runOnApproval ? 'Yes' : 'No' }}</p>
      </WctFormField>

      <WctFormField label="State">
        <Dropdown v-if="editing" id="state" v-model="targetGeneral.selectedState" :options="nextStates.nextStateList" optionLabel="label"
          optionGroupLabel="label" optionGroupChildren="items" checkmark class="w-full md:w-18rem" :disabled="!editing">
          <template>
            <div class="flex align-items-center">
              <div>{{ formatTargetState(targetGeneral.selectedState) }}</div>
            </div>
          </template>
          <template #optiongroup="slotProps">
            <div class="flex align-items-center">
              <div>{{ slotProps.option.label }}</div>
            </div>
          </template>
          <template #option="slotProps">
            <div class="flex align-items-center">
              <div>{{ slotProps.option.name }}</div>
            </div>
          </template>
        </Dropdown>
        <p v-else class="font-semibold">{{ formatTargetState(targetGeneral.selectedState) }}</p>
      </WctFormField>
    </div>
  </WctTabViewPanel>

  <!-- Groups -->
  <TargetTabPanelGeneralGroups :editing="editing" />

  <!-- Seeds -->
  <TargetTabPanelGeneralSeeds :editing="editing"/>

  <!-- Archive options -->
  <h4>Archive options</h4>
  <WctTabViewPanel columns >
    <div class="col">
      <WctFormField checkbox label="Use Automated QA">
        <Checkbox v-if="editing" v-model="targetGeneral.automatedQA" :binary="true"
          :disabled="!editing" />
        <p v-else class="font-semibold">{{ targetGeneral.automatedQA ? 'Yes' : 'No' }}</p>
      </WctFormField>
      <WctFormField checkbox label="Auto-prune">
        <Checkbox v-if="editing" v-model="targetGeneral.autoPrune" :binary="true"
          :disabled="!editing" />
        <p v-else class="font-semibold">{{ targetGeneral.autoPrune ? 'Yes' : 'No' }}</p>
      </WctFormField>
      <WctFormField checkbox label="Reference Crawl">
        <Checkbox v-if="editing" v-model="targetGeneral.referenceCrawl" :binary="true"
          :disabled="!editing" />
        <p v-else class="font-semibold">{{ targetGeneral.referenceCrawl ? 'Yes' : 'No' }}</p>  
      </WctFormField>
    </div>

    <div class="col">
      <WctFormField label="Request to Archivists">
        <Textarea v-if="editing" v-model="targetGeneral.requestToArchivists" autoResize rows="6" :disabled="!editing" />
        <p v-else class="font-semibold">{{ targetGeneral.requestToArchivists }}</p>  
      </WctFormField>
    </div>
  </WctTabViewPanel>
</template>

<style>

</style>
