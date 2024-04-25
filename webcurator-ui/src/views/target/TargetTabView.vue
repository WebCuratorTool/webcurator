<script setup lang="ts">
import { ref, watch, computed, onMounted, onBeforeUpdate, provide } from "vue";
import {useRoute, useRouter, onBeforeRouteUpdate } from 'vue-router';
import {type UseFetchApis, useFetch} from '@/utils/rest.api';
import {useTargetGeneralDTO, target, getTargetState} from './target';
import TargetGeneral from "./TargetGeneral.vue";

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
    rest.get('targets/'+id).then(data=>{
        isTargetAvailable.value=true;
        targetGeneral.setData(data.general);
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


// const isTargetAvailable=computed(() => {
//     if(openMode.value==="view" || openMode.value==="edit"){
//         if(target.selectedTarget){
//             return true;
//         }
//     }
//     return false;
// });

const save=()=>{

};

</script>

<template>    
    <div class="main-container">
        <div class="main-header">
            <div class="target-header-container">
                <div class="w-full">
                    <router-link class="nav-bar-link" :to="{ name: 'targets'}"><i id="main-header-arrow-left" class="pi pi-arrow-left"></i></router-link>
                </div>
        
                <div class="w-full">
                    <span class="title">Target</span>
                    <div v-if="isTargetAvailable" class="subtitle-container p-overlay-badge ">
                        <span class="sub-title">{{ targetGeneral.id }}</span>
                        <span class="p-badge p-component p-badge-secondary" data-pc-name="badge" data-pc-section="root">{{ getTargetState() }}</span>
                    </div>
                </div>
            </div>
        </div>
        <div class="main-content">
            <TabView class="tabview-custom">
                <TabPanel header="Genaral">
                    <TargetGeneral />
                </TabPanel>
                <TabPanel header="Seeds">
                    <p class="m-0">
                        Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim
                        ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Consectetur, adipisci velit, sed quia non numquam eius modi.
                    </p>
                </TabPanel>
                <TabPanel header="Profile">
                    <p class="m-0">
                        At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui
                        officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus.
                    </p>
                </TabPanel>
                <TabPanel header="Schedule">
                
                </TabPanel>
                <TabPanel header="Annotations">
                
                </TabPanel>
                <TabPanel header="Description">
                
                </TabPanel>
                <TabPanel header="Groups">
                
                </TabPanel>
                <TabPanel header="Access">
                
                </TabPanel>
            </TabView>

            
        </div>

        <div class="layout-footer">
            <Button icon="pi pi-save" @click="save" label="Save"/>
        </div>
    </div>
</template>
