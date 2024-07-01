<script setup lang="ts">
import { ref, inject } from 'vue';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { useTargetGropusDTO } from '@/stores/target'


const dialogRef: any = inject('dialogRef');

const rest: UseFetchApis = useFetch();

const targetGroups = useTargetGropusDTO();

const groups = ref([]);
const loading = ref(false);

const name = ref('');
const offset = ref(null);
const limit = ref(null);

const states: any = {
    8: 'Pending',
    9: 'Active',
    10: 'Inactive'
}

const search = () => {
    const searchParams = {
        filter: { name: name.value },
        offset: 0,
        limit: 1024
    }

    loading.value = true;

    rest
        .post('groups', searchParams, { header: 'X-HTTP-Method-Override', value: 'GET' })
        .then((data: any) => {
            groups.value = data['groups']
            loading.value = false
        })
        .catch((err: any) => {
            console.log(err.message)
            loading.value = false
        })
}

const isGroupAdded = (id: number) => {
    return targetGroups.targetGroups.some((t: any) => t.id == id);
}

const addGroup = (group: any) => {
    targetGroups.targetGroups.push(group);
}

search();

</script>

<template>
    <h5>Search</h5>
    <div class="flex mb-4">
        <InputText v-model="name" type="text" placeholder="Name" class="mr-4" />
        <Button label="Search&nbsp;&nbsp;" icon="pi pi-search" iconPos="right" @click="search()" />
    </div>

    <Divider type="dotted" />

    <div class="flex flex-wrap gap-2">
        <Chip class="p-0" v-for="group in targetGroups.targetGroups">
            <span class="p-2 m-0">{{ group.name }}</span>
            <Button class="p-0 m-0" icon="pi pi-times-circle" style="width: 2rem;" link @click="targetGroups.removeGroup(group.id)"/>
        </Chip>
    </div>

    <Divider type="dotted" />

    <DataTable class="w-full" :value="groups" size="small" paginator :rows="10" :loading="loading">
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
                    <Button v-else label="Add" text @click="addGroup(data)" />
                </div>
            </template>
        </Column>
    </DataTable>
</template>