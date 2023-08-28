<template>
    <p class="heading">References</p>
    <div class="d-flex justify-content-between w-50">
        <div class="d-flex justify-content-between w-25">
            <p class="me-1 text-muted">ID</p>
            <p class="text-dark">{{ general.id }}</p>
        </div>
        <div class="d-flex justify-content-between w-25">
            <p class="me-1 text-muted">Reference number</p>
            <p class="text-dark">{{ general.referenceNumber }}</p>
        </div>
    </div>

    <div class="d-flex justify-content-between w-50">
        <div class="d-flex justify-content-between w-25">
            <p class="me-1 text-muted">Name</p>
            <p class="text-dark">{{ general.name }}</p>
        </div>
        <div class="d-flex justify-content-between w-25">
            <p class="me-1 text-muted">Owner</p>
            <p class="text-dark">{{ general.owner }}</p>
        </div>
    </div>

    <Table :fields="fields" :data="seeds"></Table>

</template>

<script setup lang="ts">
import type { General, Seed } from '@/types/target'
import Table from '@/components/Table.vue'

defineProps<{
    general: General,
    seeds: Seed[]
}>()

const fields = [
  {display: 'ID', value:'id'}, 
  {display: 'Seed', value: 'seed'},
]
</script>

<style>
    .heading {
        font-weight: bold;
        font-size: medium;
    }
</style>