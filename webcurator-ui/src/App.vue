<script setup lang="ts">
import { RouterView } from 'vue-router';
import { togglePreset, toggleThemeMode } from './utils/themes';
toggleThemeMode('light');
togglePreset('indigo');
</script>

<template>
  <div class="app">
    <router-view />
  </div>
</template>

<style>
.app {
  height: 100vh;
  width: 100vh;
}
</style>
