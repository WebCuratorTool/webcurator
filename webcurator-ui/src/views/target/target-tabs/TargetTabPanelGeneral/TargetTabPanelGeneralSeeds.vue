<script setup lang="ts">
// libraries
import { defineAsyncComponent, ref, toRaw, watch } from 'vue';
import { useDialog } from 'primevue/usedialog';
import { useRoute } from 'vue-router';
import { useToast } from 'primevue/usetoast';

// components
import WctTabViewPanel from '@/components/WctTabViewPanel.vue';
// stores
import { useTargetSeedsDTO } from '@/stores/target';
// utils
import { formatDate } from '@/utils/helper';

const AddPermissionModal = defineAsyncComponent(() => import('./modals/TargetAddPermissionModal.vue'));
const AddSeedModal = defineAsyncComponent(() => import('./modals/TargetAddSeedModal.vue'));
const ViewPermissionModal = defineAsyncComponent(() => import('./modals/TargetViewPermissionModal.vue'));

const addPermissionsModal = useDialog();
const addSeedModal = useDialog();
const viewPermissionModal = useDialog();

const toast = useToast();

const props = defineProps<{
  editing: boolean;
}>();

const route = useRoute()
const targetId = route.params.id as string

const targetSeeds = useTargetSeedsDTO();

const editingSeed = ref(0);
const previousSeed = ref({});

const editSeed = (seed: any) => {
  previousSeed.value = structuredClone(toRaw(seed));
  editingSeed.value = seed.id;
};

const cancelEditSeed = () => {
  targetSeeds.replaceSeed(previousSeed.value);
  editingSeed.value = 0;
};

const removePermission = (seed: any, auth: any) => {
  seed.authorisations = seed.authorisations.filter((a: any) => a.permissionId !== auth.permissionId);
};

const showAddPermission = (seed: any) => {
  addPermissionsModal.open(AddPermissionModal, {
    props: { header: `Add Permission to ${seed.seed}`, modal: true, dismissableMask: true, style: { width: '50vw' } },
    data: { seed: seed, targetId: targetId }
  });
};

const showAddSeed = () => {
  addSeedModal.open(AddSeedModal, {
    props: { header: 'Add Seed', modal: true, dismissableMask: true, style: { width: '20vw' } }
  });
};

const showViewPermission = (permissionId: number) => {
  viewPermissionModal.open(ViewPermissionModal, { 
    props: { modal: true, dismissableMask: true, closable: false, style: { width: '50vw' } },
    data: { permissionId: permissionId }
  })
}

// If the Target is switched out of editing mode, clear the editing seed value too
watch(() => props.editing, async(updatedEditingState) => {
  if (updatedEditingState == false) {
    editingSeed.value = 0;
  }
});
</script>

<template>
  <div class="flex justify-between">
    <h4>Seeds</h4>
    <Button v-if="editing" icon="pi pi-plus" label="Add" text @click="showAddSeed()" />
  </div>
  <WctTabViewPanel>
    <table v-if="targetSeeds.targetSeeds.length > 0" class="w-full target-seed-parent-table">
      <thead>
        <tr style="border-bottom: 1px solid #e4e4e4">
          <th style="width: 20%; text-align: left; padding: 0.5rem">Seed</th>
          <th style="width: 10%; text-align: left; padding: 0.5rem">Primary</th>
          <th style="width: 15%; text-align: left; padding: 0.5rem">Harvest Auth</th>
          <th style="width: 15%; text-align: left; padding: 0.5rem">Auth Agent</th>
          <th style="width: 17.5%; text-align: left; padding: 0.5rem">Start Date</th>
          <th style="width: 17.5%; text-align: left; padding: 0.5rem">End Date</th>
          <th style="width: 10%; text-align: left; padding: 0.5rem">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="data in targetSeeds.targetSeeds" :key="data.id">
          <td style="width: 20%; padding: 0.5rem">
            <span v-if="editingSeed != data.id">{{ data.seed }}</span>
            <InputText v-else v-model="data.seed" />
          </td>
          <td style="width: 10%; padding: 0.5rem">
            <span v-if="editingSeed != data.id">{{ data.primary ? 'Yes' : 'No' }}</span>
            <Checkbox v-else v-model="data.primary" :binary="true" />
          </td>
          <td colspan="5" style="padding: 0.5rem">
            <table class="w-full target-seed-child-table">
              <tbody>
                <tr v-for="authorisation in data.authorisations" :key="authorisation.id">
                  <td style="width: 15%; padding: 0.5rem">{{ authorisation.name }}</td>
                  <td style="width: 15%; padding: 0.5rem">{{ authorisation.agent }}</td>
                  <td style="width: 17.5%; padding: 0.5rem">{{ authorisation.startDate && formatDate(authorisation.startDate) }}</td>
                  <td style="width: 17.5%; padding: 0.5rem">{{ authorisation.endDate && formatDate(authorisation.endDate) }}</td>
                  <td style="width: 5%; padding: 0.5rem">
                    <div class="flex">
                      <Button class="p-button-text" style="width: 2rem;" icon="pi pi-eye" v-tooltip.bottom="'View Permission'" text @click="showViewPermission(authorisation.permissionId)"/>
                      <Button v-if="editing" class="p-button-text" style="width: 2rem;" icon="pi pi-link" v-tooltip.bottom="'Unlink Permission'" text @click="removePermission(data, authorisation)" />
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </td>
          <td v-if="editing" style="width: 5%; padding: 0.5rem">
            <div v-if="editing && editingSeed != data.id" class="flex">
              <Button class="p-button-text" style="width: 2rem;" icon="pi pi-plus-circle" v-tooltip.bottom="'Add Permission'" text @click="showAddPermission(data)" />
              <Button class="p-button-text" style="width: 2rem;" icon="pi pi-pencil" v-tooltip.bottom="'Edit Seed'" text @click="editSeed(data)" />
              <Button class="p-button-text" style="width: 2rem;" icon="pi pi-trash" v-tooltip.bottom="'Remove Seed'" text @click="targetSeeds.removeSeed(data.id)" />
            </div>
            <div v-else-if="editing && editingSeed == data.id" class="flex">
              <Button class="p-button-text" style="width: 2rem" icon="pi pi-save" v-tooltip.bottom="'Save'" text @click="editingSeed = 0" />
              <Button class="p-button-text" style="width: 2rem" icon="pi pi-times" v-tooltip.bottom="'Cancel'" text @click="cancelEditSeed()" />
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-else class="text-center">
      <p class="text-500">No seeds have been added to this target yet</p>
    </div>
  </WctTabViewPanel>
</template>

<style>
.target-seed-parent-table {
  border-collapse: collapse;
}

.target-seed-parent-table tr:not(:last-child) {
  border-bottom: 1px solid #e4e4e4 !important;
}

.target-seed-parent-table table {
  border: none;
  border-collapse: separate;
}

.target-seed-parent-table tbody tr:hover {
  background-color: #f5f5f5;
}
</style>
