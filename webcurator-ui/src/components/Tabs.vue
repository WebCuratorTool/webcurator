<script setup lang="ts">
import { provide, ref, watch } from "vue";

import { tabsKey } from "./TabState";

const props = defineProps<{ value?: string | number }>();

const current = ref(String(props.value ?? "0"));

watch(
  () => props.value,
  (nextValue) => {
    current.value = String(nextValue ?? "0");
  },
);

provide(tabsKey, {
  value: current,
  setValue(nextValue: string) {
    current.value = nextValue;
  },
});
</script>

<template>
  <div class="wct-tabs">
    <slot />
  </div>
</template>
