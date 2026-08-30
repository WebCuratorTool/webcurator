<script setup lang="ts">
import { computed } from "vue";

import { confirmState, resolveConfirm } from "@/composables/uiBridge";

const props = defineProps<{ group?: string }>();

const activeMessage = computed(() => {
  const message = confirmState.active;
  if (!message) {
    return null;
  }
  if (props.group && message.group !== props.group) {
    return null;
  }
  return message;
});
</script>

<template>
  <div v-if="activeMessage" class="wct-modal-backdrop">
    <div class="wct-modal-card">
      <slot
        name="container"
        :message="activeMessage"
        :acceptCallback="() => resolveConfirm(true)"
        :rejectCallback="() => resolveConfirm(false)"
      >
        <h3>{{ activeMessage.header || "Confirm" }}</h3>
        <p>{{ activeMessage.message }}</p>
        <div class="wct-modal-actions">
          <Button label="Cancel" @click="resolveConfirm(false)" />
          <Button label="OK" @click="resolveConfirm(true)" />
        </div>
      </slot>
    </div>
  </div>
</template>

<style scoped>
.wct-modal-backdrop {
  position: fixed;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(2, 6, 23, 0.3);
  z-index: 1300;
}

.wct-modal-card {
  width: min(36rem, 92vw);
  background: #fff;
  border-radius: 0.75rem;
  padding: 1rem;
  border: 1px solid #cbd5e1;
}

.wct-modal-actions {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
</style>
