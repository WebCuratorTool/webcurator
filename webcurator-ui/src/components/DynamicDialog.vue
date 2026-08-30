<script setup lang="ts">
import { computed, provide } from "vue";

import { closeDialog, dialogState } from "@/composables/uiBridge";
import type { DynamicDialogInstance } from "@/types/ui";

const dialogRef = computed<DynamicDialogInstance>(() => ({
  data: dialogState.value?.options.data,
  close: () => closeDialog(),
}));

provide("dialogRef", dialogRef);
</script>

<template>
  <div v-if="dialogState" class="wct-modal-backdrop">
    <div class="wct-modal-card" :style="dialogState.options.props?.style as any">
      <header class="wct-modal-header">
        <h3>{{ dialogState.options.props?.header || "Dialog" }}</h3>
        <Button
          v-if="dialogState.options.props?.closable !== false"
          label="Close"
          text
          @click="closeDialog"
        />
      </header>
      <component :is="dialogState.component" />
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
  z-index: 1400;
}

.wct-modal-card {
  width: min(48rem, 92vw);
  max-height: 92vh;
  overflow: auto;
  background: #fff;
  border-radius: 0.75rem;
  padding: 1rem;
  border: 1px solid #cbd5e1;
}

.wct-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}
</style>
