<template>
  <span class="wct-spinner" aria-label="loading" />
</template>

<style scoped>
.wct-spinner {
  display: inline-block;
  width: 2rem;
  height: 2rem;
  border: 3px solid #cbd5e1;
  border-top-color: #2563eb;
  border-radius: 9999px;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
