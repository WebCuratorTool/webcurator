<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  first?: number;
  rows?: number;
  totalRecords?: number;
}>();

const emit = defineEmits<{
  page: [event: { page: number }];
}>();

const currentPage = computed(() => Math.floor((props.first ?? 0) / (props.rows ?? 10)));
const totalPages = computed(() => {
  const rows = props.rows ?? 10;
  const total = props.totalRecords ?? 0;
  return Math.max(Math.ceil(total / rows), 1);
});

const goTo = (page: number) => {
  if (page < 0 || page >= totalPages.value) {
    return;
  }
  emit("page", { page });
};
</script>

<template>
  <div class="wct-paginator">
    <Button :disabled="currentPage <= 0" label="Prev" @click="goTo(currentPage - 1)" />
    <span>Page {{ currentPage + 1 }} / {{ totalPages }}</span>
    <Button
      :disabled="currentPage >= totalPages - 1"
      label="Next"
      @click="goTo(currentPage + 1)"
    />
  </div>
</template>

<style scoped>
.wct-paginator {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
</style>
