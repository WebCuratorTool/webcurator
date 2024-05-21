<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useTargetGeneralDTO, useNextStateStore } from '@/stores/target';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';

import TargetTabView from './target-tabs/TargetTabView.vue';

const router = useRouter()

const editing = ref(true);
const isTargetAvailable = ref(false);

const rest: UseFetchApis = useFetch();

const targetGeneral = useTargetGeneralDTO();
const nextStates = useNextStateStore();

const initData = () => {
    isTargetAvailable.value = false;
    targetGeneral.initData();
    nextStates.initData();
}

const save = () => {
    const dataReq = {
        general: targetGeneral.getData()
    }

    rest.post('targets/save', dataReq)
    .then((data: any) => {
        console.log(data)
    })
    .catch((err: any) => {
        console.log(err.message)
    })
    .finally(() => {
        editing.value = false
        router.push('/wct/targets/')
    })
}

const setEditing = (isEditing: boolean) => {
    editing.value = isEditing
    if (!isEditing) {
        router.push('/wct/targets/')
    }
}

initData()

</script>

<template>
    <TargetTabView 
        :editing=editing 
        :isTargetAvailable=isTargetAvailable 
        @setEditing="setEditing"
        @save="save"    
    />
</template>