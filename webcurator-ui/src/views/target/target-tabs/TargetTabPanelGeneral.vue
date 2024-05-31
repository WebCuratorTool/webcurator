<script setup lang="ts">
import { useUsersStore, getPresentationUserName } from '@/stores/users'
import { useTargetGeneralDTO, useTargetGropusDTO, useTargetSeedsDTO, formatTargetState, useNextStateStore } from '@/stores/target'

import Loading from '@/components/Loading.vue'
import WctFormField from '@/components/WctFormField.vue'
import WctTabViewPanel from '@/components/WctTabViewPanel.vue'

defineProps<{
    editing: boolean,
    loading: boolean
}>()

const targetGeneral = useTargetGeneralDTO();
const targetGroups = useTargetGropusDTO();
const targetSeeds = useTargetSeedsDTO();

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
        <Dropdown v-if="editing" id="user" v-model="targetGeneral.selectedUser" :options="users.userList" placeholder="Select an User" checkmark
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
        <p v-else class="font-semibold">{{ formatTargetState(targetGeneral.selectedState)}}</p>
      </WctFormField>
    </div>
  </WctTabViewPanel>

  <!-- Groups -->
  <h4>Groups</h4>
  <WctTabViewPanel>
    <div class="flex flex-wrap gap-2">
      <Chip v-for="group in targetGroups.targetGroups" :label="group.name" :removable="editing"/>
    </div>
  </WctTabViewPanel>

  <!-- Seeds -->
  <h4>Seeds</h4>
  <WctTabViewPanel>
    <DataTable class="w-full" :rowHover="true" :value="targetSeeds.targetSeeds">
      <Column field="seed" header="Seed"></Column>
      <Column field="authorisations" header="Harvset Auth">
        <template #body="{ data }">{{ data.authorisations[0] }}</template>
      </Column>
      <Column field="primary" header="Primary">
        <template #body="{ data }">
          <Checkbox
              v-model="data.primary" 
              :binary="true"
              :disabled="!editing" 
          />
        </template>
      </Column>
    </DataTable>
  </WctTabViewPanel>

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
