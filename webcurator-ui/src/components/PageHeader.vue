<template>
    <div class="page-header">
        <p>{{ title }}</p>
    </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
}>()
</script>

<style>
.page-header {
	height: 5em;
	background: #dfe1e2;
	color: #1F5384;
	font-size: xx-large;
	font-weight: bold;
	text-align: left;
	margin: 0;
	padding: 0 120px;
	display: flex;
	align-items: center;
}
</style>