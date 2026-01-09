<script setup lang="ts">
import { inject, onMounted, ref } from 'vue';
import { formatDate } from '@/utils/helper';
import type { Permission } from '@/types/permission';
import { useHarvestAuthorisationStatusStore } from '@/stores/harvestAuthorisations';
import { usePermissionStore } from '@/stores/permissions';

import WctFormField from '@/components/WctFormField.vue';
import Loading from '@/components/Loading.vue'

const dialogRef: any = inject('dialogRef');

const permission = ref<Permission>({} as Permission);
const loading = ref(true);
const permissionStatuses = ref<{ [key: string]: string }>({});

onMounted(async() => {
  try {
    const fetchedPermission: any = await usePermissionStore().fetch(dialogRef.value.data.permissionId);
    permission.value = fetchedPermission;
    const statuses = await useHarvestAuthorisationStatusStore().fetch();
    permissionStatuses.value = statuses;
    loading.value = false;
  } catch (error) {
    console.log(error);
  }
});

</script>

<template>
  <div>
    <Loading v-if="loading" />
    <div v-else class="mt-4">
      <WctFormField label="Authorising Agent">
        <p class="font-semibold">{{ permission.authorisingAgent.name }}</p>
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
        <p v-for="(url, index) in permission.urlPatterns" :key="index" class="font-semibold !mb-0">{{ url }}</p>  
      </WctFormField>

      <p class="font-semibold">Exclusions</p>
      <DataTable v-if="permission.exclusions.length > 0" size="small" showGridlines class="mb-4 w-full" :rowHover="true" :value="permission.exclusions">
        <Column field="url" header="URL" />
        <Column field="reason" header="Reason" />
      </DataTable>
      <div v-else class="text-center mb-4">
        <p class="text-500">No exclusions have been defined</p>
      </div>

      <p class="font-semibold">Annotations</p>
      <DataTable v-if="permission.annotations.length > 0" size="small" showGridlines class="w-full" :rowHover="true" :value="permission.annotations" >
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