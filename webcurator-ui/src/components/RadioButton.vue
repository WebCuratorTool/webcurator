<script setup lang="ts">
const props = defineProps<{
  modelValue?: any;
  value?: any;
  inputId?: string;
  name?: string;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: any];
}>();
</script>

<template>
  <input
    :id="inputId"
    :name="name"
    type="radio"
    :checked="modelValue === value"
    :disabled="disabled"
    @change="emit('update:modelValue', value)"
  />
</template>
