<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
</script>

<template>
  <div class="app">
    <NavBar />
    <router-view />
  </div>
</template>

<style scoped>
.app {
  height: 100vh;
  width: 100%;
}
</style>
