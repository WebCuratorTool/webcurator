<script setup lang="ts">
import { computed, inject } from "vue";

import { tabsKey } from "./TabState";

const props = defineProps<{
  value: string | number;
  disabled?: boolean;
}>();

const tabs = inject(tabsKey);
const isActive = computed(() => tabs?.value.value === String(props.value));
</script>

<template>
  <UButton
    variant="ghost"
    color="neutral"
    :disabled="disabled"
    class="wct-tab"
    :class="{ 'wct-tab--active': isActive }"
    @click="tabs?.setValue(String(props.value))"
  >
    <slot />
  </UButton>
</template>

<style scoped>
.wct-tab {
  padding: 0.5rem 0.75rem;
  border: 0;
  border-bottom: 2px solid transparent;
  background: transparent;
  cursor: pointer;
}

.wct-tab--active {
  border-bottom-color: #2563eb;
  color: #2563eb;
  font-weight: 700;
}
</style>
