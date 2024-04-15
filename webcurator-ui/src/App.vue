<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { useDialog } from 'primevue/usedialog';
import NavBar from '@/components/NavBar.vue'

// import api from './restclient';


</script>

<template>
  <div class="app">
    <NavBar />
    <router-view />
     <DynamicDialog />
  </div>
</template>

<style scoped>
.app {
  height: 100vh;
  width: 100%;
}
</style>
