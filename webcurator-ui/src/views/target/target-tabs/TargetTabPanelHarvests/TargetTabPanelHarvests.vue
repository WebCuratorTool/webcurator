<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { useDialog } from 'primevue/usedialog';
import { formatDatetime } from '@/utils/helper'
import { useTargetHarvestsDTO } from '@/stores/target';

import WctFormField from '@/components/WctFormField.vue'
import WctTabViewPanel from '@/components/WctTabViewPanel.vue'
import type { TargetHarvest } from '@/types/target';
import Button from 'primevue/button';

const targetSchedule = useTargetHarvestsDTO().targetSchedule;

const ViewHarvestModal = defineAsyncComponent(() => import('./modals/TargetViewHarvestModal.vue'));

const newHarverst = {
  cron: '',
  startDate: null,
  endDate: null,
  type: 0,
  nextExecutionDate: null,
  lastProcessedDate: null,
  owner: ''
}

defineProps<{
    editing: boolean
}>()


const viewHarvestModal = useDialog();

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
    <div v-if="editing">
      <Button label="Harvest now" />
      <WctFormField label="Allow harvest optimization" class="mt-2">
        <Checkbox
          v-model="targetSchedule.harvsestOptimization" 
          :binary="true"
        />
      </WctFormField>
    </div>

    <div class="flex justify-content-end">
      <Button v-if="editing" icon="pi pi-plus" label="Add" text @click="showViewHarvestModal(newHarverst, true)" />
    </div>

    <WctTabViewPanel>
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
  </div>
</template>