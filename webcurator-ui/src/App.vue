<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { useDialog } from 'primevue/usedialog';
import NavBar from '@/components/NavBar.vue'
import AppConfig from '@/layout/AppConfig.vue';

// import api from './restclient';


</script>

<template>
  <div class="app">
     <NavBar />
     <div class="main-container">
      <router-view />
     </div>
     
     <DynamicDialog />
     <AppConfig />
  </div>
</template>

<style scoped>
.app {
  height: 100vh;
  width: 100%;
}
</style>
