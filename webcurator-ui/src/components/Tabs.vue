<script setup lang="ts">
import { provide, ref, watch } from "vue";

import { tabsKey } from "./TabState";

const props = defineProps<{ value?: string | number }>();
const emit = defineEmits<{ "update:value": [value: string] }>();

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
    emit("update:value", nextValue);
  },
});
</script>

<template>
  <UTabs v-model="current" :items="[]" :content="false" class="wct-tabs">
    <template #default>
      <slot />
    </template>
  </UTabs>
</template>
