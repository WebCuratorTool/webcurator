<script setup lang="ts">
import { ref, onBeforeMount } from 'vue';
import { useRoute } from 'vue-router'
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import {
    setTarget,
    useTargetDescriptionDTO,
    useTargetGeneralDTO,
    useTargetGropusDTO,
    useTargetProfileDTO,
    useTargetSeedsDTO, 
    useNextStateStore 
} from '@/stores/target';

import TargetTabView from './target-tabs/TargetTabView.vue';

const route = useRoute()
const targetId = route.params.id as string

const rest: UseFetchApis = useFetch();

const targetGeneral = useTargetGeneralDTO();
const targetProfile = useTargetProfileDTO();
const targetDescription = useTargetDescriptionDTO();
const targetSeeds = useTargetSeedsDTO();
const targetGroups = useTargetGropusDTO();
const nextStates = useNextStateStore();

const editing = ref(false);
const isTargetAvailable = ref(false);
const loading = ref(false);

const initData = () => {
    isTargetAvailable.value = false;
    targetGeneral.initData();
    nextStates.initData();
}

const fetchTargetDetails = () => {
    isTargetAvailable.value = false;
    loading.value = true;

    rest.get('targets/' + targetId).then((data: any) => {        
        isTargetAvailable.value = true;
        setTarget(data);
        nextStates.setData(targetGeneral.selectedState, data.general.nextStates);
    }).catch((err: any) => {
        console.log(err.message);
        initData();
    }).finally(() => {
        loading.value = false;
    });
}

const save = () => {
    const dataReq = {
        general: targetGeneral.getData(),
        profile: targetProfile.getData(),
        description: targetDescription.getData(),
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

fetchTargetDetails();

</script>

<template>
    <TargetTabView 
        :editing=editing 
        :isTargetAvailable=isTargetAvailable
        :loading=loading 
        @setEditing="setEditing"
        @save="save"    
    />
</template>