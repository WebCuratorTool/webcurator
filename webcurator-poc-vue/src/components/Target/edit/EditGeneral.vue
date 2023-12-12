<template>
    <p class="heading">Edit general features</p>
    <div class="w-50">
        <div class="d-flex justify-content-between w-25">
            <p class="me-1 text-muted">ID</p>
            <p class="text-dark">{{ general.id }}</p>
        </div>
        <div class="d-flex justify-content-between w-25">
            <p class="me-1 text-muted">Name*</p>
            <input :value="editedGeneral.name" @input="event => editedGeneral.name = event.target.value" />
        </div>
        <div class="d-flex justify-content-between w-25 mt-1">
            <p class="me-1 text-muted">Owner</p>
            <input :value="editedGeneral.owner" @input="event => editedGeneral.owner = event.target.value" />
        </div>
    </div>
    <div class="button-box">
        <button class="button-black" @click="onSave">Save</button>
        <button class="button-white" @click="onCancel">Cancel</button>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { General } from '@/types/target'

const props = defineProps<{
    general: General,
}>()

const editedGeneral = ref({
    name: props.general.name,
    owner: props.general.owner
})

const emit = defineEmits(['on-edit', 'on-save'])

const onCancel = () => {
    emit('on-edit', '')
}

const onSave = () => {
    emit('on-save', {general: editedGeneral.value})
    emit('on-edit', '')
}
</script>

<style>
.button-box{
    display: flex;
    justify-content: space-between;
    margin-top: 20px;
    width: 140px;
}
</style>