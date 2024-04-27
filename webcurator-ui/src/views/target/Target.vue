<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue';
import { ref, shallowRef, computed, onMounted } from "vue";
import {type UseFetchApis, useFetch} from '@/utils/rest.api';
import {formatDatetime} from '@/utils/helper';
import TargetList from './TargetList.vue';
import TargetTabView from './TargetTabView.vue';

const current = shallowRef(TargetList);

const props=ref({
    page: 'TargetTabView',
    openMode:'new',
    targetId:0,
});


const popPage=(options:any)=>{
    if(options.page==='TargetList'){
        current.value=TargetList;
    }else{
        props.value=options;
        current.value=TargetTabView;
    }
}


</script>

<template>
    <KeepAlive include="TargetList">
      <component :is="current" :props="props" @popPage="popPage" ></component>
    </KeepAlive>
</template>

<style>
</style>