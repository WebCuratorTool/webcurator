<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue';
import { ref, computed, onMounted } from "vue";
import {type UseFetchApis, useFetch} from '@/utils/rest.api';
import {formatDatetime} from '@/utils/helper';
import {useUsersStore} from '@/stores/users';
import { useAgenciesStore } from '@/stores/agencies';
import {stateList, formatTargetState} from '@/stores/target';

const options=defineProps(['props']);

const emit = defineEmits(['popPage']);

const rest: UseFetchApis=useFetch();

const users=useUsersStore();
const agencies=useAgenciesStore();


// Search conditions
const targetId=ref(null);
const targetName=ref(null);
const targetSeed=ref(null);
const targetDescription=ref(null);
const targetMemberOf=ref(null);

// Filter conditions
const selectedAgency=ref(null);
const selectedUser=ref(null);
const noneDisplayOnly=ref(false);
const selectedState=ref(null);

const targetList=ref(null);
const loadingTargetList=ref(false);

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

const createNew=()=>{
    emit('popPage', {
        page: 'TargetTabView',
        mode: 'new',
        id: 0,
    });
}

const openDetails=(mode:string, id:number)=>{
    emit('popPage', {
        page: 'TargetTabView',
        mode: mode,
        id: id,
    });
}

</script>

<template>
    <div class="targets">
        <PageHeader title="Targets" />
    </div>
    <div class="main-content grid">
        <div class="col-12 card">
            <h5>Query</h5>
            <div class="formgrid grid" id="grid-search">
                <div class="field col">
                    <label>Target ID</label>
                    <InputNumber v-model="targetId" :useGrouping="false"/>
                </div>
                <div class="field col">
                    <label>Target Name</label>
                    <InputText v-model="targetName" type="text" />
                </div>
                <div class="field col">
                    <label>Seed</label>
                    <InputText v-model="targetSeed" type="text" />
                </div>
                <div class="field col">
                    <label>Description</label>
                    <InputText v-model="targetDescription" type="text" />
                </div>
                <div class="field col">
                    <label>Member of</label>
                    <InputText v-model="targetMemberOf" type="text" />
                </div>
                <div class="field col">
                    <Button label="Search&nbsp;&nbsp;" icon="pi pi-search" iconPos="right" id="search-button" @click="search()"></Button>
                </div>
            </div>

            <div class="formgroup-inline">
                <div class="field">
                    <InputGroup class="w-full md:w-20rem">
                        <InputGroupAddon>Agency</InputGroupAddon>
                        <Dropdown id="agency" v-model="selectedAgency" :options="agencies.agencyList" optionLabel="name" placeholder="Select an Agency" checkmark class="w-full md:w-18rem" />
                    </InputGroup>
                </div>
                <div class="field">
                    <InputGroup class="w-full md:w-20rem">
                        <InputGroupAddon>User</InputGroupAddon>
                        <Dropdown id="user" v-model="selectedUser" :options="users.userList" optionLabel="name" placeholder="Select an User" checkmark class="w-full md:w-18rem" />
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

        <div class="col-12 card">
            <DataTable
                :value="targetList"
                size="small"
                :paginator="true"
                :rows="10"
                dataKey="oid"
                :rowHover="true"
                filterDisplay="menu"
                :loading="loadingTargetList"
                :globalFilterFields="['name', 'country.name', 'representative.name', 'balance', 'status']"
                resizableColumns columnResizeMode="fit"
                showGridlines
            >
                <template #header>
                    <div class="flex justify-content-between flex-column sm:flex-row">
                        <h5>Results</h5>
                        <Button severity="secondary" @click="createNew" raised>Create new</Button>
                    </div>
                </template>
                <template #empty> No targets found. </template>
                <template #loading> Loading target list. Please wait. </template>
                <Column field="id" sortable  header="Id" dataType="numeric" style="min-width: 2rem"></Column>
                <Column header="Date" sortable dataType="date" style="min-width: 5rem">
                    <template #body="{ data }">
                        {{ formatDatetime(data.creationDate) }}
                    </template>
                </Column>
                <Column field="name" header="Name" sortable style="min-width: 8rem"></Column>
                <Column field="agency" header="Agency" sortable style="min-width: 5rem"></Column>
                <Column field="owner" header="Owner" sortable filterField="owner" style="min-width: 6rem"></Column>
                <Column field="state" header="Status" sortable style="min-width: 2rem">
                    <template #body="{ data }">
                        {{ formatTargetState(data.state) }}
                    </template>
                </Column>
                <Column header="Seed" field="seed" style="min-width: 12rem">
                    <template #body="{ data }">
                        <div v-for="seed in data.seeds" :key="seed">                        
                            <span v-if="seed.primary" style="font-weight: bold;">{{ seed.seed }}</span>
                            <span v-else>{{ seed.seed }}</span>
                        </div>                        
                    </template>
                </Column>
                <Column header="Action" field="id" style="max-width: 5rem">
                    <template #body="{ data }">
                        <div id="actions" class="flex flex-wrap justify-content-center">
                            <Button @click="openDetails('view', data.id)" text><img alt="logo" src="@/assets/images/action-icon-view.gif" /></Button>
                            <Button @click="openDetails('edit', data.id)" text><img alt="logo" src="@/assets/images/action-icon-edit.gif" /></Button>
                            <Button text><img alt="logo" src="@/assets/images/action-icon-copy.gif" /></Button>
                            <Button text><img alt="logo" src="@/assets/images/action-icon-target-instances.gif" /></Button>
                        </div>
                    </template>
                </Column>
            </DataTable>
        </div>
    </div>
</template>

<style>
#grid-search label{
    text-align: left;
}
.btn-sub{
  font-size: 1.0em;
}

#search-button{
    margin-top: 22px;
    margin-left: 120px;
}

.toolbar{
    border: 0px;
    margin: 0px;
    padding-top: 0;
    padding-bottom: 0;
}

#actions img{
    width: 1rem;
    height: 1rem;
    padding: 0;
}

#actions button{
    padding: 0 0.3rem;
}
</style>