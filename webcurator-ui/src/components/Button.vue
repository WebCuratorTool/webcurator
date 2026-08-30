<script setup lang="ts">
defineProps<{
  label?: string;
  icon?: string;
  iconPos?: "left" | "right";
  text?: boolean;
  outlined?: boolean;
  rounded?: boolean;
  severity?: string;
  disabled?: boolean;
  type?: "button" | "submit" | "reset";
  fluid?: boolean;
}>();

defineEmits<{
  click: [event: MouseEvent];
}>();
</script>

<template>
  <button
    :type="type || 'button'"
    :disabled="disabled"
    class="wct-btn"
    :class="[
      { 'w-full': fluid, 'wct-btn--text': text, 'wct-btn--outlined': outlined, 'rounded-full': rounded },
      severity ? `wct-btn--${severity}` : 'wct-btn--primary',
    ]"
    @click="$emit('click', $event)"
  >
    <i v-if="icon && (!iconPos || iconPos === 'left')" :class="icon" />
    <slot>{{ label }}</slot>
    <i v-if="icon && iconPos === 'right'" :class="icon" />
  </button>
</template>

<style scoped>
.wct-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  min-height: 2.25rem;
  padding: 0.45rem 0.85rem;
  border-radius: 0.5rem;
  border: 1px solid transparent;
  cursor: pointer;
  font-weight: 600;
}

.wct-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.wct-btn--primary {
  background: #2563eb;
  color: #fff;
}

.wct-btn--secondary {
  background: #475569;
  color: #fff;
}

.wct-btn--danger {
  background: #dc2626;
  color: #fff;
}

.wct-btn--text {
  background: transparent;
  color: inherit;
}

.wct-btn--outlined {
  background: transparent;
  border-color: #94a3b8;
  color: #334155;
}
</style>
