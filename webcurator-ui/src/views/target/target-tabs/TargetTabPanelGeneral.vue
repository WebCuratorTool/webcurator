<script setup lang="ts">
import { defineAsyncComponent, ref, toRaw } from 'vue';
import { useDialog } from 'primevue/usedialog';
import { useUsersStore, getPresentationUserName } from '@/stores/users'
import { useTargetGeneralDTO, useTargetGropusDTO, useTargetSeedsDTO, formatTargetState, useNextStateStore } from '@/stores/target'

import WctFormField from '@/components/WctFormField.vue'
import WctTabViewPanel from '@/components/WctTabViewPanel.vue'

const AddGroupsModal = defineAsyncComponent(() => import('./modals/TargetAddGroupsModal.vue'))
const AddHarvestAuthModal = defineAsyncComponent(() => import('./modals/TargetAddHarvestAuthModal.vue'))

defineProps<{
    editing: boolean
}>()

const targetGeneral = useTargetGeneralDTO();
const targetGroups = useTargetGropusDTO();
const targetSeeds = useTargetSeedsDTO();

const users = useUsersStore();
const nextStates = useNextStateStore();

const addGroupsModal = useDialog();
const addSeedsModal = useDialog();

const newSeed = ref({ seed: '', authorisations: [], primary: false });

const addingSeeds = ref(false);
const editingSeed = ref(0);
const previousSeed = ref({});

const showAddGroups = () => {
  const modalRef = addGroupsModal.open(AddGroupsModal, {
    props: { header: 'Add Groups', modal: true, dismissableMask: true, style: { width: '50vw' } }
  })
}

const showAddHarvestAuth = (seed: any) => {
  const modalRef = addSeedsModal.open(AddHarvestAuthModal, {
    props: { header: `Add Harvest Auth to ${seed.seed}`, modal: true, dismissableMask: true, style: { width: '50vw' } },
    data: { seed: seed }
  })
}

const addSeed = () => {
  if (newSeed.value.seed != '') {
    targetSeeds.addSeed(newSeed.value);
    newSeed.value = { seed: '', authorisations: [], primary: false };
  }
}

const editSeed = (seed: any) => {
  previousSeed.value = structuredClone(toRaw(seed));
  editingSeed.value = seed.id;
}

const cancelEditSeed = () => {
  targetSeeds.replaceSeed(previousSeed.value)
  editingSeed.value = 0;
}

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
        <p v-else class="font-semibold">{{ formatTargetState(targetGeneral.selectedState) }}</p>
      </WctFormField>
    </div>
  </WctTabViewPanel>

  <!-- Groups -->
  <div class="flex justify-content-between">
    <h4>Groups</h4>
    <Button v-if="editing" icon="pi pi-plus" label="Add" text @click="showAddGroups" />
  </div>
  <WctTabViewPanel>
    <div class="flex flex-wrap gap-2">
      <Chip class="px-2" v-for="group in targetGroups.targetGroups">
          <span class="p-2 m-0">{{ group.name }}</span>
          <Button v-if="editing" class="p-0 m-0" icon="pi pi-times-circle" style="width: 2rem;" link @click="targetGroups.removeGroup(group.id)"/>
      </Chip>
    </div>
  </WctTabViewPanel>

  <!-- Seeds -->
  <div class="flex justify-content-between">
    <h4>Seeds</h4>
    <Button v-if="editing && !addingSeeds" icon="pi pi-plus" label="Add" text @click="addingSeeds = true" />
    <Button v-if="editing && addingSeeds" icon="pi pi-times" text @click="addingSeeds = false" />
  </div>
  <WctTabViewPanel>
    <div v-if="editing && addingSeeds" class="mb-2 grid">
      <div class="col-7">
        <p>Add Seed</p>
        <InputText v-model="newSeed.seed" />
      </div>
      <div class="col-3">
        <p>Authorisation</p>
        <Dropdown :options="['Auto', 'Add Later']"/>
      </div>
      <div class="col flex align-items-end">
        <Button class="w-auto" icon="pi pi-plus" label="Add"  text @click="addSeed" />
      </div>
    </div>

    <DataTable class="w-full" :rowHover="true" :value="targetSeeds.targetSeeds">
      <Column field="seed" header="Seed">
        <template #body="{ data }">
          <div class="flex align-items-center justify-content-between">
            <span v-if="editingSeed != data.id">{{ data.seed }}</span>
            <InputText v-else v-model="data.seed" />
          </div>
        </template>
      </Column>
      <Column field="authorisations" header="Harvset Auth">
        <template #body="{ data }">
          <div class="flex align-items-center">
            <div>
              <div v-for="authorisation in data.authorisations">
                {{ authorisation }}
              </div>
            </div>
            <div class="flex-shrink-0">
              <Button v-if="editing && editingSeed == data.id" label="+/-" text @click="showAddHarvestAuth(data)" />
            </div>
          </div>
        </template>
      </Column>
      <Column field="primary" header="Primary">
        <template #body="{ data }">
          <Checkbox
              v-model="data.primary" 
              :binary="true"
              :disabled="!editing || editingSeed != data.id" 
          />
        </template>
      </Column>
      <Column v-if="editing" style="max-width: 8rem">
        <template #body="{ data }">
          <div v-if="editing && editingSeed != data.id">
            <Button icon="pi pi-pencil" text @click="editSeed(data)" />
            <Button icon="pi pi-trash" text @click="targetSeeds.removeSeed(data.id)" />
          </div>
          <div v-else-if="editing && editingSeed == data.id">
              <Button icon="pi pi-save" text @click="editingSeed = 0" />
              <Button icon="pi pi-times" text @click="cancelEditSeed()" />
            </div>
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
