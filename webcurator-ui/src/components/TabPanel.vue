<script setup lang="ts">
import { inject } from "vue";

import { tabsKey } from "./TabState";

const props = defineProps<{ value: string | number }>();
const tabs = inject(tabsKey);
</script>

<template>
  <section v-show="tabs?.value.value === String(props.value)">
    <slot />
  </section>
</template>
