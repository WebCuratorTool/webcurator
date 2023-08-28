<template>
    <p class="heading">Base Profile</p>
    <div class="d-flex justify-content-between w-50">
        <div class="d-flex justify-content-between w-25">
            <p class="me-1 text-muted">Harvester Type</p>
            <p class="text-dark">{{ profile.harvesterType }}</p>
        </div>
        <div class="d-flex justify-content-between w-25">
            <p class="me-1 text-muted">Base Profile</p>
            <p class="text-dark">{{ profile.name}}</p>
        </div>
    </div>
    <Table :fields="fields" :data="profile.overrides"></Table>

</template>

<script setup lang="ts">
import type { Profile } from '@/types/target'
import Table from '@/components/Table.vue'

defineProps<{
    profile: Profile;
}>()

const fields = [
  {display: 'Profile Element', value:'id'}, 
  {display: 'Override Value', value: 'value'},
  {display: 'Enable Override', value: 'enabled'},
]
</script>

<style>
    .heading {
        font-weight: bold;
        font-size: medium;
    }
</style>