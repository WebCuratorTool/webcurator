<template>
  <span>
    <table style="width:100vw;">
      <tr>
        <td id="col-sidebar">
          <div class="sidebar">
            <img class="logo" src="./assets/wct_logo.png" height="75"/>
            <nav v-show="isLoggedIn">
              <router-link to="/"><i class="bi bi-grid"></i>&emsp;Dashboard</router-link>
              <router-link to="/queue"><i class="bi bi-stickies-fill"></i>&emsp;Queue</router-link>
              <router-link to="/harvest-authorisations"><i class="fa-solid fa-file-circle-check"></i>&emsp;Harvest Authorisations</router-link>
              <router-link to="/targets"><i class="bi bi-bullseye"></i>&emsp;Targets</router-link>
              <router-link to="/groups"><i class="fa-solid fa-layer-group"></i>&emsp;Groups</router-link>
              <router-link to="/management"><i class="fa-solid fa-user-gear"></i>&emsp;Management</router-link>
            </nav>
          </div>
        </td>
        <td id="col-content">
          <router-view />
        </td>
      </tr>
    </table>
  </span>
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/user'

const { isLoggedIn } = storeToRefs(useAuthStore())
</script>

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
