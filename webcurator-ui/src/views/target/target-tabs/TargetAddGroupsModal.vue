<script setup lang="ts">
import { ref } from 'vue';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { useTargetGropusDTO } from '@/stores/target'

const rest: UseFetchApis = useFetch();

const targetGroups = useTargetGropusDTO();

const groups = ref([]);
const filteredGroups = ref([]);
const loading = ref(false);

const searchTerm = ref('');

const states: any = {
    8: 'Pending',
    9: 'Active',
    10: 'Inactive'
}

const fetch = () => {
    const searchParams = {
        offset: 0,
        limit: 1024
    }

    loading.value = true;

    rest
        .post('groups', searchParams, { header: 'X-HTTP-Method-Override', value: 'GET' })
        .then((data: any) => {
            groups.value = data['groups']
            filteredGroups.value = groups.value
            loading.value = false
        })
        .catch((err: any) => {
            console.log(err.message)
            loading.value = false
        })
}

const search = () => {
    const lowerCaseSearchTerm = searchTerm.value.toLowerCase();
    filteredGroups.value = groups.value.filter((g: any ) => 
        g.name.toLowerCase().includes(lowerCaseSearchTerm) || g.agency.toLowerCase().includes(lowerCaseSearchTerm)
    );
}

const isGroupAdded = (id: number) => {
    return targetGroups.targetGroups.some((t: any) => t.id == id);
}

fetch();

</script>

<template>
    <div class="h-full">
        <h5>Search</h5>
        <div class="flex mb-4">
            <InputText v-model="searchTerm" type="text" placeholder="Name" class="mr-4" />
            <Button label="Search&nbsp;&nbsp;" icon="pi pi-search" iconPos="right" @click="search()" />
        </div>
    
        <Divider type="dotted" />
    
        <div class="flex flex-wrap gap-2">
            <Chip class="px-2" v-for="group in targetGroups.targetGroups">
                <span class="p-2 m-0">{{ group.name }}</span>
                <Button class="p-0 m-0" icon="pi pi-times-circle" style="width: 2rem;" link @click="targetGroups.removeGroup(group.id)"/>
            </Chip>
        </div>
    
        <Divider type="dotted" />
    
        <DataTable class="w-full" :value="filteredGroups" size="small" paginator :rows="10" scrollHeight="100%" :loading="loading"
            pt:wrapper:class="h-26rem">
            <Column field="name" header="Name" />
            <Column field="state" header="Status">
                <template #body="{ data }">
                    {{ states[data.state] }}
                </template>
            </Column>
            <Column field="agency" header="Agency" sortable />
            <Column>
                <template #body="{ data }">
                    <div class="flex justify-content-center">
                        <i v-if="isGroupAdded(data.id)" class="pi pi-check" />
                        <Button v-else class="p-0 m-0" label="Add" text @click="targetGroups.addGroup(data)" />
                    </div>
                </template>
            </Column>
        </DataTable>
    </div>
</template>

