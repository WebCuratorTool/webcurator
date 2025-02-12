<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { useDialog } from 'primevue/usedialog';
import { formatDatetime } from '@/utils/helper'
import { useTargetHarvestsDTO } from '@/stores/target';

import WctTabViewPanel from '@/components/WctTabViewPanel.vue'
import TargetTabPanelHarvetsTargetInstances from './TargetTabPanelHarvetsTargetInstances.vue';
import Button from 'primevue/button';

const viewHarvestModal = useDialog();

const targetSchedule = useTargetHarvestsDTO().targetSchedule;

const ViewHarvestModal = defineAsyncComponent(() => import('./modals/TargetViewHarvestModal.vue'));

const newHarverst = {
  cron: '',
  startDate: Date.now(),
  endDate: null,
  type: -3,
  nextExecutionDate: null,
  lastProcessedDate: null,
  dayOfMonth: '1',
  owner: ''
}

defineProps<{
    editing: boolean
}>()

const showViewHarvestModal = (targetSchedule: any, editingHarvest: boolean) => {
  viewHarvestModal.open(ViewHarvestModal, {
    props: { header: 'Schedule', modal: true, dismissableMask: true, style: { width: '50vw' } },
    data: { 
      targetSchedule: targetSchedule,
      editingHarvest: editingHarvest
    }
  });
}
</script>

<template>
  <div>
    <div v-if="editing" class="flex justify-content-end">
      <Button label="Harvest now" />
    </div>

    <div class="flex justify-content-between mt-6">
      <div class="flex align-items-center my-4">
        <h4 class="mb-0">Schedule</h4>
        <div v-if="editing" class="flex align-items-center ml-6">
          <label class="mr-2">Allow Harvest Optimization</label>
          <Checkbox
            v-model="targetSchedule.harvsestOptimization" 
            :binary="true"
          />
        </div>
      </div>
      <Button v-if="editing" icon="pi pi-plus" label="Add" text @click="showViewHarvestModal(newHarverst, true)" />
    </div>
    <WctTabViewPanel class="mt-2">
      <DataTable class="w-full" :rowHover="true" :value="targetSchedule.schedules">
        <Column field="cron" header="Schedule" />
        <Column field="owner" header="Owner" />
        <Column field="nextExecutionDate" header="Next Scheduled Time" dataType="date">
          <template #body="{ data }">
            {{ data.nextExecutionDate ? formatDatetime(data.nextExecutionDate) : '' }}
          </template>
        </Column>
        <Column header="Action">
          <template #body="{ data }">
            <Button class="p-button-text" style="width: 2rem;" icon="pi pi-eye" v-tooltip.bottom="'View Harvest'" text @click="showViewHarvestModal(data, false)" />
            <Button v-if="editing" class="p-button-text" style="width: 2rem;" icon="pi pi-pencil" v-tooltip.bottom="'Edit Harvest'" text @click="showViewHarvestModal(data, true)" />
            <Button v-if="editing" class="p-button-text" style="width: 2rem;" icon="pi pi-trash" v-tooltip.bottom="'Remove Harvest'" text />
          </template>
        </Column>
      </DataTable>
    </WctTabViewPanel>

    <TargetTabPanelHarvetsTargetInstances type="upcoming" header="Upcoming Target Instances" />

    <TargetTabPanelHarvetsTargetInstances type="latest" header="Last 5 Target Instances" />
  </div>
</template>