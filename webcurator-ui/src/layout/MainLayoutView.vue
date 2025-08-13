<script setup lang="ts">
import { RouterView } from 'vue-router';
import LoginDialog from '@/views/login/LoginDialog.vue';
import NavBar from '@/components/NavBar.vue';
import { useApiAuthStore } from '@/utils/rest.api';
import { togglePreset, toggleThemeMode } from '@/utils/themes';
// import { togglePreset, toggleThemeMode } from './utils/themes';

const loginStore = useApiAuthStore();
toggleThemeMode('light');
togglePreset('indigo');
</script>

<template>
  <LoginDialog v-if="loginStore.isAuthenticating" id="login-dialog" />

  <div class="app">
    <NavBar />
    <div class="main-container">
      <router-view />
    </div>

    <DynamicDialog />
  </div>
</template>

<style>
.app {
  height: 100vh;
  width: 100%;
}
#login-dialog {
  position: fixed;
  z-index: 9999;
}

/* .main-container .p-button {
  background: #1f5384;
} */
</style>
