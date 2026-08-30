<script setup lang="ts"></script>

<template>
  <UCard>
    <template v-if="$slots.header || $slots.icons" #header>
      <div class="wct-panel__header">
        <slot name="header" />
        <div v-if="$slots.icons" class="wct-panel__icons">
          <slot name="icons" />
        </div>
      </div>
    </template>

    <div class="wct-panel__body">
      <slot />
    </div>

    <template v-if="$slots.footer" #footer>
      <div class="wct-panel__footer">
      <slot name="footer" />
      </div>
    </template>
  </UCard>
</template>

<style scoped>
.wct-panel__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
