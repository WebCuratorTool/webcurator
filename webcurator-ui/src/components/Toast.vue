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
    <UAlert
      v-for="message in messages"
      :key="message.id"
      :color="message.severity === 'warn' || message.severity === 'error' ? 'warning' : 'info'"
      variant="soft"
      class="wct-toast"
    >
      <template #description>
        <slot name="message" :message="message">
          <strong>{{ message.summary }}</strong>
          <div>{{ message.detail }}</div>
        </slot>
      </template>
    </UAlert>
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
}
</style>
