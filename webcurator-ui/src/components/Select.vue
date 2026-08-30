<script setup lang="ts">
import { computed, useAttrs } from "vue";

const props = defineProps<{
  modelValue?: any;
  options?: any[];
  optionLabel?: string;
  placeholder?: string;
  disabled?: boolean;
  showClear?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: any];
  change: [event: { value: any }];
}>();

const attrs = useAttrs();

const normalizedOptions = computed(() => props.options ?? []);

const mappedItems = computed(() => {
  const items = normalizedOptions.value.map((option) => {
    const label =
      props.optionLabel && option && typeof option === "object"
        ? String(option[props.optionLabel] ?? "")
        : String(option ?? "");

    return {
      label,
      value: option,
    };
  });

  if (props.showClear || props.placeholder) {
    items.unshift({
      label: props.placeholder || "Select",
      value: null,
    });
  }

  return items;
});

const model = computed({
  get: () => props.modelValue,
  set: (value: any) => {
    emit("update:modelValue", value);
    emit("change", { value });
  },
});

const onChange = () => {
  const value = model.value;
  emit("update:modelValue", value);
  emit("change", { value });
};
</script>

<template>
  <USelect
    v-bind="attrs"
    v-model="model"
    :items="mappedItems"
    value-key="value"
    label-key="label"
    :placeholder="placeholder"
    :disabled="disabled"
    @change="onChange"
  />
</template>
