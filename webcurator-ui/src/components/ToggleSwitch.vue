<script setup lang="ts">
defineProps<{
  modelValue?: boolean;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
}>();
</script>

<template>
  <label class="wct-toggle">
    <input
      type="checkbox"
      :checked="Boolean(modelValue)"
      :disabled="disabled"
      @change="emit('update:modelValue', ($event.target as HTMLInputElement).checked)"
    />
    <span class="wct-toggle__slider" />
  </label>
</template>

<style scoped>
.wct-toggle {
  position: relative;
  display: inline-flex;
}

.wct-toggle input {
  width: 0;
  height: 0;
  opacity: 0;
}

.wct-toggle__slider {
  width: 2.2rem;
  height: 1.2rem;
  background: #94a3b8;
  border-radius: 9999px;
  position: relative;
}

.wct-toggle__slider::after {
  content: "";
  position: absolute;
  left: 0.1rem;
  top: 0.1rem;
  width: 1rem;
  height: 1rem;
  border-radius: 9999px;
  background: #fff;
  transition: transform 0.15s ease;
}

.wct-toggle input:checked + .wct-toggle__slider {
  background: #2563eb;
}

.wct-toggle input:checked + .wct-toggle__slider::after {
  transform: translateX(1rem);
}
</style>
