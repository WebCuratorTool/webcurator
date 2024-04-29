<script setup lang="ts">
import { ref, watch, computed, onBeforeMount, onActivated, provide } from "vue";
import {type UseFetchApis, useFetch} from '@/utils/rest.api';
import {formatDatetime} from '@/utils/helper';
import {useTargetGeneralDTO, formatTargetState} from '@/stores/target';
import TargetTabPanelGeneral from "./TargetTabPanelGeneral.vue";
import TargetTabPanelSeeds from "./TargetTabPanelSeeds.vue";
import TargetTabPanelProfile from "./TargetTabPanelProfile.vue";
import TargetTabPanelSchedule from "./TargetTabPanelSchedule.vue";
import TargetTabPanelAnnotations from "./TargetTabPanelAnnotations.vue";
import TargetTabPanelDescription from "./TargetTabPanelDescription.vue";
import TargetTabPanelGroups from "./TargetTabPanelGroups.vue";
import TargetTabPanelAccess from "./TargetTabPanelAccess.vue";

const options=defineProps(['props']);

const emit = defineEmits(['popPage']);

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

const fetchTargetDetais=()=>{
    isTargetAvailable.value=false;
    
    rest.get('targets/'+targetId.value).then((data:any)=>{
        isTargetAvailable.value=true;
        targetGeneral.setData(data.general);
        if(openMode.value === 'copy'){
            targetGeneral.name='';
        }
    }).catch((err:any)=>{
        console.log(err.message);
        initData();
     });
}


onBeforeMount(()=>{
    console.log("onBeforeMount");
    openMode.value=options.props.mode;
    if (openMode.value === 'view') {
        readOnly.value=true;
    }else{
        readOnly.value=false;
    }
    if (openMode.value==="new") {
        initData();
    }else{
        targetId.value=options.props.id;
        fetchTargetDetais();
    }
});

onActivated(()=>{
    console.log("onActivated");
});

const cancel=()=>{
    emit('popPage', {
        page: 'TargetList',
        mode: 'new',
        id: 0,
    });
}

const save=()=>{
    const dataReq={
        general: targetGeneral.getData(),
    }

    let rsp;
    if(openMode.value==='new'){
        rsp=rest.post('targets/save', dataReq)
    }else if(openMode.value==='edit'){
        rsp=rest.put('targets/' + targetGeneral.id, dataReq);
    }else{
        return;
    }

    rsp.then((data:any)=>{
        console.log(data);
    }).catch((err:any)=>{
        console.log(err.message);
        // alert(err.message);
    }).finally(() => {
        emit('popPage', {
            page: 'TargetList',
            mode: 'new',
            id: 0,
        });
    });
};

</script>

<template>    
    <div class="main-container">
        <div class="main-header">
            <div class="target-header-container">
                <!-- <div class="w-full">
                    
                    
                </div> -->
                <Toolbar style="border: none; background: transparent;">
                    <template #start> <Button icon="pi pi-arrow-left" @click="cancel" text/> </template>
                    <template #end> <Button icon="pi pi-save" @click="save" label="Save" :disabled="readOnly"/> </template>
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
                    <TargetTabPanelGeneral :readOnly="readOnly" />
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