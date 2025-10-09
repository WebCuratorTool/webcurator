<script setup lang="ts">
import { RouterView } from 'vue-router';
import LoginView from '@/views/login/LoginView.vue';
import NavBar from '@/components/NavBar.vue';
import { useAuthStore } from '@/utils/rest.api';
import { useProgressStore } from '@/utils/progress';
const authStore = useAuthStore();
const progress = useProgressStore();
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

  <!-- pt:mask:class="backdrop-blur-sm" -->
  <ConfirmDialog v-model:visible="progress.visible" pt:mask:class="backdrop-blur-sm">
    <template #container="{ message, acceptCallback, rejectCallback }">
      <div class="flex items-center justify-center p-8">
        <ProgressSpinner />
        <!-- <i class="pi pi-spin pi-spinner" style="font-size: 6rem"></i> -->
      </div>
    </template>
  </ConfirmDialog>
</template>

<style>
#login-dialog {
  width: 100vw;
  height: 100vh;
  background: var(--p-content-background);
  position: fixed;
  z-index: 9999;
}
</style>
