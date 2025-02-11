<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { formatDate, formatTime } from '@/utils/helper'
import { days, dates, parseCron, getCronMonths, getNextScheduledTimes, getMonthGroups, createCronExpression } from '@/utils/cronParser';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import Loading from '@/components/Loading.vue';
import WctFormField from '@/components/WctFormField.vue'
import { useTargetGeneralDTO, useTargetHarvestsDTO } from '@/stores/target';

const targetGeneral = useTargetGeneralDTO();
const targetHarvests = useTargetHarvestsDTO();

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
const months = ref('');
const monthGroups = ref<string[]>([]);
const newCronObject = ref({});

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
  cronFields.value = dialogRef.value.data.targetSchedule.cron != '' ? parseCron(dialogRef.value.data.targetSchedule.cron) : {};

  if (editing.value) {
    newCronObject.value.time = time.value != '' ? time.value : formatTime(Date.now());
    newCronObject.value.dayOfMonth = cronFields.value.dayOfMonth != '' ? cronFields.value.dayOfMonth : '';
    newCronObject.value.month = cronFields.value.month != '' ? cronFields.value.month : '';
    newCronObject.value.months = cronFields.value.months != '' ? cronFields.value.months : '';
    newCronObject.value.weekDays = cronFields.value.weekDays != '' ? cronFields.value.weekDays : '';
    newCronObject.value.year = cronFields.value.year != '' ? cronFields.value.year : '';
  }
})

const shouldShowMonths = () => {
 return ['quarterly', 'bimonthly', 'annually', 'half-yearly'].includes(scheduleType.value.toLowerCase());
}

const shouldShowDayOfMonth = () => {
 return ['monthly', 'bimonthly', 'quarterly', 'annually', 'half-yearly'].includes(scheduleType.value.toLowerCase());
}

const fetchMonthGroups = () => {
  if (shouldShowMonths()) {
    const results = getMonthGroups(scheduleType.value);
    months.value = results[0];
    monthGroups.value = results;
  }
  else monthGroups.value = [];
}

watch(scheduleType, () => {
  fetchMonthGroups();
});

const closeDialog = () => {
    dialogRef.value.close();
}

const save = () => {
  console.log(newCronObject.value.time);
  const cronExpression = createCronExpression(newCronObject.value);
  const scheduleKey = Object.keys(scheduleTypes.value).find((key) => scheduleTypes.value[key] == scheduleType.value);
  targetHarvests.addSchedule(
    {
      cron: cronExpression,
      startDate: startDate.value,
      endDate: endDate.value,
      type: scheduleKey,
      nextExecutionDate: getNextScheduledTimes(cronExpression, 1)[0],
      owner: targetGeneral.selectedUser.code
    }
  )
  closeDialog();
}

fetch();

</script>

<template>
  <Loading v-if="loading" />
  <div v-else-if="targetSchedule && !loading" class="h-full mt-3">
    <!-- From Date -->
    <WctFormField label="From Date">
      <Calendar v-if="editing" v-model="startDate" dateFormat="dd/mm/yy" :showIcon="false" />
      <p v-else class="font-semibold">{{ formatDate(targetSchedule.startDate) }}</p>
    </WctFormField>

    <!-- To Date -->
    <WctFormField label="To Date"> 
      <Calendar v-if="editing" v-model="endDate" dateFormat="dd/mm/yy" :showIcon="false" />
      <p v-else class="font-semibold">{{ targetSchedule.endDate? formatDate(targetSchedule.endDate) : '' }}</p>
    </WctFormField>

    <!-- Schedule Type -->
    <WctFormField label="Type">
      <Dropdown v-if="editing"
        v-model="scheduleType"
        :options="Object.values(scheduleTypes)"
        :disabled="!editing"
      />
      <p v-else class="font-semibold">{{ scheduleType }}</p> 
    </WctFormField>

    <!-- Day of Week -->
    <WctFormField v-if="scheduleType == 'Weekly'" label="Day">
      <Dropdown v-if="editing"
        v-model="newCronObject.dayOfWeek"
        :options="days"
      />
      <p v-else class="font-semibold">{{ cronFields.dayOfWeek }}</p>
    </WctFormField>

    <!-- Time -->
    <WctFormField v-if="scheduleType != 'Custom' && scheduleType != 'Every Monday at 9:00pm'" label="Time">
      <Calendar v-if="editing" v-model="newCronObject.time" timeOnly />
      <p v-else class="font-semibold">{{ formatTime(targetSchedule.nextExecutionDate) }}</p>
    </WctFormField>

    <!-- Day of Month -->
    <WctFormField v-if="!editing && !isNaN(cronFields.dayOfMonth)" label="Day of Month">
      <p class="font-semibold">{{ cronFields.dayOfMonth }}</p>
    </WctFormField>
    <WctFormField v-if="editing && shouldShowDayOfMonth()" label="Day of Month">
      <Dropdown
        v-model="newCronObject.dayOfMonth"
        :options="dates"
      />
    </WctFormField>

    <!-- Month -->
    <WctFormField v-if="editing && shouldShowMonths()" label="Month">
      <Dropdown v-if="editing"
        v-model="newCronObject.months"
        :options="monthGroups"
      />
    </WctFormField>
    <WctFormField v-if="!editing && !cronFields.month.includes('*') && !cronFields.month.includes('?')" label="Month">
      <p class="font-semibold">{{ getCronMonths(targetSchedule.cron) }}</p>
    </WctFormField>

    <div v-if="editing">
      <Button label="Save" @click="save"/>
      <Button label="Cancel" text class="ml-2" @click="closeDialog"/>
    </div>

    <div v-if="scheduleType == 'Custom'">
      <p v-for="(time, index) in getNextScheduledTimes(targetSchedule.cron, 10)" class="font-semibold" :key="index">
        {{ time }}
      </p>
    </div>
  </div>
</template>