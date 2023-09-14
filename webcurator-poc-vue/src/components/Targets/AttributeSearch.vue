<template>
    <div class="d-flex">
        <input :value="searchTerms.targetId" @input="event => searchTerms.targetId = event.target.value" />
        <button @click="onSearch">Search</button>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits(['on-search'])

const onSearch = () => {
    emit('on-search', searchTerms.value)
}

const searchTerms = ref({
    targetId: ''
})
</script>