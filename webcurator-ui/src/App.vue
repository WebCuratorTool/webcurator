<script setup lang="ts">
import { RouterView } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

// import api from './restclient';
</script>

<template>
  <div class="app">
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
</style>
