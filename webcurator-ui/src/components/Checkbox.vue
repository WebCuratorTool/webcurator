<script setup lang="ts">
import { computed, useAttrs } from "vue";

const props = defineProps<{
  modelValue?: any;
  binary?: boolean;
  value?: any;
  inputId?: string;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: any];
}>();

const attrs = useAttrs();

const checked = computed(() => {
  if (props.binary) {
    return Boolean(props.modelValue);
  }
  return Array.isArray(props.modelValue)
    ? props.modelValue.includes(props.value)
    : false;
});

const onUpdate = (nextState: boolean | "indeterminate") => {
  const isChecked = nextState === true;
  if (props.binary) {
    emit("update:modelValue", isChecked);
    return;
  }

  const current = Array.isArray(props.modelValue) ? [...props.modelValue] : [];
  const idx = current.findIndex((item) => item === props.value);
  if (isChecked && idx < 0) {
    current.push(props.value);
  }
  if (!isChecked && idx >= 0) {
    current.splice(idx, 1);
  }
  emit("update:modelValue", current);
};
</script>

<template>
  <UCheckbox
    v-bind="attrs"
    :id="inputId"
    :model-value="checked"
    :value="value"
    :disabled="disabled"
    @update:model-value="onUpdate"
  />
</template>
