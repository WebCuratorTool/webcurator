<script setup lang="ts">
import { ref, inject, computed, onMounted, onBeforeMount } from "vue";
import {type TargateGeneralDTO, stateList} from '@/components/target/target';

// defineProps<{
//     openMode: string,
//     targetId: number
// }>()

// const fields:TargateGeneralDTO=inject('targetGeneral', {
//     id: undefined,
//     name: "",
//     description: "",
//     referenceNumber: "",
//     runOnApproval: false,
//     automatedQA: false,
// });

// const id=ref();
// const name=ref("");
// const description=ref("");
// const referenceNumber=ref("");
// const runOnApproval=ref(false);
// const automatedQA=ref(false);
// const userList=ref([]);
// const selectedUser=ref();
// const selectedState=ref(null);
// const autoPrune=ref(false);
// const referenceCrawl=ref(false);
// const requestToArchivists=ref("");


let targetDTO:any=null;

onBeforeMount(()=>{
    targetDTO=inject("TargetDTO",{
    general:{
        id: undefined,
        creationDate: 0,
        name: "",
        description: "",
        referenceNumber: "",
        runOnApproval: false,
        automatedQA: false,
        owner: "",
        state: 1,
        autoPrune:false,
        referenceCrawl:false,
        requestToArchivists:"",
    }
});
    console.log("Inject targetDTO:");
    console.log(targetDTO);
});

</script>

<template>

<div class="card p-fluid">   
    <div class="grid">
        <div class="col-2">
            <label>Id:</label>
        </div>
        <div class="col-10">
            <InputText v-model="targetDTO.general.id" disabled />
        </div>
        <div class="col-2">
            <label>Name(*):</label>
        </div>
        <div class="col-10">
            <InputText v-model="targetDTO.general.name"/>
        </div>
        <div class="col-2">
            <label>Description:</label>
        </div>
        <div class="col-10">
            <Textarea v-model="targetDTO.general.description" autoResize rows="6"/>
        </div>
        <div class="col-2">
            <label>Reference Number:</label>
        </div>
        <div class="col-10">
            <InputText v-model="targetDTO.general.referenceNumber"/>
        </div>
        <div class="col-2">
            <label>Run on Approval:</label>
        </div>
        <div class="col-10">        
            <Checkbox id="checkOption1" name="option1" value="Run on Approval" v-model="targetDTO.general.runOnApproval"/>
        </div>
        <div class="col-2">
            <label>Use Automated QA:</label>
        </div>
        <div class="col-10">
            <Checkbox id="checkOption2" name="option2" value="Use Automated QA" v-model="targetDTO.general.automatedQA" />
        </div>
        <div class="col-2">
            <label>Owner:</label>
        </div>
        <div class="col-10">
            <Dropdown id="user" v-model="targetDTO.general.selectedUser" :options="userList" optionLabel="name" placeholder="Select an User" checkmark class="w-full md:w-18rem" />
        </div>
        <div class="col-2">
            <label>State:</label>
        </div>
        <div class="col-10">
            <Dropdown id="state" v-model="targetDTO.general.selectedState" :options="stateList" optionLabel="name" placeholder="Select the state" checkmark class="w-full md:w-18rem" />
        </div>
        <div class="col-2">
            <label>Auto-prune:</label>
        </div>
        <div class="col-10">        
            <Checkbox id="checkOption3" name="option3" value="Auto-prune:" v-model="targetDTO.general.autoPrune"/>
        </div>
        <div class="col-2">
            <label>Reference Crawl:</label>
        </div>
        <div class="col-10">
            <Checkbox id="checkOption4" name="option4" value="Reference Crawl" v-model="targetDTO.general.referenceCrawl" />
        </div>
        <div class="col-2">
            <label>Request to Archivists:</label>
        </div>
        <div class="col-10">
            <Textarea v-model="targetDTO.general.requestToArchivists" autoResize rows="6"/>
        </div>
    </div>
</div>

    


   
</template>

<style>
    .grid{
        align-items: center;
    }
    label{
        width: 100%;
        display: inline-block;
        position: relative;
        text-align: right;
        margin-right: 0;
    }
</style>