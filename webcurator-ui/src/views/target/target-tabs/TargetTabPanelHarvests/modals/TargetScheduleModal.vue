<script setup lang="ts">
import Loading from '@/components/Loading.vue';
import WctFormField from '@/components/WctFormField.vue';
import { useTargetGeneralDTO, useTargetHarvestsDTO } from '@/stores/target';
import { createCronExpression, createCustomCronExpression, dates, days, getCronMonths, getMonthGroups, getNextScheduledTimes, parseCron } from '@/utils/cronParser';
import { formatDate, formatTime } from '@/utils/helper';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { computed, inject, ref, watch } from 'vue';

const targetGeneral = useTargetGeneralDTO();
const targetHarvests = useTargetHarvestsDTO();

const rest: UseFetchApis = useFetch();

const dialogRef: any = inject('dialogRef');

const targetSchedule = ref(dialogRef.value.data.targetSchedule);
const cronFields = ref();
const scheduleType = ref('');
const startDate = ref();
const endDate = ref();
const time = ref();
const editing = ref(dialogRef.value.data.editingSchedule);
const isNewSchedule = ref(dialogRef.value.data.isNewSchedule);
const scheduleTypes = ref();
const loading = ref(true);
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

startDate.value = formatDate(dialogRef.value.data.targetSchedule.startDate);
endDate.value = dialogRef.value.data.targetSchedule.endDate != null ? formatDate(dialogRef.value.data.targetSchedule.endDate) : '';
time.value = dialogRef.value.data.targetSchedule.nextExecutionDate != null ? formatTime(dialogRef.value.data.targetSchedule.nextExecutionDate) : '';
cronFields.value = dialogRef.value.data.targetSchedule.cron != '' ? parseCron(dialogRef.value.data.targetSchedule.cron) : { dayOfMonth: '', month: '' };

// Init a new cron object if in editing mode
if (editing.value) {
  newCronObject.value.time = time.value != '' ? time.value : formatTime(Date.now());
  newCronObject.value.dayOfMonth = cronFields.value.dayOfMonth != '' ? (cronFields.value.dayOfMonth == 'L' ? 'Last' : cronFields.value.dayOfMonth) : '1';
  newCronObject.value.month = cronFields.value.month;
  newCronObject.value.months = cronFields.value.month != '' ? getCronMonths(targetSchedule.value.cron) : '';
  newCronObject.value.weekDays = cronFields.value.weekDays;
  newCronObject.value.year = cronFields.value.year;
  newCronObject.value.dayOfWeek = days.find((d) => d.slice(0, 3).toUpperCase() === cronFields.value.dayOfWeek) || '';
}

const shouldShowMonths = computed(() => {
  return scheduleType.value && ['quarterly', 'bimonthly', 'annually', 'half-yearly'].includes(scheduleType.value.toLowerCase());
});

const shouldShowDayOfMonth = computed(() => {
  return scheduleType.value && ['monthly', 'bimonthly', 'quarterly', 'annually', 'half-yearly'].includes(scheduleType.value.toLowerCase());
});

const shouldShowDayOfWeek = computed(() => {
  return (scheduleType.value && scheduleType.value.toLowerCase() == 'weekly') || scheduleType.value.toLowerCase() == 'custom';
});

const fetch = () => {
  rest
    .get('targets/schedule-types')
    .then((data: any) => {
      scheduleTypes.value = data;
      scheduleType.value = scheduleTypes.value[targetSchedule.value.type];
      loading.value = false;
    })
    .catch((err: any) => {
      console.log(err.message);
    });
};

const getNextCustomTimes = () => {
  // Temporary validation until validation library is implemented
  if (
    !cronFields.value.minute ||
    cronFields.value.minute == '' ||
    !cronFields.value.hour ||
    cronFields.value.hour == '' ||
    !cronFields.value.dayOfMonth ||
    cronFields.value.dayOfMonth == '' ||
    !cronFields.value.dayOfWeek ||
    cronFields.value.dayOfWeek == '' ||
    !cronFields.value.month ||
    cronFields.value.month == '' ||
    !cronFields.value.year ||
    cronFields.value.year == ''
  ) {
    validationErrors.value = true;
    customScheduledTimes.value = [];
  } else {
    validationErrors.value = false;
    const cronString = createCustomCronExpression(cronFields.value);
    customScheduledTimes.value = getNextScheduledTimes(cronString, 10, startDate.value);
  }
};

const saveSchedule = () => {
  const parseDate = (dateStr: any) => {
    const [day, month, year] = dateStr.split('/');
    return new Date(`${year}-${month}-${day}`);
  };

  // Turn the start date into a date object
  const startDateObject = Object.prototype.toString.call(startDate.value) !== '[object Date]' ? parseDate(startDate.value) : new Date(startDate.value);

  // Create the cron string from all the parts chosen from the input fields
  const cronExpression = scheduleType.value === 'Custom' ? createCustomCronExpression(cronFields.value) : createCronExpression(newCronObject.value);

  // Get the schedule type number from the schedule types list
  const scheduleTypeNumber = Object.keys(scheduleTypes.value).find((key) => scheduleTypes.value[key] === scheduleType.value);

  const scheduleToSave = {
    cron: cronExpression,
    startDate: startDateObject,
    endDate: endDate.value || null,
    type: scheduleTypeNumber,
    nextExecutionDate: getNextScheduledTimes(cronExpression, 1, startDateObject)[0],
    owner: targetGeneral.selectedUser.code,
    // If editing an exsiting schedule, add the id to the object
    ...(isNewSchedule.value ? {} : { id: targetSchedule.value.id })
  };

  if (isNewSchedule.value) {
    targetHarvests.addSchedule(scheduleToSave);
  } else {
    targetHarvests.replaceSchedule(scheduleToSave);
  }

  closeDialog();
};

const closeDialog = () => {
  dialogRef.value.close();
};

const updateCronDayOfWeek = () => {
  if (shouldShowDayOfWeek.value) {
    newCronObject.value.dayOfWeek ||= 'Monday';
    newCronObject.value.dayOfMonth ||= '1';
  } else {
    newCronObject.value.dayOfWeek = '';
  }
};

const updateCronDayOfMonth = () => {
  newCronObject.value.dayOfMonth = shouldShowDayOfMonth.value ? newCronObject.value.dayOfMonth || '1' : '';
};

const updateMonthGroups = () => {
  if (shouldShowMonths.value) {
    const results = getMonthGroups(scheduleType.value);
    newCronObject.value.months = results[0];
    monthGroups.value = results;
  } else {
    newCronObject.value.months = '';
    monthGroups.value = [];
  }
};

watch(scheduleType, updateMonthGroups);
watch(shouldShowDayOfWeek, updateCronDayOfWeek);
watch(shouldShowDayOfMonth, updateCronDayOfMonth);

fetch();
</script>

<template>
  <Loading v-if="loading" />
  <div v-else-if="targetSchedule && !loading" class="h-full mt-3 flex">
    <div :class="editing && scheduleType == 'Custom' ? 'w-7' : 'w-full'">
      <p v-if="editing && validationErrors" class="text-red-600 font-semibold">Missing fields</p>
      <!-- From Date -->
      <WctFormField label="From Date">
        <DatePicker v-if="editing" v-model="startDate" dateFormat="dd/mm/yy" :showIcon="false" />
        <p v-else class="font-semibold">{{ formatDate(targetSchedule.startDate) }}</p>
      </WctFormField>

      <!-- To Date -->
      <WctFormField label="To Date">
        <DatePicker v-if="editing" v-model="endDate" dateFormat="dd/mm/yy" :showIcon="false" />
        <p v-else class="font-semibold">{{ targetSchedule.endDate ? formatDate(targetSchedule.endDate) : '' }}</p>
      </WctFormField>

      <!-- Schedule Type -->
      <WctFormField label="Type">
        <Select v-if="editing" v-model="scheduleType" :options="Object.values(scheduleTypes)" :disabled="!editing" />
        <p v-else class="font-semibold">{{ scheduleType }}</p>
      </WctFormField>

      <div v-if="scheduleType != 'Custom'">
        <!-- Day of Week -->
        <WctFormField v-if="shouldShowDayOfWeek" label="Day">
          <Select v-if="editing" v-model="newCronObject.dayOfWeek" :options="days" />
          <p v-else class="font-semibold">{{ cronFields.dayOfWeek }}</p>
        </WctFormField>

        <!-- Time -->
        <WctFormField v-if="scheduleType != 'Every Monday at 9:00pm'" label="Time">
          <DatePicker v-if="editing" v-model="newCronObject.time" timeOnly />
          <p v-else class="font-semibold">{{ formatTime(targetSchedule.nextExecutionDate) }}</p>
        </WctFormField>

        <!-- Day of Month -->
        <WctFormField v-if="!editing && !isNaN(cronFields.dayOfMonth)" label="Day of Month">
          <p class="font-semibold">{{ cronFields.dayOfMonth }}</p>
        </WctFormField>
        <WctFormField v-if="editing && shouldShowDayOfMonth" label="Day of Month">
          <Select v-model.sync="newCronObject.dayOfMonth" :options="dates" />
        </WctFormField>

        <!-- Month -->
        <WctFormField v-if="editing && shouldShowMonths" label="Month">
          <Select v-if="editing" v-model="newCronObject.months" :options="monthGroups" />
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

        <WctFormField label="Days of Month">
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

      <div v-if="editing" class="flex items-center justify-end w-full mt-4">
        <Button class="wct-primary-button" label="Save" @click="saveSchedule" />
        <Button label="Cancel" text class="ml-2" @click="closeDialog" />
      </div>
    </div>

    <div v-if="scheduleType == 'Custom' && editing">
      <p>Next 10 scheduled times</p>
      <Button label="Test" outlined @click="getNextCustomTimes" />
      <div v-if="customScheduledTimes.length" class="pt-4">
        <p v-for="(time, index) in customScheduledTimes" class="font-semibold" :key="index">
          {{ time.toLocaleString() }}
        </p>
      </div>
    </div>
  </div>
</template>
