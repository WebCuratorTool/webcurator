<script setup lang="ts">
import { inject, onMounted, ref } from 'vue';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';

const dialogRef: any = inject('dialogRef');

const rest: UseFetchApis = useFetch();

const harvestAuths = ref([]);
const filteredHarvestAuths = ref([]);
const loading = ref(false);

const searchTerm = ref('');

const seed = ref();

const fetch = () => {
    const searchParams = {
        offset: 0,
        limit: 1024
    }

    loading.value = true;

    rest
        .post('harvest-authorisations', searchParams, { header: 'X-HTTP-Method-Override', value: 'GET' })
        .then((data: any) => {
            harvestAuths.value = data['harvestAuthorisations']
            filteredHarvestAuths.value = harvestAuths.value
            loading.value = false
        })
        .catch((err: any) => {
            console.log(err.message)
            loading.value = false
        })
}

const search = () => {
    const lowerCaseSearchTerm = searchTerm.value.toLowerCase();
    filteredHarvestAuths.value = harvestAuths.value.filter((g: any ) => 
        g.name.toLowerCase().includes(lowerCaseSearchTerm)
    );
}

const isAuthAdded = (id: number) => {
    return seed.value.authorisations.some((authId: any) => authId == id);
}

onMounted(() => {
    seed.value = dialogRef.value.data.seed;
})

fetch();

</script>

<template>
    <div class="h-full">
        <h5>Search</h5>
        <div class="flex mb-4">
            <InputText v-model="searchTerm" type="text" class="mr-4" />
            <Button label="Search&nbsp;&nbsp;" icon="pi pi-search" iconPos="right" @click="search()" />
        </div>

        <DataTable class="w-full" :value="filteredHarvestAuths" size="small" paginator :rows="10" scrollHeight="100%" :loading="loading"
            pt:wrapper:class="h-26rem">
            <Column field="name" header="Name" />
            <Column field="authorisingAgents" header="Authorising Agents">
                <template #body="{ data }">
                    <div v-for="authorisingAgent in data.authorisingAgents">
                        <span>{{ authorisingAgent }}</span>
                    </div>
                </template>
            </Column>
            <Column>
                <template #body="{ data }">
                    <div class="flex justify-content-center">
                        <i v-if="isAuthAdded(data.id)" class="pi pi-check" />
                        <Button v-else class="p-0 m-0" label="Add" text @click="seed.authorisations.push(data.id)" />
                    </div>
                </template>
            </Column>
        </DataTable>
    </div>
</template>