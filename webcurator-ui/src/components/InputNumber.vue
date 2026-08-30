<script setup lang="ts">
const props = defineProps<{
  modelValue?: number | string | null;
  min?: number;
  max?: number;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: number | null];
}>();

const onInput = (event: Event) => {
  const value = (event.target as HTMLInputElement).value;
  if (value === "") {
    emit("update:modelValue", null);
    return;
  }
  const parsed = Number(value);
  emit("update:modelValue", Number.isNaN(parsed) ? null : parsed);
};
</script>

<template>
  <input
    type="number"
    class="wct-input"
    :value="props.modelValue ?? ''"
    :min="min"
    :max="max"
    :disabled="disabled"
    @input="onInput"
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
