<template>
  <div v-show="isActive"><slot></slot></div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'Tab',
  props: {
    name: { type: String, required: true },
    selected: { type: Boolean, default: false },
  },
  data() {
    return {
      isActive: false,
    };
  },
  mounted() {
    this.isActive = this.selected;
  },
  created() {
    const parent = this.$parent as any; // This assumes the parent has the `tabs` property
    if (parent.tabs) {
      parent.tabs.push(this);
    }
  },
});
</script>

<style scoped>
/* Add your scoped styles here */
</style>
