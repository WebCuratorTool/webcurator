<script setup lang="ts">
import { RouterView } from 'vue-router';
import LoginDialog from './components/LoginDialog.vue';
import NavBar from './components/NavBar.vue';
import { useLoginStore } from './utils/rest.api';
import { togglePreset, toggleThemeMode } from './utils/themes';
// import { togglePreset, toggleThemeMode } from './utils/themes';

const loginStore = useLoginStore();
toggleThemeMode('light');
togglePreset('purple');
</script>

<template>
  <LoginDialog v-if="loginStore.visibleLoginWindow" id="login-dialog" />

  <div v-show="!loginStore.visibleLoginWindow" class="app">
    <NavBar />
    <div class="main-container">
      <router-view />
    </div>

    <DynamicDialog />
  </div>
</template>

<style scoped>
.app {
  height: 100vh;
  width: 100%;
}
#login-dialog {
  position: fixed;
  z-index: 9999;
}
</style>
