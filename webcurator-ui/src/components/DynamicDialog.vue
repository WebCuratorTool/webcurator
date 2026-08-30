<script setup lang="ts">
import { computed, provide } from "vue";

import { closeDialog, dialogState } from "@/composables/uiBridge";
import type { DynamicDialogInstance } from "@/types/ui";

const dialogRef = computed<DynamicDialogInstance>(() => ({
  data: dialogState.value?.options.data,
  close: () => closeDialog(),
}));

provide("dialogRef", dialogRef);

const open = computed({
  get: () => Boolean(dialogState.value),
  set: (nextValue: boolean) => {
    if (!nextValue && dialogState.value) {
      closeDialog();
    }
  },
});
</script>

<template>
  <UModal
    v-model:open="open"
    :dismissible="dialogState?.options.props?.dismissableMask !== false"
    :close="dialogState?.options.props?.closable !== false"
  >
    <template #header>
      <h3>{{ dialogState?.options.props?.header || "Dialog" }}</h3>
    </template>
    <template #body>
      <div :style="dialogState?.options.props?.style as any">
        <component v-if="dialogState" :is="dialogState.component" />
      </div>
    </template>
  </UModal>
</template>

<style scoped>
</style>
