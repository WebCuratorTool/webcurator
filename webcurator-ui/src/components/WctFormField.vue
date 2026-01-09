<script setup lang="ts">
defineProps<{
  label: string;
  checkbox?: boolean;
  inputId?: string;
}>();
</script>
<template>
  <div class="flex items-start justify-start gap-2 w-full">
    <div class="wct-form-col-left">
      <label :for="inputId">{{ label }}:</label>
    </div>
    <div class="wct-form-col-right">
      <Fluid>
        <slot></slot>
      </Fluid>
    </div>
  </div>
</template>
<style lang="scss">
.wct-form-field {
  padding: 0.5rem;
  text-align: left;
}
.wct-form-col-left {
  @extend .wct-form-field;
  width: 30%;
}
.wct-form-col-right {
  @extend .wct-form-field;
  width: 70%;
}

// label {
//   width: 100%;
//   display: inline-block;
//   position: relative;
//   text-align: left;
//   margin-right: 0;
// }
</style>
