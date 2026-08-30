<script setup lang="ts">
import { computed, useAttrs } from "vue";

const props = defineProps<{
  modelValue?: number | string | null;
  min?: number;
  max?: number;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: number | null];
}>();

const attrs = useAttrs();

const model = computed<number | undefined>({
  get: () => {
    if (props.modelValue === null || props.modelValue === undefined || props.modelValue === "") {
      return undefined;
    }
    const parsed = Number(props.modelValue);
    return Number.isNaN(parsed) ? undefined : parsed;
  },
  set: (value) => {
    emit("update:modelValue", value ?? null);
  },
});
</script>

<template>
  <UInputNumber
    v-bind="attrs"
    v-model="model"
    :min="min"
    :max="max"
    :disabled="disabled"
  />
</template>
