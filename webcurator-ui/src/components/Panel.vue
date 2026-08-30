<template>
  <section class="wct-panel">
    <header v-if="$slots.header" class="wct-panel__header">
      <slot name="header" />
      <div v-if="$slots.icons" class="wct-panel__icons">
        <slot name="icons" />
      </div>
    </header>
    <div class="wct-panel__body">
      <slot />
    </div>
    <footer v-if="$slots.footer" class="wct-panel__footer">
      <slot name="footer" />
    </footer>
  </section>
</template>

<style scoped>
.wct-panel {
  border: 1px solid #cbd5e1;
  border-radius: 0.75rem;
  background: #fff;
}

.wct-panel__header,
.wct-panel__footer,
.wct-panel__body {
  padding: 0.75rem;
}

.wct-panel__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e2e8f0;
}
</style>
