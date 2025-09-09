<script setup lang="ts">
import { RouterView } from 'vue-router';
import LoginView from '@/views/login/LoginView.vue';
import NavBar from '@/components/NavBar.vue';
import { useAuthStore } from '@/utils/rest.api';
import { ref } from 'vue';
import { useToast } from 'primevue';
const authStore = useAuthStore();

const toast = useToast();
const visible = ref(true);
const onReply = () => {
  toast.removeGroup('bc');
  visible.value = false;
};

const onClose = () => {
  visible.value = false;
};
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
