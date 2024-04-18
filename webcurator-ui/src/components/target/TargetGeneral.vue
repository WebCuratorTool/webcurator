<script setup lang="ts">
import { ref, inject, computed, onMounted, onBeforeMount } from "vue";
import {useTargetGeneralDTO, stateList} from '@/components/target/target';

const fields=useTargetGeneralDTO();

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
            <InputText v-model="fields.id" disabled />
        </div>
        <div class="col-2">
            <label>Name(*):</label>
        </div>
        <div class="col-10">
            <InputText v-model="fields.name"/>
        </div>
        <div class="col-2">
            <label>Description:</label>
        </div>
        <div class="col-10">
            <Textarea v-model="fields.description" autoResize rows="6"/>
        </div>
        <div class="col-2">
            <label>Reference Number:</label>
        </div>
        <div class="col-10">
            <InputText v-model="fields.referenceNumber"/>
        </div>
        <div class="col-2">
            <label>Run on Approval:</label>
        </div>
        <div class="col-10">        
            <Checkbox id="checkOption1" name="option1" value="Run on Approval" v-model="fields.runOnApproval"/>
        </div>
        <div class="col-2">
            <label>Use Automated QA:</label>
        </div>
        <div class="col-10">
            <Checkbox id="checkOption2" name="option2" value="Use Automated QA" v-model="fields.automatedQA" />
        </div>
        <div class="col-2">
            <label>Owner:</label>
        </div>
        <div class="col-10">
            <Dropdown id="user" v-model="fields.selectedUser" :options="userList" optionLabel="name" placeholder="Select an User" checkmark class="w-full md:w-18rem" />
        </div>
        <div class="col-2">
            <label>State:</label>
        </div>
        <div class="col-10">
            <Dropdown id="state" v-model="fields.selectedState" :options="stateList" optionLabel="name" placeholder="Select the state" checkmark class="w-full md:w-18rem" />
        </div>
        <div class="col-2">
            <label>Auto-prune:</label>
        </div>
        <div class="col-10">        
            <Checkbox id="checkOption3" name="option3" value="Auto-prune:" v-model="fields.autoPrune"/>
        </div>
        <div class="col-2">
            <label>Reference Crawl:</label>
        </div>
        <div class="col-10">
            <Checkbox id="checkOption4" name="option4" value="Reference Crawl" v-model="fields.referenceCrawl" />
        </div>
        <div class="col-2">
            <label>Request to Archivists:</label>
        </div>
        <div class="col-10">
            <Textarea v-model="fields.requestToArchivists" autoResize rows="6"/>
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