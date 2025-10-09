<script setup lang="ts">
import { RouterView } from 'vue-router';
import LoginView from '@/views/login/LoginView.vue';
import NavBar from '@/components/NavBar.vue';
import { useAuthStore } from '@/utils/rest.api';
import { progressVisible } from '@/utils/progress';
const authStore = useAuthStore();
</script>

<template>
  <div v-if="authStore.isAuthenticating" id="login-dialog">
    <LoginView />
  </div>
  <div>
    <NavBar />
    <div class="main-container">
      <router-view />
    </div>
  </div>

  <!-- <ConfirmDialog v-model:visible="progress.visible" group="progress">
    <template #container="{ message, acceptCallback, rejectCallback }">
      <div class="flex items-center justify-center">
        <ProgressSpinner />
      </div>
    </template>
  </ConfirmDialog> -->
  <div v-if="progressVisible" class="flex items-center justify-center progress-bar" pt:mask:class="backdrop-blur-sm">
    <ProgressSpinner />
  </div>
</template>

<style>
/* .p-confirmdialog {
  background: transparent !important;
  box-shadow: none !important;
  background-color: transparent !important;
  border: 0;
} */
#login-dialog {
  width: 100vw;
  height: 100vh;
  background: var(--p-content-background);
  position: fixed;
  z-index: 9999;
}

.progress-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  height: calc(100vh - 80px);
  width: 100vw;
  z-index: 10;
}
</style>
