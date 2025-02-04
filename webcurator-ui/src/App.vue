<script setup lang="ts">
import NavBar from '@/components/'
import { RouterView } from 'vue-router'
import LoginDialog from './components/LoginDialog.vue'
import { useLoginStore } from './utils/rest.api'
const loginStore = useLoginStore()

// import api from './restclient';
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
