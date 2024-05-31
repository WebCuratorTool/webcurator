<template>
    <div class="grid mb-4">
        <div :class="checkbox ? 'col-4' : 'col-2'" style="padding: 0.5rem; text-align: left;">
            <label>{{ label }}:</label>
        </div>
        <div :class="checkbox ? 'col' : 'col-10'"  style="padding: 0.5rem;">
            <slot></slot>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    label: string,
    checkbox?: boolean
}>()
</script>

<style>
.col-2, .col-10{
    padding: 15px;
}

label{
    width: 100%;
    display: inline-block;
    position: relative;
    text-align: left;
    margin-right: 0;
}
</style>