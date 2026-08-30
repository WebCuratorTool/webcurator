<script setup lang="ts">
const props = defineProps<{
  modelValue?: string | number | Date | null;
  disabled?: boolean;
  timeOnly?: boolean;
  showTime?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

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
</script>

<template>
  <input
    class="wct-input"
    :type="timeOnly ? 'time' : showTime ? 'datetime-local' : 'date'"
    :value="toInputValue()"
    :disabled="disabled"
    @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
  />
</template>

<style scoped>
.wct-input {
  width: 100%;
  min-height: 2.25rem;
  border: 1px solid #cbd5e1;
  border-radius: 0.5rem;
  padding: 0.35rem 0.65rem;
}
</style>
