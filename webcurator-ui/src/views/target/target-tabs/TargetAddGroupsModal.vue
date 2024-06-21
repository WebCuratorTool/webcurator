<script setup lang="ts">
import { ref, inject, onMounted } from 'vue';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';

const dialogRef: any = inject('dialogRef');

const rest: UseFetchApis = useFetch();

const groups = ref([]);
const targetGroups = ref(dialogRef.value.data.targetGroups);
const editedGroups = ref(dialogRef.value.data.editedGroups);
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

const findAddedGroup = (id: number) => {
    return editedGroups.value.some((t: any) => t.id == id);
}

const addGroup = (group: any) => {
    targetGroups.value.push(group);
    editedGroups.value.push(group);
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

    <DataTable class="w-full" :value="groups" :loading="loading">
        <Column field="name" header="Name" />
        <Column field="state" header="Status">
            <template #body="{ data }">
                {{ states[data.state] }}
            </template>
        </Column>
        <Column field="agency" header="Agency" />
        <Column>
            <template #body="{ data }">
                <div class="flex justify-content-center">
                    <i v-if="findAddedGroup(data.id)" class="pi pi-check" />
                    <Button v-else label="Add" text @click="addGroup(data)" />
                </div>
            </template>
        </Column>
    </DataTable>
</template>