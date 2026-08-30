<script setup lang="ts">
import { computed, useAttrs } from "vue";

const props = defineProps<{
  first?: number;
  rows?: number;
  totalRecords?: number;
}>();

const emit = defineEmits<{
  page: [event: { page: number }];
}>();

const attrs = useAttrs();

const rows = computed(() => props.rows ?? 10);
const currentPage = computed(() => Math.floor((props.first ?? 0) / rows.value) + 1);
const totalPages = computed(() => {
  const total = props.totalRecords ?? 0;
  return Math.max(Math.ceil(total / rows.value), 1);
});

const onUpdatePage = (page: number) => emit("page", { page: page - 1 });
</script>

<template>
  <div class="wct-paginator">
    <UPagination
      v-bind="attrs"
      :page="currentPage"
      :items-per-page="rows"
      :total="totalRecords ?? 0"
      @update:page="onUpdatePage"
    />
    <span>Page {{ currentPage + 1 }} / {{ totalPages }}</span>
  </div>
</template>

<style scoped>
.wct-paginator {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
</style>
