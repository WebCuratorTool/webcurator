<script setup lang="ts">
import { ref, watch, computed, onMounted, onBeforeUpdate, provide } from "vue";
import {useRoute, useRouter, onBeforeRouteUpdate } from 'vue-router';
import {type UseFetchApis, useFetch} from '@/utils/rest.api';
import {formatDatetime} from '@/utils/helper';
import {useTargetGeneralDTO, target, formatTargetState} from '@/stores/target';
import TargetTabPanelGeneral from "./TargetTabPanelGeneral.vue";
import TargetTabPanelSeeds from "./TargetTabPanelSeeds.vue";
import TargetTabPanelProfile from "./TargetTabPanelProfile.vue";
import TargetTabPanelSchedule from "./TargetTabPanelSchedule.vue";
import TargetTabPanelAnnotations from "./TargetTabPanelAnnotations.vue";
import TargetTabPanelDescription from "./TargetTabPanelDescription.vue";
import TargetTabPanelGroups from "./TargetTabPanelGroups.vue";
import TargetTabPanelAccess from "./TargetTabPanelAccess.vue";

const route=useRoute();
const router=useRouter();
const rest: UseFetchApis=useFetch();

const targetGeneral=useTargetGeneralDTO();

const openMode=ref();
const targetId=ref();
const readOnly=ref(false);
const isTargetAvailable=ref(false);

const initData=()=>{
    isTargetAvailable.value=false;
    targetId.value=undefined;
    targetGeneral.initData();
}

const fetchTargetDetais=(mode, id)=>{
    isTargetAvailable.value=false;
    targetId.value=id;
    rest.get('targets/'+id).then((data:any)=>{
        isTargetAvailable.value=true;
        console.log(data.general);
        targetGeneral.setData(data.general);
        console.log(targetGeneral.id);
        console.log(targetGeneral.creationDate);
    }).catch((err:any)=>{
        console.log(err.message);
        initData();
     });
}


watch(()=>router.currentRoute.value.path, (to, from)=>{
    openMode.value=route.params.mode;
    if (openMode.value === 'view') {
        readOnly.value=true;
    }else{
        readOnly.value=false;
    }
    if (openMode.value==="new") {
        initData();
    }else{
        fetchTargetDetais(route.params.mode, route.params.id);
    }
   
}, {immediate:true});

onMounted(()=>{
    console.log("onMounted");
    console.log(route.params.mode)
});

onBeforeUpdate(()=>{
    console.log("onBeforeUpdate");
});

onBeforeRouteUpdate((to) => {
    console.log("onBeforeRouteUpdate");
});

const save=()=>{
    const dataReq={
        general: targetGeneral.getData(),
    }

    if(openMode.value==='new'){
        rest.post('targets/save', dataReq).then((data:any)=>{
            console.log(data);
        }).catch((err:any)=>{
            console.log(err.message);
        });
    }else if(openMode.value==='edit'){
        rest.put('targets/' + targetGeneral.id, dataReq).then((data:any)=>{
            console.log(data);
        }).catch((err:any)=>{
            console.log(err.message);
        });
    }
};

</script>

<template>    
    <div class="main-container">
        <div class="main-header">
            <div class="target-header-container">
                <!-- <div class="w-full">
                    
                    
                </div> -->
                <Toolbar style="border: none; background: transparent;">
                    <template #start> <router-link class="nav-bar-link" :to="{ name: 'targets'}"><i id="main-header-arrow-left" class="pi pi-arrow-left"></i></router-link> </template>
                    <template #end> <Button icon="pi pi-save" @click="save" label="Save"/> </template>
                </Toolbar>

        
                <div class="w-full">
                    <span class="title">Target</span>
                    <div v-if="isTargetAvailable" class="subtitle-container p-overlay-badge ">
                        <span class="sub-title">{{ targetGeneral.id }} - {{ formatDatetime(targetGeneral.creationDate) }}</span>
                        <span class="p-badge p-component p-badge-secondary" data-pc-name="badge" data-pc-section="root">{{ formatTargetState(targetGeneral.selectedState) }}</span>
                    </div>
                </div>
            </div>
        </div>
        <div class="main-content">
            <TabView class="tabview-custom">
                <TabPanel header="Genaral">
                    <TargetTabPanelGeneral />
                </TabPanel>
                <TabPanel header="Seeds">
                    <TargetTabPanelSeeds />
                </TabPanel>
                <TabPanel header="Profile">
                    <TargetTabPanelProfile />
                </TabPanel>
                <TabPanel header="Schedule">
                    <TargetTabPanelSchedule />
                </TabPanel>
                <TabPanel header="Annotations">
                    <TargetTabPanelAnnotations />
                </TabPanel>
                <TabPanel header="Description">
                    <TargetTabPanelDescription />
                </TabPanel>
                <TabPanel header="Groups">
                    <TargetTabPanelGroups />
                </TabPanel>
                <TabPanel header="Access">
                    <TargetTabPanelAccess />
                </TabPanel>
            </TabView>
        </div>
    </div>
</template>

<style>
.tabview-custom{
    min-height: 60vh;
}
</style>