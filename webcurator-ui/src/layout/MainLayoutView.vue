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

  <div v-if="progress.visible" class="flex items-center justify-center p-progress-dialog" pt:mask:class="backdrop-blur-sm">
    <!-- <ProgressSpinner style="width: 50px; height: 50px" strokeWidth="8" fill="transparent" animationDuration=".5s" aria-label="Custom ProgressSpinner" /> -->
    <div class="card">
      <ProgressSpinner />
    </div>
  </div>
</template>

<style>
#login-dialog {
  width: 100vw;
  height: 100vh;
  background: var(--p-content-background);
  position: fixed;
  z-index: 9999;
}

.p-progress-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.4);
  z-index: 10 !important;
}
</style>
