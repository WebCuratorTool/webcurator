<script setup lang="ts">
import { RouterView } from 'vue-router';
import LoginView from '@/views/login/LoginView.vue';
import NavBar from '@/components/NavBar.vue';
import { useAuthStore } from '@/utils/rest.api';
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
    <DynamicDialog />
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
</style>
