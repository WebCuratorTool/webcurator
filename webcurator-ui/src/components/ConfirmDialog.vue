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

const open = computed({
  get: () => Boolean(activeMessage.value),
  set: (nextValue: boolean) => {
    if (!nextValue && activeMessage.value) {
      resolveConfirm(false);
    }
  },
});
</script>

<template>
  <UModal v-model:open="open" :dismissible="false">
    <template #body>
      <slot
        v-if="activeMessage"
        name="container"
        :message="activeMessage"
        :acceptCallback="() => resolveConfirm(true)"
        :rejectCallback="() => resolveConfirm(false)"
      >
        <h3>{{ activeMessage?.header || "Confirm" }}</h3>
        <p>{{ activeMessage?.message }}</p>
        <div class="wct-modal-actions">
          <Button label="Cancel" @click="resolveConfirm(false)" />
          <Button label="OK" @click="resolveConfirm(true)" />
        </div>
      </slot>
    </template>
  </UModal>
</template>

<style scoped>
.wct-modal-actions {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
</style>
