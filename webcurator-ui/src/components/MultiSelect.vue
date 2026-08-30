<script setup lang="ts">
const props = defineProps<{
  modelValue?: any[];
  options?: any[];
  optionLabel?: string;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: any[]];
}>();

const labelOf = (option: any) => {
  if (props.optionLabel && option && typeof option === "object") {
    return String(option[props.optionLabel] ?? "");
  }
  return String(option ?? "");
};

const isSelected = (option: any) =>
  Array.isArray(props.modelValue) ? props.modelValue.includes(option) : false;

const toggle = (option: any, checked: boolean) => {
  const values = Array.isArray(props.modelValue) ? [...props.modelValue] : [];
  const idx = values.findIndex((value) => value === option);

  if (checked && idx < 0) {
    values.push(option);
  }
  if (!checked && idx >= 0) {
    values.splice(idx, 1);
  }

  emit("update:modelValue", values);
};
</script>

<template>
  <div class="wct-multiselect" :aria-disabled="disabled">
    <label
      v-for="(option, index) in options || []"
      :key="index"
      class="wct-multiselect__option"
    >
      <input
        type="checkbox"
        :disabled="disabled"
        :checked="isSelected(option)"
        @change="toggle(option, ($event.target as HTMLInputElement).checked)"
      />
      <span>{{ labelOf(option) }}</span>
    </label>
  </div>
</template>

<style scoped>
.wct-multiselect {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 0.5rem;
  padding: 0.35rem 0.65rem;
  max-height: 12rem;
  overflow: auto;
}

.wct-multiselect__option {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  padding: 0.2rem 0;
}
</style>
