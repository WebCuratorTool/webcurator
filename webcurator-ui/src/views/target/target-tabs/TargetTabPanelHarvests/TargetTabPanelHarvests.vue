<script setup lang="ts">
import { useDialog } from "primevue/usedialog";
import { defineAsyncComponent, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";

import WctTabViewPanel from "@/components/WctTabViewPanel.vue";
import { useTargetHarvestsDTO } from "@/stores/target";
import { useTargetInstanceStateStore } from "@/stores/targetInstance";
import type { TargetHarvest } from "@/types/target";
import { formatDatetime } from "@/utils/helper";

import TargetTabPanelHarvetsTargetInstances from "./TargetTabPanelHarvetsTargetInstances.vue";

const ScheduleModal = defineAsyncComponent(
  () => import("./modals/TargetScheduleModal.vue"),
);
const scheduleModal = useDialog();

const route = useRoute();
const targetId = route.params.id as string;
const { t } = useI18n();

const targetHarvests = useTargetHarvestsDTO();
const targetSchedule = useTargetHarvestsDTO().targetSchedule;

const targetInstanceStates = ref<{ [key: string]: string }>({});

const newSchedule = {
  cron: "",
  startDate: Date.now(),
  endDate: null,
  type: -3,
  nextExecutionDate: null,
  lastProcessedDate: null,
  dayOfMonth: "1",
  owner: "",
};

defineProps<{
  editing: boolean;
}>();

const showScheduleModal = (
  targetSchedule: TargetHarvest,
  editingSchedule: boolean,
  isNewSchedule: boolean,
) => {
  scheduleModal.open(ScheduleModal, {
    props: {
      header: t("target.harvestsPanel.schedule"),
      modal: true,
      dismissableMask: true,
    },
    data: {
      targetSchedule: targetSchedule,
      editingSchedule: editingSchedule,
      isNewSchedule: isNewSchedule,
    },
  });
};

onMounted(async () => {
  const states: Record<number, string> =
    await useTargetInstanceStateStore().fetch();
  targetInstanceStates.value = states;
});
</script>

<template>
  <div>
    <div v-if="editing" class="flex justify-end">
      <Button
        class="wct-primary-button"
        :label="t('target.harvestsPanel.harvestNow')"
      />
    </div>

    <div class="flex items-center justify-between mt-6">
      <div class="flex items-center py-4 gap-4">
        <h4
          style="
            display: inline-block;
            height: auto;
            margin: 0;
            padding: 0 0 4px 0;
          "
        >
          {{ t("target.harvestsPanel.schedule") }}
        </h4>
        <div v-if="editing" class="flex items-center">
          <label for="allow-harvest-optimization" class="mr-2">{{
            t("target.harvestsPanel.allowHarvestOptimization")
          }}</label>
          <Checkbox
            v-model="targetSchedule.harvsestOptimization"
            :binary="true"
            inputId="allow-harvest-optimization"
          />
        </div>
      </div>
      <Button
        v-if="editing"
        icon="pi pi-plus"
        :label="t('common.add')"
        text
        @click="showScheduleModal(newSchedule, true, true)"
      />
    </div>
    <WctTabViewPanel class="mt-2">
      <DataTable
        v-if="targetSchedule.schedules && targetSchedule.schedules.length"
        class="w-full"
        :rowHover="true"
        :value="targetSchedule.schedules"
      >
        <Column field="cron" :header="t('target.harvestsPanel.schedule')" />
        <Column field="owner" :header="t('common.owner')" />
        <Column
          field="nextExecutionDate"
          :header="t('target.harvestsPanel.nextScheduledTime')"
          dataType="date"
        >
          <template #body="{ data }">
            {{
              data.nextExecutionDate
                ? formatDatetime(data.nextExecutionDate)
                : ""
            }}
          </template>
        </Column>
        <Column :header="t('target.harvestsPanel.action')">
          <template #body="{ data }">
            <Button
              class="p-button-text"
              style="width: 2rem"
              icon="pi pi-eye"
              v-tooltip.bottom="t('target.harvestsPanel.viewSchedule')"
              text
              @click="showScheduleModal(data, false, false)"
            />
            <Button
              v-if="editing"
              class="p-button-text"
              style="width: 2rem"
              icon="pi pi-pencil"
              v-tooltip.bottom="t('target.harvestsPanel.editSchedule')"
              text
              @click="showScheduleModal(data, true, false)"
            />
            <Button
              v-if="editing"
              class="p-button-text"
              style="width: 2rem"
              icon="pi pi-trash"
              v-tooltip.bottom="t('target.harvestsPanel.removeSchedule')"
              text
              @click="targetHarvests.removeSchedule(data.id)"
            />
          </template>
        </Column>
      </DataTable>
      <div v-else class="text-center">
        <p class="text-500">{{ t("target.harvestsPanel.noSchedules") }}</p>
      </div>
    </WctTabViewPanel>

    <TargetTabPanelHarvetsTargetInstances
      v-if="targetId"
      type="upcoming"
      :header="t('target.harvestsPanel.upcomingTargetInstances')"
      :targetInstanceStates="targetInstanceStates"
      :targetId="targetId"
    />

    <TargetTabPanelHarvetsTargetInstances
      v-if="targetId"
      type="latest"
      :header="t('target.harvestsPanel.lastFiveTargetInstances')"
      :targetInstanceStates="targetInstanceStates"
      :targetId="targetId"
    />
  </div>
</template>
