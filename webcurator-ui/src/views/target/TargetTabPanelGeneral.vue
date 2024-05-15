<script setup lang="ts">
import { ref } from 'vue'
import { useUsersStore, getPresentationUserName } from '@/stores/users'
import WctFormField from '@/components/WctFormField.vue'
import WctTabViewPanel from '@/components/WctTabViewPanel.vue'
import { useTargetGeneralDTO, formatTargetState, useNextStateStore } from '@/stores/target'

const options = defineProps(['readOnly'])
const readOnly = ref(options.readOnly)
const fields = useTargetGeneralDTO()
const users = useUsersStore()
const nextStates = useNextStateStore()

</script>

<template>
  <WctTabViewPanel>
    <WctFormField label="Id">
      <InputText v-model="fields.id" :disabled="true" />
    </WctFormField>

    <WctFormField label="Name(*)">
      <InputText v-model="fields.name" :disabled="readOnly" />
    </WctFormField>

    <WctFormField label="Description">
      <Textarea v-model="fields.description" autoResize rows="6" :disabled="readOnly" />
    </WctFormField>

    <WctFormField label="Reference Number">
      <InputText v-model="fields.referenceNumber" :disabled="readOnly" />
    </WctFormField>

    <WctFormField label="Run on Approval">
      <Checkbox id="checkOption1" name="option1" value="Run on Approval" v-model="fields.runOnApproval" :binary="true"
        :disabled="readOnly" />
    </WctFormField>

    <WctFormField label="Use Automated QA">
      <Checkbox id="checkOption2" name="option2" value="Use Automated QA" v-model="fields.automatedQA" :binary="true"
        :disabled="readOnly" />
    </WctFormField>

    <WctFormField label="Owner">
      <Dropdown id="user" v-model="fields.selectedUser" :options="users.userList" placeholder="Select an User" checkmark
        class="w-full md:w-18rem" :disabled="readOnly">
        <template #value="slotProps">
          <div class="flex align-items-center">
            <div>{{ getPresentationUserName(fields.selectedUser) }}</div>
          </div>
        </template>
        <template #option="slotProps">
          <div class="flex align-items-center">
            <div>{{ slotProps.option.name }}</div>
          </div>
        </template>
      </Dropdown>
    </WctFormField>

    <WctFormField label="State">
      <Dropdown id="state" v-model="fields.selectedState" :options="nextStates.nextStateList" optionLabel="label"
        optionGroupLabel="label" optionGroupChildren="items" checkmark class="w-full md:w-18rem" :disabled="readOnly">
        <template #value="slotProps">
          <div class="flex align-items-center">
            <div>{{ formatTargetState(fields.selectedState) }}</div>
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

    <WctFormField label="Auto-prune">
      <Checkbox id="checkOption3" name="option3" value="Auto-prune:" v-model="fields.autoPrune" :binary="true"
        :disabled="readOnly" />
    </WctFormField>

    <WctFormField label="Reference Crawl">
      <Checkbox id="checkOption4" name="option4" value="Reference Crawl" v-model="fields.referenceCrawl" :binary="true"
        :disabled="readOnly" />
    </WctFormField>

    <WctFormField label="Request to Archivists">
      <Textarea v-model="fields.requestToArchivists" autoResize rows="6" :disabled="readOnly" />
    </WctFormField>
  </WctTabViewPanel>
</template>

<style>
.grid {
  align-items: center;
}
</style>
