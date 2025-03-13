<script setup lang="ts">
import { inject, ref, watch } from 'vue';
import { formatDate, formatTime } from '@/utils/helper'
import { days, dates, parseCron, getCronMonths, getNextScheduledTimes, getMonthGroups, createCronExpression, createCustomCronExpression } from '@/utils/cronParser';
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
const customScheduledTimes = ref<Date[]>([]);
const validationErrors = ref(false);
const newCronObject = ref({
  time: null,
  dayOfMonth: '',
  month: '',
  months: '',
  weekDays: '',
  year: '',
  dayOfWeek: ''
});

const fetch = () => {
  rest.get('targets/schedule-types').then((data: any) => {
    scheduleTypes.value = data;
    scheduleType.value = scheduleTypes.value[targetSchedule.value.type];
    loading.value = false;
  }).catch((err: any) => {
    console.log(err.message);
  })
}

targetSchedule.value = dialogRef.value.data.targetSchedule;
editing.value = dialogRef.value.data.editingHarvest;
startDate.value = formatDate(dialogRef.value.data.targetSchedule.startDate);
endDate.value = dialogRef.value.data.targetSchedule.endDate != null ? formatDate(dialogRef.value.data.targetSchedule.endDate) : '';
time.value = dialogRef.value.data.targetSchedule.nextExecutionDate != null ? formatTime(dialogRef.value.data.targetSchedule.nextExecutionDate) : '';
cronFields.value = dialogRef.value.data.targetSchedule.cron != '' ? parseCron(dialogRef.value.data.targetSchedule.cron) : { dayOfMonth: '' };

if (editing.value) {
  newCronObject.value.time = time.value != '' ? time.value : formatTime(Date.now());
  newCronObject.value.dayOfMonth = cronFields.value.dayOfMonth != '' ? cronFields.value.dayOfMonth : '1';
  newCronObject.value.month = cronFields.value.month;
  newCronObject.value.months = cronFields.value.months;
  newCronObject.value.weekDays = cronFields.value.weekDays;
  newCronObject.value.year = cronFields.value.year;
  newCronObject.value.dayOfWeek = (
    days.find((d) => d.slice(0, 3).toUpperCase() === cronFields.value.dayOfWeek)
  ) || 'Monday';
}

const shouldShowMonths = () => {
  return ['quarterly', 'bimonthly', 'annually', 'half-yearly'].includes(scheduleType.value.toLowerCase());
}

const shouldShowDayOfMonth = () => {
  const shouldShowDayOfMonth = ['monthly', 'bimonthly', 'quarterly', 'annually', 'half-yearly'].includes(scheduleType.value.toLowerCase());
  if (shouldShowDayOfMonth && newCronObject.value.dayOfMonth == '') {
    newCronObject.value.dayOfMonth = '1';
  } else if (!shouldShowDayOfMonth) {
    newCronObject.value.dayOfMonth = '';
  }
  return shouldShowDayOfMonth;
}

const shouldShowDayOfWeek = () => {
  const shouldShowDayOfWeek = scheduleType.value.toLowerCase() == 'weekly' || scheduleType.value.toLowerCase() == 'custom';
  if (shouldShowDayOfWeek && newCronObject.value.dayOfWeek == '') {
    newCronObject.value.dayOfWeek = 'Monday';
  } else if (!shouldShowDayOfWeek) {
    newCronObject.value.dayOfWeek = '';
  }
  return shouldShowDayOfWeek;
}

const fetchMonthGroups = () => {
  if (shouldShowMonths()) {
    const results = getMonthGroups(scheduleType.value);
    months.value = results[0];
    monthGroups.value = results;
  }
  else monthGroups.value = [];
}

const getNextCustomTimes = () => {
  // Temporary validation until validation library is implemented
  if ((!cronFields.value.minute || cronFields.value.minute == '') || (!cronFields.value.hour || cronFields.value.hour == '') || 
  (!cronFields.value.dayOfMonth || cronFields.value.dayOfMonth == '') || (!cronFields.value.dayOfWeek || cronFields.value.dayOfWeek == '') ||
  (!cronFields.value.month || cronFields.value.month == '') || (!cronFields.value.year || cronFields.value.year == '')) {
    validationErrors.value = true;
    customScheduledTimes.value = [];
  } else {
    validationErrors.value = false;
    const cronString = createCustomCronExpression(cronFields.value);
    customScheduledTimes.value = getNextScheduledTimes(cronString, 10, startDate.value);
  }
}

watch(scheduleType, () => {
  fetchMonthGroups();
});

const closeDialog = () => {
  dialogRef.value.close();
}

const save = () => {
  if (Object.prototype.toString.call(startDate.value) != '[object Date]') {
    const dateComponents = startDate.value.split('/');
    startDate.value = new Date(`${dateComponents[2]}-${dateComponents[1]}-${dateComponents[0]}`);  
  } else {
    startDate.value = new Date(startDate.value);
  }

  let cronExpression;

  if (scheduleType.value == 'Custom') {
    cronExpression = createCustomCronExpression(cronFields.value);
  } else {
    cronExpression = createCronExpression(newCronObject.value);
  }

  const scheduleKey = Object.keys(scheduleTypes.value).find((key) => scheduleTypes.value[key] == scheduleType.value);
  targetHarvests.addSchedule(
    {
      cron: cronExpression,
      startDate: startDate.value,
      endDate: endDate.value != '' ? endDate.value : null,
      type: scheduleKey,
      nextExecutionDate: getNextScheduledTimes(cronExpression, 1, startDate.value)[0],
      owner: targetGeneral.selectedUser.code
    }
  )
  closeDialog();
}

fetch();

</script>

<template>
  <Loading v-if="loading" />
  <div v-else-if="targetSchedule && !loading" class="h-full mt-3 flex">
    <div :class="editing && scheduleType == 'Custom' ? 'w-7' : 'w-full'">
      <p v-if="editing && validationErrors" class="text-red-600 font-semibold">
        Missing fields
      </p>
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
  
      <div v-if="scheduleType != 'Custom'">
        <!-- Day of Week -->
        <WctFormField v-if="shouldShowDayOfWeek()" label="Day">
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
      </div>
  
      <div v-if="scheduleType == 'Custom'">
        <WctFormField label="Minutes">
          <InputText v-if="editing" v-model="cronFields.minute" />
          <p v-else class="font-semibold">{{ cronFields.minute }}</p>
        </WctFormField>
  
        <WctFormField label="Hours">
          <InputText v-if="editing" v-model="cronFields.hour" />
          <p v-else class="font-semibold">{{ cronFields.hour }}</p>
        </WctFormField>
  
        <WctFormField label="Days of Week">
          <InputText v-if="editing" v-model="cronFields.dayOfWeek" />
          <p v-else class="font-semibold">{{ cronFields.dayOfWeek }}</p>  
        </WctFormField>
  
        <WctFormField label=" Days of Month">
          <InputText v-if="editing" v-model="cronFields.dayOfMonth" />
          <p v-else class="font-semibold">{{ cronFields.dayOfMonth }}</p>   
        </WctFormField>
  
        <WctFormField label="Months">
          <InputText v-if="editing" v-model="cronFields.month" />
          <p v-else class="font-semibold">{{ cronFields.month }}</p>   
        </WctFormField>
  
        <WctFormField label="Years">
          <InputText v-if="editing" v-model="cronFields.year" />
          <p v-else class="font-semibold">{{ cronFields.year }}</p>   
        </WctFormField>
      </div>
    
      <div v-if="editing">
        <Button label="Save" @click="save"/>
        <Button label="Cancel" text class="ml-2" @click="closeDialog"/>
      </div>
    </div>

    <div v-if="scheduleType == 'Custom'">
      <p>Next 10 scheduled times</p>
      <Button label="Test" outlined @click="getNextCustomTimes"/>
        <div v-if="customScheduledTimes.length">
          <p v-for="(time, index) in customScheduledTimes" class="font-semibold" :key="index">
            {{ time.toLocaleString() }}
          </p>
        </div>
    </div>
  </div>
</template>