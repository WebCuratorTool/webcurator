<script setup lang="ts">
import { computed, useAttrs } from "vue";

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

const attrs = useAttrs();

const checked = computed(() => props.modelValue === props.value);

const onUpdate = (nextState: boolean | "indeterminate") => {
  if (nextState === true) {
    emit("update:modelValue", props.value);
  }
};
</script>

<template>
  <UCheckbox
    v-bind="attrs"
    :id="inputId"
    :name="name"
    :model-value="checked"
    :disabled="disabled"
    @update:model-value="onUpdate"
  />
</template>
