<script setup lang="ts">
import { ref, inject, computed, onMounted, onBeforeMount } from "vue";
import {useUsersStore} from '@/stores/users';
import FormField from '@/components/FormField.vue';
import {useTargetGeneralDTO, stateList} from './target';


const fields=useTargetGeneralDTO();
const users=useUsersStore();
</script>

<template>

<div class="card p-fluid">
    <div class="grid">
        <FormField label="Id">
            <InputText v-model="fields.id" disabled />
        </FormField>

        <FormField label="Name(*)">
            <InputText v-model="fields.name"/>
        </FormField>

        <FormField label="Description">
            <Textarea v-model="fields.description" autoResize rows="6"/>
        </FormField>

        <FormField label="Reference Number">
            <InputText v-model="fields.referenceNumber"/>
        </FormField>

        <FormField label="Run on Approval">
            <Checkbox id="checkOption1" name="option1" value="Run on Approval" v-model="fields.runOnApproval"/>
        </FormField>

        <FormField label="Use Automated QA">
            <Checkbox id="checkOption2" name="option2" value="Use Automated QA" v-model="fields.automatedQA" />
        </FormField>

        <FormField label="Owner">
            <Dropdown id="user" v-model="fields.selectedUser" :options="users.userList" optionLabel="name" placeholder="Select an User" checkmark class="w-full md:w-18rem" />
        </FormField>

        <FormField label="State">
            <Dropdown id="state" v-model="fields.selectedState" :options="stateList" optionLabel="name" placeholder="Select the state" checkmark class="w-full md:w-18rem" /> 
        </FormField>

        <FormField label="Auto-prune">
            <Checkbox id="checkOption3" name="option3" value="Auto-prune:" v-model="fields.autoPrune"/>
        </FormField>

        <FormField label="Reference Crawl">
            <Checkbox id="checkOption4" name="option4" value="Reference Crawl" v-model="fields.referenceCrawl" />
        </FormField>

        <FormField label="Request to Archivists">
            <Textarea v-model="fields.requestToArchivists" autoResize rows="6"/>
        </FormField>
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

    .col-2, .col-10{
        padding: 0.2rem;
    }
</style>