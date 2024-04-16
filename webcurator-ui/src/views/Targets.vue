<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue';
import { ref, computed, onMounted } from "vue";
import {type UseFetchApis, useFetch} from '../rest.api';

const rest: UseFetchApis=useFetch();

const STATE_MAP=["Pending","Reinstated","Nominated","Rejected","Approved", "Cancelled", "Completed"];

// Search conditions
const targetId=ref(null);
const targetName=ref(null);
const targetSeed=ref(null);
const targetDescription=ref(null);
const targetMemberOf=ref(null);

// Filter conditions
const agencyList=ref();
const selectedAgency=ref(null);
const userList=ref();
const selectedUser=ref(null);
const noneDisplayOnly=ref(false);
// const stateList=ref([
//     {"name":"Pending", "code":1},
//     {"name":"Reinstated", "code":2},
//     {"name":"Nominated", "code":3},
//     {"name":"Rejected", "code":4},
//     {"name":"Approved", "code":5},
//     {"name":"Cancelled", "code":6},
//     {"name":"Completed", "code":7}
// ]);
const stateList=computed(()=>{
    const ary=[];
    for(var i=0; i<STATE_MAP.length; i++){
        ary.push({
            "name": STATE_MAP[i],
            "code": i+1,
        })
    }
    return ary;
});

const selectedState=ref(null);

const targetList=ref(null);
const loadingTargetList=ref(false);

onMounted(() => {
    rest.get("agencies").then((data:any)=>{
        const formatedData=[];
        for(var i=0; i<data.length; i++){
            var item=data[i];
            formatedData.push({
                "name": item["name"],
                "code": item["id"],                
            });
        }
        agencyList.value = formatedData;
    }).catch((err:any)=>{
        console.log(err.message);
    });

    rest.get("users").then((data:any)=>{
        data=data["users"];
        const formatedData=[];
        for(var i=0; i<data.length; i++){
            var item=data[i];
            formatedData.push({
                "name": item["firstName"] + " " + item["lastName"] + " (" + item["name"] + ")",
                "code": item["id"],                
            });
        }
        userList.value = formatedData;
    }).catch((err:any)=>{
        console.log(err.message);
    });
});

const formatDate = (timestamp:number) => {
    const value=new Date(timestamp);
    return value.toLocaleDateString(undefined, {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
};

const formatState = (stateCode:number) => {
    if(stateCode>0 && stateCode<=STATE_MAP.length){
        return STATE_MAP[stateCode - 1];
    }else{
        return 'unknown';
    }
};

const search= () => {
    const filter={
        "targetId": targetId.value,
        "name": targetName.value,
        "seed": targetSeed.value,
        "description": targetDescription.value,
    }

    const searchParams={
        "filter": filter,
        "offset": 0,
        "limit": 1024,
        "sortBy": "creationDate,asc",
    }

    loadingTargetList.value=true;
    rest.post("targets", searchParams).then((data:any) => {
        console.log(data);
        targetList.value=data["targets"];
        console.log(targetList.value);
        loadingTargetList.value=false;
    }).catch((err:any)=>{
        console.log(err.message);
        loadingTargetList.value=false;
    });
};


</script>

<template>
    <div class="targets">
        <PageHeader title="Targets" />
    </div>
    <div class="main-content grid">
        <div class="col-12 card">
            <h5>Query</h5>
            <div class="flex flex-wrap align-items-center justify-content-between gap-2">
                <div class="formgroup-inline search-condition">
                    <div class="field search-input">
                        <label>Target ID</label>
                        <InputNumber v-model="targetId" :useGrouping="false"/>
                    </div>
                    <div class="field search-input">
                        <label>Target Name</label>
                        <InputText v-model="targetName" type="text" />
                    </div>
                    <div class="field search-input">
                        <label>Seed</label>
                        <InputText v-model="targetSeed" type="text" />
                    </div>
                    <div class="field search-input">
                        <label>Description</label>
                        <InputText v-model="targetDescription" type="text" />
                    </div>
                    <div class="field search-input">
                        <label>Member of</label>
                        <InputText v-model="targetMemberOf" type="text" />
                    </div>
                </div>
                <Button label="Search&nbsp;&nbsp;" icon="pi pi-search" iconPos="right" id="search-button" @click="search()"></Button>
            </div>

            <div class="flex flex-wrap align-items-center justify-content-between gap-2">
                <div class="formgroup-inline">
                    <div class="field">
                        <InputGroup class="w-full md:w-20rem">
                            <InputGroupAddon>Agency</InputGroupAddon>
                            <Dropdown id="agency" v-model="selectedAgency" :options="agencyList" optionLabel="name" placeholder="Select an Agency" checkmark class="w-full md:w-18rem" />
                        </InputGroup>
                    </div>
                    <div class="field">
                        <InputGroup class="w-full md:w-20rem">
                            <InputGroupAddon>User</InputGroupAddon>
                            <Dropdown id="user" v-model="selectedUser" :options="userList" optionLabel="name" placeholder="Select an User" checkmark class="w-full md:w-18rem" />
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
                        <InputGroup class="w-full md:w-20rem">
                            <InputGroupAddon>State</InputGroupAddon>
                            <MultiSelect v-model="selectedState" :options="stateList" optionLabel="name" placeholder="Select States"  :maxSelectedLabels="3" class="w-full md:w-20rem" />
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

        <div class="col-12 card">
            <DataTable
                :value="targetList"
                :paginator="true"
                :rows="10"
                dataKey="oid"
                :rowHover="true"
                filterDisplay="menu"
                :loading="loadingTargetList"
                :globalFilterFields="['name', 'country.name', 'representative.name', 'balance', 'status']"
                showGridlines
            >
                <template #header>
                    <div class="flex justify-content-between flex-column sm:flex-row">
                        <h5>Results</h5>
                        <Button label="Create new" severity="secondary"/>
                    </div>
                </template>
                <template #empty> No targets found. </template>
                <template #loading> Loading target list. Please wait. </template>
                <Column field="oid" header="Id" dataType="numeric" style="min-width: 2rem">
                    <template #body="{ data }">
                        {{ data.id }}
                    </template>
                </Column>
                <Column header="Date" filterField="date" dataType="date" style="min-width: 5rem">
                    <template #body="{ data }">
                        {{ formatDate(data.creationDate) }}
                    </template>
                </Column>
                <Column field="name" header="Name" style="min-width: 8rem">
                    <template #body="{ data }">
                        {{ data.name }}
                    </template>
                </Column>
                <Column header="Agency" filterField="agency" style="min-width: 5rem">
                    <template #body="{ data }">
                        {{ data.agency }}
                    </template>
                </Column>
                <Column header="Owner" filterField="owner" style="min-width: 3rem">
                    <template #body="{ data }">
                        {{ data.owner }}
                    </template>
                </Column>
                <Column header="Status" field="status" style="min-width: 2rem">
                    <template #body="{ data }">
                        {{ formatState(data.state) }}
                    </template>
                </Column>
                <Column header="Seed" field="seed" style="min-width: 12rem">
                    <template #body="{ data }">
                        <div v-for="seed in data.seeds" :key="seed">                        
                            <span style="font-weight: bold;" v-if="seed.primary">{{ seed.seed }}</span>
                            <span v-else>{{ seed.seed }}</span>
                        </div>                        
                    </template>
                </Column>
                <Column header="Action" field="oid" style="min-width: 12rem">
                    <template #body="{ data }">
                        {{ data.oid }}
                    </template>
                </Column>
            </DataTable>
        </div>
    </div>
</template>

<style>
.btn-sub{
  font-size: 1.25em;
}

.search-condition .search-input {
    width: 15rem;
}

#search-button{
    margin-top: 22px;
}

.toolbar{
    border: 0px;
    margin: 0px;
    padding-top: 0;
    padding-bottom: 0;
}

</style>