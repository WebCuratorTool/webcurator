<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue';

import { ref } from "vue";

const selectedCity = ref();
const cities = ref([
    { name: 'New York', code: 'NY' },
    { name: 'Rome', code: 'RM' },
    { name: 'London', code: 'LDN' },
    { name: 'Istanbul', code: 'IST' },
    { name: 'Paris', code: 'PRS' }
]);
const noneDisplayOnly=ref(false);

</script>

<template>
  <div class="targets">
    <PageHeader title="Targets" />
  </div>
  <div class="main-content">
    <div class="card p-fluid" style="width: 100%;">
        <h3>Query</h3>
        <div class="grid">
            <div class="col-12 md:col-11">
                <div class="formgroup-inline search-condition">
                    <div class="field">
                        <label for="targetId">Target ID</label>
                        <InputText id="targetId" type="text" />
                    </div>
                    <div class="field">
                        <label for="targetName">Target Name</label>
                        <InputText id="targetName" type="text" />
                    </div>
                    <div class="field">
                        <label for="seed">Seed</label>
                        <InputText id="seed" type="text" />
                    </div>
                    <div class="field">
                        <label for="description">Description</label>
                        <InputText id="description" type="text" />
                    </div>
                    <div class="field">
                        <label for="memberof">Member of</label>
                        <InputText id="memberof" type="text" />
                    </div>
                </div>
            </div>
            <div class="col-12 md:col-1">
                <div class="formgroup-inline">
                    <div class="field">
                        <label for="search"></label>
                        <Button id="search" label="Search" icon="pi pi-search" iconPos="right"></Button>
                    </div>
                </div>
            </div>
        </div>

        <div class="formgroup-inline">
            <div class="field">
                <InputGroup class="w-full md:w-20rem">
                    <InputGroupAddon>Agency</InputGroupAddon>
                    <Dropdown id="agency" v-model="selectedCity" :options="cities" showClear optionLabel="name" placeholder="Select a City" class="w-full md:w-14rem" />
                </InputGroup>                   
            </div>
            <div class="field">
                <InputGroup class="w-full md:w-20rem">
                    <InputGroupAddon>User</InputGroupAddon>
                    <Dropdown id="user" v-model="selectedCity" :options="cities" showClear optionLabel="name" placeholder="Select a City" class="w-full md:w-14rem" />
                </InputGroup>
            </div>
            
            <div class="field">
                <InputGroup>
                    <InputGroupAddon>Noe-Display  Only</InputGroupAddon>      
                    <InputGroupAddon>
                        <Checkbox v-model="noneDisplayOnly" :binary="true" />
                    </InputGroupAddon>
                </InputGroup>
            </div>
            <div class="field">
                <Button label="&nbsp;&nbsp;Reset filter" icon="pi pi-times" severity="secondary"  class="btn-sub" />
            </div>
            <div class="field">
                <Button label="&nbsp;&nbsp;Filter" icon="pi pi-filter" severity="secondary"  class="btn-sub" />
            </div>
        </div>
    
        </div>
    </div>
</template>

<style>
.btn-sub{
  font-size: 1.25em;
}

.search-condition .field {
    width: 15rem;
}

</style>