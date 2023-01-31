<script setup lang="ts">
// import { RouterLink, RouterView } from 'vue-router'
import HelloWorld from './components/HelloWorld.vue'
</script>

<template>
  <table style="width:100vw;">
  <tr>
    <td id="col-sidebar">
      <div class="sidebar">
        <img class="logo" src="./assets/wct_logo.png" height="75"/>
        <nav>
          <router-link to="/"><i class="bi bi-house"></i>&emsp;Home</router-link>
          <router-link to="/dashboard"><i class="bi bi-grid"></i>&emsp;Dashboard</router-link>
          <router-link to="/queue"><i class="bi bi-stickies-fill"></i>&emsp;Queue</router-link>
          <router-link to="/harvest-authorisations"><i class="fa-solid fa-file-circle-check"></i>&emsp;Harvest Authorisations</router-link>
          <router-link to="/target"><i class="bi bi-bullseye"></i>&emsp;Targets</router-link>
          <router-link to="/groups"><i class="fa-solid fa-layer-group"></i>&emsp;Groups</router-link>
          <router-link to="/management"><i class="fa-solid fa-user-gear"></i>&emsp;Management</router-link>
        </nav>
      </div>
    </td>
    <td id="col-content">
      <!-- <RouterView/> -->
      <router-view></router-view>
    </td>
  </tr>
  </table>
</template>

<style scoped>
header {
  line-height: 1.5;
  max-height: 100vh;
}

table, tr, td{
  border: 0;
  margin: 0;
  padding: 0;
  vertical-align: top;
}

#col-sidebar{
  width: 15%;
  height: 100vh;
  background: #1F5384;
}

#col-content{
  width: 85%;
  height: 100vh;
  overflow-x: hidden;
  overflow-y: auto;
  /* padding: 0 5em; */
}

.sidebar .logo{
  margin: 2em 0;
}

nav a {
  display: block;
  padding: 1em  1rem;
  border-left: 1px solid var(--color-border);
  color: #E4F0F5;
  font-size: 1.2em;
  text-decoration: none;
}

nav a:hover{
  background: #2D6EA9;
  color: #E3EBF3;
}

nav a.router-link-exact-active {
  background: #2D6EA9;
  color: #E3EBF3;
}

nav a.router-link-exact-active:hover {
  background: #2D6EA9;
  color: #E3EBF3;
}



</style>
