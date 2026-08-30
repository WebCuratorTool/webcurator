<script setup lang="ts">
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

const onChange = (event: Event) => {
  const checked = (event.target as HTMLInputElement).checked;
  if (props.binary) {
    emit("update:modelValue", checked);
    return;
  }

  const current = Array.isArray(props.modelValue) ? [...props.modelValue] : [];
  const idx = current.findIndex((item) => item === props.value);
  if (checked && idx < 0) {
    current.push(props.value);
  }
  if (!checked && idx >= 0) {
    current.splice(idx, 1);
  }
  emit("update:modelValue", current);
};

const isChecked = () => {
  if (props.binary) {
    return Boolean(props.modelValue);
  }
  return Array.isArray(props.modelValue)
    ? props.modelValue.includes(props.value)
    : false;
};
</script>

<template>
  <input
    :id="inputId"
    type="checkbox"
    :checked="isChecked()"
    :disabled="disabled"
    @change="onChange"
  />
</template>
