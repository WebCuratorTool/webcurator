<script setup lang="ts">
import { computed } from "vue";

import { toastState } from "@/composables/uiBridge";

const props = defineProps<{ group?: string }>();

const messages = computed(() => {
  if (!props.group) {
    return toastState.items;
  }
  return toastState.items.filter((message) => message.group === props.group);
});
</script>

<template>
  <div class="wct-toast-stack">
    <article v-for="message in messages" :key="message.id" class="wct-toast" :class="`wct-toast--${message.severity || 'info'}`">
      <slot name="message" :message="message">
        <strong>{{ message.summary }}</strong>
        <div>{{ message.detail }}</div>
      </slot>
    </article>
  </div>
</template>

<style scoped>
.wct-toast-stack {
  position: fixed;
  top: 1rem;
  right: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  z-index: 1200;
}

.wct-toast {
  min-width: 18rem;
  border-radius: 0.6rem;
  border: 1px solid #93c5fd;
  background: #eff6ff;
  color: #1e3a8a;
  padding: 0.65rem 0.75rem;
}

.wct-toast--warn,
.wct-toast--warning,
.wct-toast--error {
  border-color: #fcd34d;
  background: #fffbeb;
  color: #78350f;
}
</style>
