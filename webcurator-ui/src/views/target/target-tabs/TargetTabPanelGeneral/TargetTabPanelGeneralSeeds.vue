<script setup lang="ts">
import { defineAsyncComponent, ref, toRaw, watch } from 'vue';
import { useDialog } from 'primevue/usedialog';
import { useToast } from "primevue/usetoast";
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

const showAddHarvestAuth = (seed: any) => {
  const modalRef = addSeedsModal.open(AddHarvestAuthModal, {
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
              <div v-for="authorisation in data.authorisations" :key="authorisation.id">
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
      <Column header="Actions" v-if="editing" style="max-width: 8rem">
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
</template>