<template>
    
        <div class="col-2">
            <label>{{ label }}:</label>
        </div>
        <div class="col-10">
            <slot></slot>
        </div>
</template>

<script setup lang="ts">
defineProps<{label: string}>()
</script>

<style>
.col-2, .col-10{
    padding: 15px;
}
</style>