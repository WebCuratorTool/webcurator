<script setup lang="ts">
import { useUsersStore, getPresentationUserName } from '@/stores/users'
import { useTargetGeneralDTO, formatTargetState, useNextStateStore } from '@/stores/target'

import WctFormField from '@/components/WctFormField.vue'
import WctTabViewPanel from '@/components/WctTabViewPanel.vue'

defineProps<{
    editing: boolean
}>()

const targetGeneral = useTargetGeneralDTO()

const users = useUsersStore()
const nextStates = useNextStateStore()

</script>

<template>
  <WctTabViewPanel columns>
    <div class="col">
      <WctFormField label="Id">
        <InputText v-model="targetGeneral.id" :disabled="true" />
      </WctFormField>
  
      <WctFormField label="Name(*)">
        <InputText v-model="targetGeneral.name" :disabled="!editing" />
      </WctFormField>
  
      <WctFormField label="Description">
        <Textarea v-model="targetGeneral.description" autoResize rows="6" :disabled="!editing" />
      </WctFormField>
    </div>
    <div class="col vertical-align-top">
      <WctFormField label="Owner">
        <Dropdown id="user" v-model="targetGeneral.selectedUser" :options="users.userList" placeholder="Select an User" checkmark
          class="w-full md:w-18rem" :disabled="!editing">
          <template #value="slotProps">
            <div class="flex align-items-center">
              <div>{{ getPresentationUserName(targetGeneral.selectedUser) }}</div>
            </div>
          </template>
          <template #option="slotProps">
            <div class="flex align-items-center">
              <div>{{ slotProps.option.name }}</div>
            </div>
          </template>
        </Dropdown>
      </WctFormField>
   
      <WctFormField label="Reference Number">
        <InputText v-model="targetGeneral.referenceNumber" :disabled="!editing" />
      </WctFormField>

      <WctFormField label="Run on Approval">
        <Checkbox id="checkOption1" name="option1" value="Run on Approval" v-model="targetGeneral.runOnApproval" :binary="true"
          :disabled="!editing" />
      </WctFormField>

      <WctFormField label="State">
        <Dropdown id="state" v-model="targetGeneral.selectedState" :options="nextStates.nextStateList" optionLabel="label"
          optionGroupLabel="label" optionGroupChildren="items" checkmark class="w-full md:w-18rem" :disabled="!editing">
          <template #value="slotProps">
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
      </WctFormField>
    </div>
  </WctTabViewPanel>

  <WctTabViewPanel columns>
    <div class="col">
      <WctFormField label="Use Automated QA">
        <Checkbox id="checkOption2" name="option2" value="Use Automated QA" v-model="targetGeneral.automatedQA" :binary="true"
          :disabled="!editing" />
      </WctFormField>
      <WctFormField label="Auto-prune">
        <Checkbox id="checkOption3" name="option3" value="Auto-prune:" v-model="targetGeneral.autoPrune" :binary="true"
          :disabled="!editing" />
      </WctFormField>
      <WctFormField label="Reference Crawl">
        <Checkbox id="checkOption4" name="option4" value="Reference Crawl" v-model="targetGeneral.referenceCrawl" :binary="true"
          :disabled="!editing" />
      </WctFormField>
    </div>

    <div class="col">
      <WctFormField label="Request to Archivists">
        <Textarea v-model="targetGeneral.requestToArchivists" autoResize rows="6" :disabled="!editing" />
      </WctFormField>
    </div>
  </WctTabViewPanel>
</template>

<style>

</style>
