<script setup lang="ts">
import { computed, useAttrs } from "vue";

const props = defineProps<{
  modelValue?: string | number | Date | null;
  disabled?: boolean;
  timeOnly?: boolean;
  showTime?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

const attrs = useAttrs();

const toInputValue = () => {
  if (props.modelValue instanceof Date) {
    return props.modelValue.toISOString().slice(0, props.timeOnly ? 16 : 10);
  }
  if (typeof props.modelValue === "number") {
    const date = new Date(props.modelValue);
    return props.showTime
      ? date.toISOString().slice(0, 16)
      : date.toISOString().slice(0, 10);
  }
  return String(props.modelValue ?? "");
};

const model = computed({
  get: () => toInputValue(),
  set: (value: string) => emit("update:modelValue", value),
});
</script>

<template>
  <UInput
    v-bind="attrs"
    v-model="model"
    :type="timeOnly ? 'time' : showTime ? 'datetime-local' : 'date'"
    :disabled="disabled"
  />
</template>
