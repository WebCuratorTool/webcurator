<script setup lang="ts">
import { inject, onMounted, ref } from 'vue';
import { formatDate, formatTime } from '@/utils/helper'
import { days, parseCron, getCronMonths, getNextScheduledTimes } from '@/utils/cronParser';

import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import Loading from '@/components/Loading.vue';
import WctFormField from '@/components/WctFormField.vue'

const rest: UseFetchApis = useFetch();

const dialogRef: any = inject('dialogRef');

const targetSchedule = ref();
const cronFields = ref();
const scheduleType = ref();
const startDate = ref();
const endDate = ref();
const time = ref();
const editing = ref(false);
const scheduleTypes = ref();
const loading = ref(true);

const fetch = () => {
  rest.get('targets/schedule-types').then((data: any) => {
    scheduleTypes.value = data;
    scheduleType.value = scheduleTypes.value[targetSchedule.value.type];
    loading.value = false;
  }).catch((err: any) => {
    console.log(err.message);
  })
}

onMounted(() => {
  targetSchedule.value = dialogRef.value.data.targetSchedule;
  editing.value = dialogRef.value.data.editingHarvest;
  startDate.value = formatDate(dialogRef.value.data.targetSchedule.startDate);
  endDate.value = dialogRef.value.data.targetSchedule.endDate != null ? formatDate(dialogRef.value.data.targetSchedule.endDate) : '';
  time.value = dialogRef.value.data.targetSchedule.nextExecutionDate != null ? formatTime(dialogRef.value.data.targetSchedule.nextExecutionDate) : '';
  cronFields.value = parseCron(dialogRef.value.data.targetSchedule.cron);
})

fetch();

</script>

<template>
  <Loading v-if="loading" />
  <div v-else-if="targetSchedule && !loading" class="h-full mt-3">
    <WctFormField label="From Date">
      <Calendar v-if="editing" v-model="startDate" dateFormat="dd/mm/yy" :showIcon="false" />
      <p v-else class="font-semibold">{{ formatDate(targetSchedule.startDate) }}</p>
    </WctFormField>
    <WctFormField label="To Date"> 
      <Calendar v-if="editing" v-model="endDate" dateFormat="dd/mm/yy" :showIcon="false" />
      <p v-else class="font-semibold">{{ targetSchedule.endDate? formatDate(targetSchedule.endDate) : '' }}</p>
    </WctFormField>
    <WctFormField label="Type">
      <Dropdown v-if="editing"
        v-model="scheduleType"
        :options="Object.values(scheduleTypes)"
        :disabled="!editing"
      />
      <p v-else class="font-semibold">{{ scheduleType }}</p> 
    </WctFormField>

    <WctFormField v-if="scheduleType == 'Weekly'" label="Day">
      <Dropdown v-if="editing"
        v-model="cronFields.dayOfWeek"
        :options="days"
      />
      <p v-else class="font-semibold">{{ cronFields.dayOfWeek }}</p>
    </WctFormField>

    <WctFormField v-if="scheduleType != 'Custom' && scheduleType != 'Every Monday at 9:00pm'" label="Time">
      <Calendar v-if="editing" v-model="time" timeOnly />
      <p v-else class="font-semibold">{{ formatTime(targetSchedule.nextExecutionDate) }}</p>
    </WctFormField>

    <WctFormField v-if="!isNaN(cronFields.dayOfMonth)" label="Day of Month">
      <p class="font-semibold">{{ cronFields.dayOfMonth }}</p>
    </WctFormField>
    <WctFormField v-if="!cronFields.month.includes('*') && !cronFields.month.includes('?')" label="Month">
      <p class="font-semibold">{{ getCronMonths(targetSchedule.cron) }}</p>
    </WctFormField>

    <div v-if="scheduleType == 'Custom'">
      <p v-for="(time, index) in getNextScheduledTimes(targetSchedule.cron)" class="font-semibold" :key="index">
        {{ time }}
      </p>
    </div>
  </div>
</template>