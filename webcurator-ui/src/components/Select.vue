<script setup lang="ts">
import { computed } from "vue";

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

const normalizedOptions = computed(() => props.options ?? []);

const selectedIndex = computed(() => {
  return normalizedOptions.value.findIndex((option) => option === props.modelValue);
});

const labelOf = (option: any) => {
  if (props.optionLabel && option && typeof option === "object") {
    return String(option[props.optionLabel] ?? "");
  }
  return String(option ?? "");
};

const onChange = (event: Event) => {
  const rawIndex = Number((event.target as HTMLSelectElement).value);
  const value = rawIndex >= 0 ? normalizedOptions.value[rawIndex] : null;
  emit("update:modelValue", value);
  emit("change", { value });
};
</script>

<template>
  <select
    class="wct-input"
    :value="selectedIndex"
    :disabled="disabled"
    @change="onChange"
  >
    <option v-if="showClear || placeholder" :value="-1">{{ placeholder || "Select" }}</option>
    <option
      v-for="(option, index) in normalizedOptions"
      :key="index"
      :value="index"
    >
      {{ labelOf(option) }}
    </option>
  </select>
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
