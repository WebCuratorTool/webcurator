<script setup lang="ts">
const props = defineProps<{
  icon?: string;
  label?: string;
  class?: string;
  iconPos?: string;
}>();

const emit = defineEmits(["click"]);
const onClick = () => {
  emit("click", props);
};
</script>

<template>
  <Button
    :icon="icon"
    :label="label"
    :iconPos="iconPos"
    @click="onClick"
    class="wct-primary-button"
  />
</template>

<style scoped></style>
