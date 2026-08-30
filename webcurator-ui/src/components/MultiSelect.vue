<script setup lang="ts">
import { computed, useAttrs } from "vue";

const props = defineProps<{
  modelValue?: any[];
  options?: any[];
  optionLabel?: string;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: any[]];
}>();

const attrs = useAttrs();

const mappedItems = computed(() => {
  return (props.options ?? []).map((option) => {
    const label =
      props.optionLabel && option && typeof option === "object"
        ? String(option[props.optionLabel] ?? "")
        : String(option ?? "");
    return { label, value: option };
  });
});

const model = computed({
  get: () => props.modelValue ?? [],
  set: (value: any[]) => emit("update:modelValue", value),
});
</script>

<template>
  <USelect
    v-bind="attrs"
    v-model="model"
    :items="mappedItems"
    value-key="value"
    label-key="label"
    multiple
    :disabled="disabled"
  />
</template>
