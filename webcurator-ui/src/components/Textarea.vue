<script setup lang="ts">
const props = defineProps<{
  modelValue?: string | null;
  rows?: number;
  cols?: number;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();
</script>

<template>
  <textarea
    class="wct-textarea"
    :rows="rows"
    :cols="cols"
    :value="props.modelValue ?? ''"
    :disabled="disabled"
    @input="emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
  />
</template>

<style scoped>
.wct-textarea {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 0.5rem;
  padding: 0.5rem 0.65rem;
}
</style>
