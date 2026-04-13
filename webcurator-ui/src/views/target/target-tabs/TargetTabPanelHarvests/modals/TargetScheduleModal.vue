<script setup lang="ts">
import type { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { computed, inject, type Ref, ref, watch } from "vue";
import * as z from "zod";

import Loading from "@/components/Loading.vue";
import WctFormField from "@/components/WctFormField.vue";
import { useTargetGeneralDTO, useTargetHarvestsDTO } from "@/stores/target";
import type { FlattenedErrors } from "@/types/validationErrors";
import {
  createCronExpression,
  createCustomCronExpression,
  dates,
  days,
  getCronMonths,
  getMonthGroups,
  getNextScheduledTimes,
  parseCron,
} from "@/utils/cronParser";
import { formatDate, formatTime } from "@/utils/helper";
import { useFetch, type UseFetchApis } from "@/utils/rest.api";

const targetGeneral = useTargetGeneralDTO();
const targetHarvests = useTargetHarvestsDTO();

const rest: UseFetchApis = useFetch();

const dialogRef = inject<Ref<DynamicDialogInstance>>("dialogRef");

const targetSchedule = ref(dialogRef?.value.data.targetSchedule);
const cronFields = ref();
const scheduleType = ref("");
const startDate = ref();
const endDate = ref();
const time = ref();
const editing = ref(dialogRef?.value.data.editingSchedule);
const isNewSchedule = ref(dialogRef?.value.data.isNewSchedule);
const scheduleTypes = ref(<Record<number, string>>{});
const loading = ref(true);
const monthGroups = ref<string[]>([]);
const customScheduledTimes = ref<Date[]>([]);
const selectedTime = ref<Date | null>(null);
const newCronObject = ref({
  time: "",
  dayOfMonth: "",
  month: "",
  months: "",
  weekDays: "",
  year: "",
  dayOfWeek: "",
});

const validationErrors = ref<FlattenedErrors>({});

startDate.value = formatDate(dialogRef?.value.data.targetSchedule.startDate);
endDate.value =
  dialogRef?.value.data.targetSchedule.endDate != null
    ? formatDate(dialogRef?.value.data.targetSchedule.endDate)
    : "";
time.value =
  dialogRef?.value.data.targetSchedule.nextExecutionDate != null
    ? formatTime(dialogRef?.value.data.targetSchedule.nextExecutionDate)
    : "";
cronFields.value =
  dialogRef?.value.data.targetSchedule.cron != ""
    ? parseCron(dialogRef?.value.data.targetSchedule.cron)
    : {
        dayOfMonth: "",
        month: "",
        minute: "",
        hour: "",
        dayOfWeek: "?",
        year: "*",
      };

// Init a new cron object if in editing mode
if (editing.value) {
  newCronObject.value.time =
    time.value != "" ? time.value : formatTime(Date.now());
  newCronObject.value.dayOfMonth =
    cronFields.value.dayOfMonth != ""
      ? cronFields.value.dayOfMonth == "L"
        ? "Last"
        : cronFields.value.dayOfMonth
      : "1";
  newCronObject.value.month = cronFields.value.month;
  newCronObject.value.months =
    cronFields.value.month != ""
      ? getCronMonths(targetSchedule.value.cron)
      : "";
  newCronObject.value.weekDays = cronFields.value.weekDays;
  newCronObject.value.year = cronFields.value.year;
  newCronObject.value.dayOfWeek =
    days.find(
      (d) => d.slice(0, 3).toUpperCase() === cronFields.value.dayOfWeek,
    ) || "";
}

const shouldShowMonths = computed(() => {
  return (
    scheduleType.value &&
    ["quarterly", "bimonthly", "annually", "half-yearly"].includes(
      scheduleType.value.toLowerCase(),
    )
  );
});

const shouldShowDayOfMonth = computed(() => {
  return (
    scheduleType.value &&
    ["monthly", "bimonthly", "quarterly", "annually", "half-yearly"].includes(
      scheduleType.value.toLowerCase(),
    )
  );
});

const shouldShowDayOfWeek = computed(() => {
  return (
    (scheduleType.value && scheduleType.value.toLowerCase() == "weekly") ||
    scheduleType.value.toLowerCase() == "custom"
  );
});

const fetch = () => {
  rest
    .get<Record<number, string>>("targets/schedule-types")
    .then((data) => {
      scheduleTypes.value = data;
      scheduleType.value = scheduleTypes.value[targetSchedule.value.type];
    })
    .finally(() => {
      loading.value = false;
    });
};

// Validation schema for custom cron
const cronPart = (min: number, max: number) =>
  z
    .string()
    .min(min, `Field is required`)
    .max(max)
    .regex(/^(?=.*[0-9*?])[0-9*?\\/,\\-]+$/, "Invalid cron part");

const customScheduleSchema = z.object({
  minute: cronPart(1, 4),
  hour: cronPart(1, 4),
  dayOfMonth: cronPart(1, 4),
  month: cronPart(1, 4),
  year: cronPart(1, 4),
  dayOfWeek: cronPart(1, 4),
});

const getNextCustomTimes = () => {
  // Validate the cron fields against the schema
  validationErrors.value = {};

  const validationResult = customScheduleSchema.safeParse(cronFields.value);
  if (!validationResult.success) {
    validationErrors.value = z.flattenError(validationResult.error);
  } else {
    const cronString = createCustomCronExpression(cronFields.value);
    customScheduledTimes.value = getNextScheduledTimes(
      cronString,
      10,
      startDate.value,
    );
  }
};

const saveSchedule = () => {
  const parseDate = (dateStr: string) => {
    const [day, month, year] = dateStr.split("/");
    return new Date(`${year}-${month}-${day}`);
  };

  if (scheduleType.value === "Custom") {
    const validationResult = customScheduleSchema.safeParse(cronFields.value);
    if (!validationResult.success) {
      validationErrors.value = z.flattenError(validationResult.error);
      return;
    }
  }

  // Turn the start date into a date object
  const startDateObject =
    Object.prototype.toString.call(startDate.value) !== "[object Date]"
      ? parseDate(startDate.value)
      : new Date(startDate.value);

  // Create the cron string from all the parts chosen from the input fields
  const cronExpression =
    scheduleType.value === "Custom"
      ? createCustomCronExpression(cronFields.value)
      : createCronExpression(newCronObject.value);

  // Get the schedule type number from the schedule types list
  const scheduleTypeKey = Object.keys(scheduleTypes.value).find(
    (key: string) => scheduleTypes.value[Number(key)] === scheduleType.value,
  );
  const scheduleTypeNumber =
    scheduleTypeKey !== undefined ? Number(scheduleTypeKey) : 0;

  const scheduleToSave = {
    cron: cronExpression,
    startDate: startDateObject,
    endDate: endDate.value || null,
    type: scheduleTypeNumber,
    nextExecutionDate: getNextScheduledTimes(
      cronExpression,
      1,
      startDateObject,
    )[0],
    owner: targetGeneral.selectedUser.code,
    // If editing an exsiting schedule, add the id to the object
    ...(isNewSchedule.value ? {} : { id: targetSchedule.value.id }),
  };

  if (isNewSchedule.value) {
    if (targetHarvests) targetHarvests.addSchedule(scheduleToSave);
  } else {
    targetHarvests.replaceSchedule(scheduleToSave);
  }

  closeDialog();
};

const closeDialog = () => {
  dialogRef?.value.close();
};

const updateCronDayOfWeek = () => {
  if (shouldShowDayOfWeek.value) {
    newCronObject.value.dayOfWeek ||= "Monday";
    newCronObject.value.dayOfMonth ||= "1";
  } else {
    newCronObject.value.dayOfWeek = "";
  }
};

const updateCronDayOfMonth = () => {
  newCronObject.value.dayOfMonth = shouldShowDayOfMonth.value
    ? newCronObject.value.dayOfMonth || "1"
    : "";
};

const updateMonthGroups = () => {
  if (shouldShowMonths.value) {
    const results = getMonthGroups(scheduleType.value);
    newCronObject.value.months = results[0];
    monthGroups.value = results;
  } else {
    newCronObject.value.months = "";
    monthGroups.value = [];
  }
};

watch(scheduleType, updateMonthGroups);
watch(shouldShowDayOfWeek, updateCronDayOfWeek);
watch(shouldShowDayOfMonth, updateCronDayOfMonth);
watch(selectedTime, (newVal) => {
  if (newVal) {
    const hours = newVal.getHours().toString().padStart(2, "0");
    const minutes = newVal.getMinutes().toString().padStart(2, "0");
    newCronObject.value.time = `${hours}:${minutes}`;
  } else {
    newCronObject.value.time = "";
  }
});

watch(
  () => newCronObject.value.time,
  (newVal) => {
    if (newVal) {
      const [hours, minutes] = newVal.split(":").map(Number);
      const date = new Date();
      date.setHours(hours);
      date.setMinutes(minutes);
      selectedTime.value = date;
    } else {
      selectedTime.value = null;
    }
  },
  { immediate: true }, // Run on component mount
);

fetch();
</script>

<template>
  <Loading v-if="loading" />
  <div v-else-if="targetSchedule && !loading" class="h-full mt-3 flex">
    <div :class="editing && scheduleType == 'Custom' ? 'w-70' : 'w-full'">
      <!-- <p v-if="editing && validationErrors" class="text-red-600 font-semibold">
        Missing fields
      </p> -->
      <!-- From Date -->
      <WctFormField label="From Date">
        <DatePicker
          v-if="editing"
          v-model="startDate"
          dateFormat="dd/mm/yy"
          :showIcon="false"
        />
        <p v-else class="font-semibold">
          {{ formatDate(targetSchedule.startDate) }}
        </p>
      </WctFormField>

      <!-- To Date -->
      <WctFormField label="To Date">
        <DatePicker
          v-if="editing"
          v-model="endDate"
          dateFormat="dd/mm/yy"
          :showIcon="false"
        />
        <p v-else class="font-semibold">
          {{ targetSchedule.endDate ? formatDate(targetSchedule.endDate) : "" }}
        </p>
      </WctFormField>

      <!-- Schedule Type -->
      <WctFormField label="Type">
        <Select
          v-if="editing"
          v-model="scheduleType"
          :options="Object.values(scheduleTypes)"
          :disabled="!editing"
        />
        <p v-else class="font-semibold">{{ scheduleType }}</p>
      </WctFormField>

      <div v-if="scheduleType != 'Custom'">
        <!-- Day of Week -->
        <WctFormField v-if="shouldShowDayOfWeek" label="Day">
          <Select
            v-if="editing"
            v-model="newCronObject.dayOfWeek"
            :options="days"
          />
          <p v-else class="font-semibold">{{ cronFields.dayOfWeek }}</p>
        </WctFormField>

        <!-- Time -->
        <WctFormField
          v-if="scheduleType != 'Every Monday at 9:00pm'"
          label="Time"
        >
          <DatePicker v-if="editing" v-model="selectedTime" timeOnly />
          <p v-else class="font-semibold">
            {{ formatTime(targetSchedule.nextExecutionDate) }}
          </p>
        </WctFormField>

        <!-- Day of Month -->
        <WctFormField
          v-if="!editing && !isNaN(cronFields.dayOfMonth)"
          label="Day of Month"
        >
          <p class="font-semibold">{{ cronFields.dayOfMonth }}</p>
        </WctFormField>
        <WctFormField
          v-if="editing && shouldShowDayOfMonth"
          label="Day of Month"
        >
          <Select v-model.sync="newCronObject.dayOfMonth" :options="dates" />
        </WctFormField>

        <!-- Month -->
        <WctFormField v-if="editing && shouldShowMonths" label="Month">
          <Select
            v-if="editing"
            v-model="newCronObject.months"
            :options="monthGroups"
          />
        </WctFormField>
        <WctFormField
          v-if="
            !editing &&
            !cronFields.month.includes('*') &&
            !cronFields.month.includes('?')
          "
          label="Month"
        >
          <p class="font-semibold">{{ getCronMonths(targetSchedule.cron) }}</p>
        </WctFormField>
      </div>

      <!-- Custom cron input -->
      <div v-if="scheduleType == 'Custom'">
        <WctFormField label="Minutes">
          <InputText
            v-if="editing"
            v-model="cronFields.minute"
            :invalid="!!validationErrors.fieldErrors?.minute?.length"
          />
          <p v-else class="font-semibold">{{ cronFields.minute }}</p>
          <Message
            v-if="validationErrors.fieldErrors?.minute?.length"
            severity="error"
            size="small"
            variant="simple"
          >
            {{ validationErrors.fieldErrors.minute[0] }}
          </Message>
        </WctFormField>

        <WctFormField label="Hours">
          <InputText
            v-if="editing"
            v-model="cronFields.hour"
            :invalid="!!validationErrors.fieldErrors?.hour?.length"
          />
          <p v-else class="font-semibold">{{ cronFields.hour }}</p>
          <Message
            v-if="validationErrors.fieldErrors?.hour?.length"
            severity="error"
            size="small"
            variant="simple"
          >
            {{ validationErrors.fieldErrors.hour[0] }}
          </Message>
        </WctFormField>

        <WctFormField label="Days of Week">
          <InputText
            v-if="editing"
            v-model="cronFields.dayOfWeek"
            :invalid="!!validationErrors.fieldErrors?.dayOfWeek?.length"
          />
          <p v-else class="font-semibold">{{ cronFields.dayOfWeek }}</p>
          <Message
            v-if="validationErrors.fieldErrors?.dayOfWeek?.length"
            severity="error"
            size="small"
            variant="simple"
          >
            {{ validationErrors.fieldErrors.dayOfWeek[0] }}
          </Message>
        </WctFormField>

        <WctFormField label="Days of Month">
          <InputText
            v-if="editing"
            v-model="cronFields.dayOfMonth"
            :invalid="!!validationErrors.fieldErrors?.dayOfMonth?.length"
          />
          <p v-else class="font-semibold">{{ cronFields.dayOfMonth }}</p>
          <Message
            v-if="validationErrors.fieldErrors?.dayOfMonth?.length"
            severity="error"
            size="small"
            variant="simple"
          >
            {{ validationErrors.fieldErrors.dayOfMonth[0] }}
          </Message>
        </WctFormField>

        <WctFormField label="Months">
          <InputText
            v-if="editing"
            v-model="cronFields.month"
            :invalid="!!validationErrors.fieldErrors?.month?.length"
          />
          <p v-else class="font-semibold">{{ cronFields.month }}</p>
          <Message
            v-if="validationErrors.fieldErrors?.month?.length"
            severity="error"
            size="small"
            variant="simple"
          >
            {{ validationErrors.fieldErrors.month[0] }}
          </Message>
        </WctFormField>

        <WctFormField label="Years">
          <InputText
            v-if="editing"
            v-model="cronFields.year"
            :invalid="!!validationErrors.fieldErrors?.year?.length"
          />
          <p v-else class="font-semibold">{{ cronFields.year }}</p>
          <Message
            v-if="validationErrors.fieldErrors?.year?.length"
            severity="error"
            size="small"
            variant="simple"
          >
            {{ validationErrors.fieldErrors.year[0] }}
          </Message>
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
        <p
          v-for="(time, index) in customScheduledTimes"
          class="font-semibold"
          :key="index"
        >
          {{ time.toLocaleString() }}
        </p>
      </div>
    </div>
  </div>
</template>
