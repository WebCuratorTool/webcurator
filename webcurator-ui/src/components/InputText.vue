<script setup lang="ts">
import { computed, useAttrs } from "vue";

const props = defineProps<{
  modelValue?: string | number | null;
  type?: string;
  placeholder?: string;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

const attrs = useAttrs();

const model = computed({
  get: () => String(props.modelValue ?? ""),
  set: (value: string) => emit("update:modelValue", value),
});
</script>

<template>
  <UInput
    v-bind="attrs"
    v-model="model"
    :type="type || 'text'"
    :placeholder="placeholder"
    :disabled="disabled"
  />
</template>
