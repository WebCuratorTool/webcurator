<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref } from 'vue';
import { useDialog } from 'primevue/usedialog';
import { formatDatetime } from '@/utils/helper'
import { useTargetHarvestsDTO } from '@/stores/target';
import { useTargetInstanceStateStore } from '@/stores/targetInstance';

import WctTabViewPanel from '@/components/WctTabViewPanel.vue'
import TargetTabPanelHarvetsTargetInstances from './TargetTabPanelHarvetsTargetInstances.vue';
import Button from 'primevue/button';

const scheduleModal = useDialog();

const targetHarvests = useTargetHarvestsDTO();
const targetSchedule = useTargetHarvestsDTO().targetSchedule;

const targetInstanceStates = ref<{ [key: string]: string }>({});

const ScheduleModal = defineAsyncComponent(() => import('./modals/TargetScheduleModal.vue'));

const newSchedule = {
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

const showScheduleModal = (targetSchedule: any, editingSchedule: boolean, isNewSchedule: boolean) => {
  scheduleModal.open(ScheduleModal, {
    props: { header: 'Schedule', modal: true, dismissableMask: true, style: { width: '50vw' } },
    data: { 
      targetSchedule: targetSchedule,
      editingSchedule: editingSchedule,
      isNewSchedule: isNewSchedule
    }
  });
}

onMounted(async () => {
  const states = await useTargetInstanceStateStore().fetch();
  targetInstanceStates.value = states;
})

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
      <Button v-if="editing" icon="pi pi-plus" label="Add" text @click="showScheduleModal(newSchedule, true, true)" />
    </div>
    <WctTabViewPanel class="mt-2">
      <DataTable v-if="targetSchedule.schedules && targetSchedule.schedules.length" class="w-full" :rowHover="true" :value="targetSchedule.schedules">
        <Column field="cron" header="Schedule" />
        <Column field="owner" header="Owner" />
        <Column field="nextExecutionDate" header="Next Scheduled Time" dataType="date">
          <template #body="{ data }">
            {{ data.nextExecutionDate ? formatDatetime(data.nextExecutionDate) : '' }}
          </template>
        </Column>
        <Column header="Action">
          <template #body="{ data }">
            <Button class="p-button-text" style="width: 2rem;" icon="pi pi-eye" v-tooltip.bottom="'View Schedule'" text @click="showScheduleModal(data, false, false)" />
            <Button v-if="editing" class="p-button-text" style="width: 2rem;" icon="pi pi-pencil" v-tooltip.bottom="'Edit Schedule'" text @click="showScheduleModal(data, true, false)" />
            <Button v-if="editing" class="p-button-text" style="width: 2rem;" icon="pi pi-trash" v-tooltip.bottom="'Remove Schedule'" text @click="targetHarvests.removeSchedule(data.id)" />
          </template>
        </Column>
      </DataTable>
      <div v-else class="text-center">
      <p class="text-500">No schedules have been created for this target</p>
    </div>
    </WctTabViewPanel>

    <TargetTabPanelHarvetsTargetInstances type="upcoming" header="Upcoming Target Instances" :targetInstanceStates=targetInstanceStates />

    <TargetTabPanelHarvetsTargetInstances type="latest" header="Last 5 Target Instances" :targetInstanceStates=targetInstanceStates />
  </div>
</template>