<script setup lang="ts">
import { RouterView } from 'vue-router';
import { togglePreset, toggleThemeMode } from './utils/themes';
import IconWarning from '@/components/icons/IconWarning.vue';

toggleThemeMode('light');
togglePreset('indigo');
</script>

<template>
  <div class="app">
    <router-view />
  </div>
  <DynamicDialog />
  <Toast group="toast-info" position="bottom-left">
    <template #message="slotProps">
      <div class="flex flex-col items-start flex-auto">
        <div class="flex items-center gap-2">
          <i class="pi pi-info-circle" />
          <span class="font-bold">{{ slotProps.message.summary }}</span>
        </div>
        <div class="p-2">{{ slotProps.message.detail }}</div>
      </div>
    </template>
  </Toast>
  <Toast group="toast-error" position="bottom-left">
    <template #message="slotProps">
      <div class="flex flex-col items-start flex-auto">
        <div class="flex items-center gap-2">
          <i class="pi pi-exclamation-triangle" />
          <span class="font-bold">{{ slotProps.message.summary }}</span>
        </div>
        <div class="font-medium text-lg my-4">{{ slotProps.message.detail }}</div>
      </div>
    </template>
  </Toast>
  <ConfirmDialog group="dlg-error">
    <template #container="{ message, acceptCallback, rejectCallback }">
      <Panel>
        <template #header>
          <div class="flex items-center gap-2">
            <span class="font-bold">{{ message.header }}</span>
          </div>
        </template>
        <template #footer>
          <div class="flex flex-col items-center justify-center w-full">
            <Divider />
            <Button label="OK" @click="acceptCallback" class="w-32" severity="secondary"></Button>
          </div>
        </template>
        <template #icons>
          <Button icon="pi pi-times" severity="secondary" rounded text @click="rejectCallback" />
        </template>
        <div class="flex items-center gap-2">
          <IconWarning style="width: 30px; height: 30px" />
          <div style="min-width: 30rem; max-width: 50rem">
            <p class="m-0">{{ message.message }}</p>
          </div>
        </div>
      </Panel>
    </template>
  </ConfirmDialog>
</template>

<style>
.app {
  height: 100vh;
  width: 100%;
}
</style>
