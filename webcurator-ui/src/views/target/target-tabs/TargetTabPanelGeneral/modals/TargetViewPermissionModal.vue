<script setup lang="ts">
import { inject, onMounted, ref } from 'vue';
import { formatDate } from '@/utils/helper';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { usePermissionStatusStore } from '@/stores/permissions';

import WctFormField from '@/components/WctFormField.vue';
import Loading from '@/components/Loading.vue'

const dialogRef: any = inject('dialogRef');

const rest: UseFetchApis = useFetch();

const permissionId = ref();
const permission = ref();
const loading = ref(true);
const permissionStatuses = ref<{ [key: string]: string }>({});

const fetch = () => {
    loading.value = true;

    rest
        .get(`permissions/${permissionId.value}`)
        .then((data: any) => {
            permission.value = data;
            loading.value = false;
        })
        .catch((err: any) => {
            console.log(err.message);
            loading.value = false;
        });
}

onMounted(async() => {
  permissionId.value = dialogRef.value.data.permissionId;
  const statuses = await usePermissionStatusStore().fetch();
  permissionStatuses.value = statuses;
  fetch();
});

</script>

<template>
    <div>
        <Loading v-if="loading" />
        <div v-else class="mt-4">
            <WctFormField label="Authorising Agent">
                <p class="font-semibold">{{ permission.authorisingAgent }}</p>
            </WctFormField>
            <WctFormField label="Dates">
                <p class="font-semibold">{{ formatDate(permission.startDate) }} - {{ formatDate(permission.endDate) }}</p>
            </WctFormField>    
            <WctFormField label="Status">
                <p class="font-semibold">{{ permissionStatuses[permission.status] }}</p>
            </WctFormField> 
            <WctFormField label="Auth Agency Response">
                <p class="font-semibold">{{ permission.authResponse }}</p>
            </WctFormField> 
            <WctFormField label="Quick Pick">
                <p class="font-semibold">{{ permission.quickPick === true ? 'Yes' : 'No' }}</p>
            </WctFormField>
            <WctFormField label="Display Name">
                <p class="font-semibold">{{ permission.displayName }}</p>
            </WctFormField>
            <WctFormField label="Urls">
                <p v-for="(url, index) in permission.urlPatterns" :key="index" class="font-semibold">{{ url }}</p>  
            </WctFormField>

            <p class="font-semibold">Exclusions</p>
            <DataTable v-if="permission.exclusions.length > 0" size="small" showGridlines class="w-full" :rowHover="true" :value="permission.exclusions">
                <Column field="url" header="URL" />
                <Column field="reason" header="Reason" />
            </DataTable>
            <div v-else class="text-center">
                <p class="text-500">No exclusions have been defined</p>
            </div>

            <p class="font-semibold mt-4">Annotations</p>
            <DataTable v-if="permission.annotations.length > 0" size="small" showGridlines class="w-full" :rowHover="true" :value="permission.annotations" :pt="{emptyMessage: 'No annotations found'}" >
                <Column field="date" header="Date" />
                <Column field="user" header="User" />
                <Column field="notes" header="Notes" />
            </DataTable>
            <div v-else class="text-center">
                <p class="text-500">No annotations available</p>
            </div>
        </div>
    </div>
</template>