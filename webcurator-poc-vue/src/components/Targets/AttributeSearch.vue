<template>
    <div class="d-flex">
        <input :value="searchTerms.id" @input="event => searchTerms.id = event.target.value" />
        <button @click="onSearch">Search</button>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits(['on-search'])

const onSearch = () => {
    emit('on-search', searchTerms)
}

const searchTerms = ref({
    id: ''
})
</script>