<script setup lang="ts">
import { ref, onBeforeMount } from 'vue';
import { useRoute } from 'vue-router'
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { useTargetGeneralDTO, useTargetProfileDTO, useNextStateStore } from '@/stores/target';

import TargetTabView from './target-tabs/TargetTabView.vue';

const route = useRoute()
const targetId = route.params.id as string

const rest: UseFetchApis = useFetch();

const targetGeneral = useTargetGeneralDTO();
const targetProfile = useTargetProfileDTO();
const nextStates = useNextStateStore();

const editing = ref(false);
const isTargetAvailable = ref(false);

const initData = () => {
    isTargetAvailable.value = false;
    targetGeneral.initData();
    nextStates.initData();
}

const fetchTargetDetails = () => {
    isTargetAvailable.value = false;

    rest.get('targets/' + targetId).then((data: any) => {        
        isTargetAvailable.value = true;
        targetGeneral.setData(data.general);
        targetProfile.setData(data.profile);
        nextStates.setData(targetGeneral.selectedState, data.general.nextStates);
    }).catch((err: any) => {
        console.log(err.message);
        initData();
    });
}

const save = () => {
    const dataReq = {
        general: targetGeneral.getData(),
        profile: targetProfile.getData()
    }    

    rest.put('targets/' + targetGeneral.id, dataReq)
    .then((data: any) => {
        console.log(data)
    })
    .catch((err: any) => {
        console.log(err.message)
    })
    .finally(() => {
        editing.value = false
    })
}

const setEditing = (isEditing: boolean) => {
    editing.value = isEditing
}

onBeforeMount(() => {
    fetchTargetDetails();
})
</script>

<template>
    <TargetTabView 
        :editing=editing 
        :isTargetAvailable=isTargetAvailable 
        @setEditing="setEditing"
        @save="save"    
    />
</template>