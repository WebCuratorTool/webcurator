<script setup lang="ts">
import { RouterView } from 'vue-router';
import LoginDialog from '@/views/login/LoginDialog.vue';
import NavBar from '@/components/NavBar.vue';
import { useApiAuthStore } from '@/utils/rest.api';
const authStore = useApiAuthStore();
</script>

<template>
  <LoginDialog v-if="authStore.isAuthenticating" id="login-dialog" />
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
  position: fixed;
  z-index: 9999;
}
</style>
