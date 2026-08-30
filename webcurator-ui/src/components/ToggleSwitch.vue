<script setup lang="ts">
import { computed, useAttrs } from "vue";

const props = defineProps<{
  modelValue?: boolean;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
}>();

const attrs = useAttrs();

const model = computed({
  get: () => Boolean(props.modelValue),
  set: (value: boolean) => emit("update:modelValue", value),
});
</script>

<template>
  <USwitch v-bind="attrs" v-model="model" :disabled="disabled" />
</template>
