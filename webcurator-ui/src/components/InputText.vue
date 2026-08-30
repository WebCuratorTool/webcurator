<script setup lang="ts">
const props = defineProps<{
  modelValue?: string | number | null;
  type?: string;
  placeholder?: string;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

const update = (event: Event) => {
  emit("update:modelValue", (event.target as HTMLInputElement).value);
};
</script>

<template>
  <input
    :type="type || 'text'"
    :value="props.modelValue ?? ''"
    :placeholder="placeholder"
    :disabled="disabled"
    class="wct-input"
    @input="update"
  />
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
