<template>
    <div class="d-flex mb-4">
        <input class="w-100" :value="searchTerms.targetId" @input="event => searchTerms.targetId = event.target.value" />
        <button class="button-black ms-2" @click="onSearch">Search</button>
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