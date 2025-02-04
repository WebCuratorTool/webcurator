<script setup lang="ts">
import { defineAsyncComponent, ref, toRaw, watch } from 'vue';
import { useDialog } from 'primevue/usedialog';
import { useToast } from "primevue/usetoast";
import { formatDate } from '@/utils/helper';

import { useTargetSeedsDTO } from '@/stores/target';

import WctTabViewPanel from '@/components/WctTabViewPanel.vue';

const AddHarvestAuthModal = defineAsyncComponent(() => import('./modals/TargetAddHarvestAuthModal.vue'));

const toast = useToast();

const props = defineProps<{
  editing: boolean
}>()

const targetSeeds = useTargetSeedsDTO();

const addingSeeds = ref(false);
const editingSeed = ref(0);
const newSeed = ref({ seed: '', authorisations: [], primary: false });
const previousSeed = ref({});
const selectedAuthorisationOption = ref('Auto');

const addSeedsModal = useDialog();

const addSeed = () => {
  if (newSeed.value.seed != '') {
    if (targetSeeds.targetSeeds.some(t => t.seed == newSeed.value.seed)) {
      showErrorMessage();
    } else {
      targetSeeds.addSeed(newSeed.value);
    }
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

const removeHarvestAuth = (seed: any, auth: any) => {
  seed.authorisations = seed.authorisations.filter((a: any) => a.permissionId !== auth.permissionId);
}

const showAddHarvestAuth = (seed: any) => {
  addSeedsModal.open(AddHarvestAuthModal, {
    props: { header: `Add Harvest Auth to ${seed.seed}`, modal: true, dismissableMask: true, style: { width: '50vw' } },
    data: { seed: seed }
  })
}

const showErrorMessage = () => {
  toast.add({ severity: 'error', summary: 'Seed not added', detail: 'The seed already exists on the target', life: 3000 });
};

watch(() => props.editing, async(newEditing) => {
  if (newEditing == false) {
    editingSeed.value = 0;
  }
})
</script>

<template>
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
        <Dropdown v-model="selectedAuthorisationOption" :options="['Auto', 'Add Later']"/>
      </div>
      <div class="col flex align-items-end">
        <Button class="w-auto" icon="pi pi-plus" label="Add"  text @click="addSeed" />
      </div>
    </div>

    <table class="w-full target-seed-parent-table">
      <thead>
        <tr style="border-bottom: 1px solid #E4E4E4;">
          <th style="width: 20%; text-align: left; padding: .5rem;">Seed</th>
          <th style="width: 10%; text-align: left; padding: .5rem;">Primary</th>
          <th style="width: 15%; text-align: left; padding: .5rem;">Harvest Auth</th>
          <th style="width: 15%; text-align: left; padding: .5rem;">Auth Agent</th>
          <th style="width: 17.5%; text-align: left; padding: .5rem;">Start Date</th>
          <th style="width: 17.5%; text-align: left; padding: .5rem;">End Date</th>
          <th style="width: 10%; text-align: left; padding: .5rem;">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="data in targetSeeds.targetSeeds" :key="data.id">
          <td style="width: 20%; padding: .5rem;">
            <span v-if="editingSeed != data.id">{{ data.seed }}</span>
            <InputText v-else v-model="data.seed" />
          </td>
          <td style="width: 10%; padding: .5rem;">
            <span v-if="editingSeed != data.id">{{ data.primary ? 'Yes' : 'No' }}</span>
            <Checkbox v-else v-model="data.primary" :binary="true" />
          </td>
          <td colspan="5" style="padding: .5rem;">
            <table class="w-full target-seed-child-table">
              <tbody>
                <tr v-for="authorisation in data.authorisations" :key="authorisation.id">
                  <td style="width: 15%; padding: .5rem;">{{ authorisation.name }}</td>
                  <td style="width: 15%; padding: .5rem;">{{ authorisation.agent }}</td>
                  <td style="width: 17.5%; padding: .5rem;">{{ authorisation.startDate && formatDate(authorisation.startDate) }}</td>
                  <td style="width: 17.5%; padding: .5rem;">{{ authorisation.endDate && formatDate(authorisation.endDate) }}</td>
                  <td style="width: 5%; padding: .5rem;">
                    <div class="flex">
                      <Button class="p-button-text" style="width: 2rem;" icon="pi pi-eye" v-tooltip.bottom="'View Permission'" text />
                      <Button v-if="editing" class="p-button-text" style="width: 2rem;" icon="pi pi-link" v-tooltip.bottom="'Unlink Permission'" text @click="removeHarvestAuth(data, authorisation)" />
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </td>
          <td v-if="editing" style="width: 5%; padding: .5rem;">
            <div v-if="editing && editingSeed != data.id" class="flex">
              <Button class="p-button-text" style="width: 2rem;" icon="pi pi-plus-circle" v-tooltip.bottom="'Add Harvest Auth'" text @click="showAddHarvestAuth(data)" />
              <Button class="p-button-text" style="width: 2rem;" icon="pi pi-pencil" v-tooltip.bottom="'Edit Seed'" text @click="editSeed(data)" />
              <Button class="p-button-text" style="width: 2rem;" icon="pi pi-trash" v-tooltip.bottom="'Remove Seed'" text @click="targetSeeds.removeSeed(data.id)" />
            </div>
            <div v-else-if="editing && editingSeed == data.id" class="flex">
              <Button class="p-button-text" style="width: 2rem;" icon="pi pi-save" v-tooltip.bottom="'Save'" text @click="editingSeed = 0" />
              <Button class="p-button-text" style="width: 2rem;" icon="pi pi-times" v-tooltip.bottom="'Cancel'" text @click="cancelEditSeed()" />
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </WctTabViewPanel>
</template>

<style>
table { 
  border-collapse: collapse; 
}

.target-seed-parent-table tr:not(:last-child) {
  border-bottom: 1px solid #E4E4E4 !important;
}

.target-seed-parent-table table {
  border: none;
  border-collapse: separate;
}

.target-seed-parent-table tbody tr:hover {
  background-color: #F5F5F5;
}

</style>