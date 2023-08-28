<template>
  <div>
    <div class="tabs">
      <ul class='tabs_header'>
        <li v-for="tab in tabs" :class="{ 'tab_selected': tab.isActive }" @click="selectTab(tab)">
          <a>{{ tab.name }}</a>
        </li>
      </ul>
    </div>

    <div class="tabs-details">
      <slot></slot>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

interface Tab {
  name: string;
  href: string;
  isActive: boolean;
}

export default defineComponent({
  name: 'Tabs',
  data() {
    return { tabs: [] as Tab[] };
  },
  methods: {
    selectTab(selectedTab: Tab) {
      this.tabs.forEach(tab => {
        tab.isActive = tab.name === selectedTab.name;
      });
    },
  },
});
</script>

<style lang="css">

  ul.tabs_header {
    display: block;
    list-style: none;
    padding: 0;
  }

  ul.tabs_header > li {
    /* padding: 15px 30px; */
    font-size: 20px;
    border-bottom: 2px solid gray;
    margin: 0;
    display: inline-block;
    cursor: pointer;
  }

  ul.tabs_header > li > a {
    margin-right: 15px;
    margin-left: 15px;
    line-height: 40px;
  }

  ul.tabs_header > li.tab_selected {
    font-weight: bold;
    border-bottom: 3px solid black;
  }

  .tab {
    /* display: inline-block;
    color: black;
    padding: 20px;
    min-width: 800px;
    border-radius: 10px;
    min-height: 400px; */
  }

  .tabs .tab{
    /* background-color: #555;
    color: #eee; */
  }

  .tabs li {
    /* background-color: #ddd;
    color: #aaa; */
  }

  .tabs li.tab_selected {
    /* background-color: #555;
    color: white; */
  }

</style>