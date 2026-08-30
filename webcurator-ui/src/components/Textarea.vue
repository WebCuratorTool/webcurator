<script setup lang="ts">
import { computed, useAttrs } from "vue";

const props = defineProps<{
  modelValue?: string | null;
  rows?: number;
  cols?: number;
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
  <UTextarea
    v-bind="attrs"
    v-model="model"
    :rows="rows"
    :cols="cols"
    :disabled="disabled"
  />
</template>
