<script setup lang="ts">
import { inject, onMounted, reactive, ref } from 'vue';
import type { DataTableRowClickEvent } from 'primevue/datatable';
import { usePermissionStore, usePermissionStatusStore } from '@/stores/permissions';
import type { Permission } from '@/types/permission';
import { formatDate } from '@/utils/helper';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';

import Loading from '@/components/Loading.vue';

const dialogRef: any = inject('dialogRef');

const rest: UseFetchApis = useFetch();

interface HarvestAuth {
  id: number;
  name: string;
  authorisingAgents: any[];
  permissions: any[];
}

const returnedHarvestAuths = ref<HarvestAuth[]>([]);
const loading = ref(false);
const loadingPermission = ref(false);

const harvestAuths = ref<{ id: number, name: string, agent: '', permissionId: number, startDate: '', endDate: '', urlPatterns: [] }[]>([]);
const filteredHarvestAuths = ref<{ id: number, name: string, agent: '', permissionId: number, startDate: '', endDate: '',  urlPatterns: []  }[]>([]);

const searchTerm = ref('');

const seed = ref();
let expandedPermission = reactive<Permission>({} as Permission);
const expandedRows = ref<any[]>([]);
const permissionStatuses = ref();

const prepareData = (data: HarvestAuth[]) => {
  data.forEach((harvestAuth: HarvestAuth) => {
    if (harvestAuth.authorisingAgents.length > 0) {
      harvestAuth.authorisingAgents.forEach((authorisingAgent: any) => {
        if (authorisingAgent.permissions.length > 0) {
          harvestAuths.value.push({
            id: harvestAuth.id,
            name: harvestAuth.name,
            agent: authorisingAgent.name,
            permissionId: authorisingAgent.permissions[0].id,
            startDate: authorisingAgent.permissions[0].startDate,
            endDate: authorisingAgent.permissions[0].endDate,
            urlPatterns: authorisingAgent.permissions[0].urlPatterns
          });
        }
      })
    }
  });
  filteredHarvestAuths.value = harvestAuths.value;
};

const setExpandedRow = async (event: DataTableRowClickEvent) => {
  loadingPermission.value = true;
  const isExpanded = (expandedRows.value as any[]).find((p) => p.id === event.data.id)

  if (isExpanded?.id) {
    expandedRows.value = [event.data] as any
    const fetchedPermission: any = await usePermissionStore().fetch(event.data.permissionId);
    
    expandedPermission = fetchedPermission;
  } else {
    expandedRows.value = []; 
    expandedPermission = ({} as Permission);
  }

  loadingPermission.value = false;
}

const fetch = () => {
  const searchParams = {
    offset: 0,
    limit: 1024,
  };

  loading.value = true;

  rest
    .post('harvest-authorisations', searchParams, { header: 'X-HTTP-Method-Override', value: 'GET' })
    .then((data: any) => {
      returnedHarvestAuths.value = data['harvestAuthorisations'];
      prepareData(returnedHarvestAuths.value);
      loading.value = false;
    })
    .catch((err: any) => {
      console.log(err.message);
      loading.value = false;
    });
};

const search = () => {
  const lowerCaseSearchTerm = searchTerm.value.toLowerCase();
  filteredHarvestAuths.value = harvestAuths.value.filter((g: any) => 
    g.name.toLowerCase().includes(lowerCaseSearchTerm) ||
    g.urlPatterns.some((urlPattern: string) => {
      // Ignore trailing slashes
      const trimmedUrlPattern = urlPattern.replace(/\/$/, '');
      const trimmedSearchTerm = lowerCaseSearchTerm.replace(/\/$/, '');
      return trimmedUrlPattern.includes(trimmedSearchTerm);
    })
  );
};

const isAuthAdded = (authPermissionId: number) => 
  seed.value.authorisations.some((a: { permissionId: number }) => a.permissionId === authPermissionId);

onMounted(async () => {
  seed.value = dialogRef.value.data.seed;
  const statuses = await usePermissionStatusStore().fetch();
  permissionStatuses.value = statuses;
});

fetch();
</script>

<template>
  <div class="h-full">
    <h5>Search</h5>
    <div class="flex mb-4">
      <InputText v-model="searchTerm" type="text" placeholder="Keyword" v-tooltip.bottom="'Search names and URL patterns'"
      class="mr-4" />
      <Button label="Search&nbsp;&nbsp;" icon="pi pi-search" iconPos="right" @click="search()" />
      <Button 
        class="ml-2 wct-secondary-button"
        label="Clear" icon="pi pi-times" 
        iconPos="right" 
        @click="searchTerm = ''; search()" 
      />
    </div>
    <Button v-if="seed" class="p-0" :label="`Search for ${seed.seed}`" text iconPos="right" @click="searchTerm = seed.seed; search()" />

    <DataTable
      v-model:expandedRows="expandedRows"
      class="w-full mt-4" 
      :value="filteredHarvestAuths" 
      size="small" 
      paginator :rows="10" 
      scrollHeight="100%" 
      :loading="loading" 
      pt:wrapper:class="h-26rem"
      @rowExpand="setExpandedRow"
    >
      <Column expander style="width: 5rem" />
      <Column field="name" header="Harvest Authorisation" />
      <Column field="agent" header="Authorising Agent" />
      <Column  header="URL Patterns">
        <template #body="slotProps">
          <div v-for="(urlPattern, index) in slotProps.data.urlPatterns" :key="index">
            {{ urlPattern }}
          </div>
        </template>
      </Column>
      <Column field="startDate" header="Start Date">
        <template #body="slotProps">
          {{  slotProps.data.startDate && formatDate(slotProps.data.startDate) }}
        </template>
      </Column>
      <Column field="endDate" header="End Date">
        <template #body="slotProps">
          {{ slotProps.data.endDate && formatDate(slotProps.data.endDate) }}
        </template>
      </Column>
      <Column>
        <template #body="slotProps">
          <div class="flex justify-content-center">
            <div v-if="isAuthAdded(slotProps.data.permissionId)" class="flex align-items-center">
              <i class="pi pi-check" />
              <Button icon="pi pi-trash" text v-tooltip.bottom="'Remove from Seed'" @click="seed.authorisations = seed.authorisations.filter((auth: any) => auth.permissionId !== data.permissionId)" />
            </div>
            <Button
              v-else
              class="p-0 m-0"
              label="Add"
              text
              v-tooltip.bottom="'Add to Seed'"
              @click="
                seed.authorisations.push({
                  id: slotProps.data.id,
                  name: slotProps.data.name,
                  agent: slotProps.data.agent,
                  permissionId: slotProps.data.permissionId,
                  startDate: slotProps.data.startDate,
                  endDate: slotProps.data.endDate,
                })
              "
            />
          </div>
        </template>
      </Column>

      <!-- Exapnded row is rendered here -->
      <template #expansion>
        <Loading v-if="loadingPermission" />
        <div v-else class="p-4">
          <div class="grid">
            <p class="col-4 p-2 font-semibold">Status:</p>
            <p class="col-8 p-2">{{ permissionStatuses[expandedPermission.status] }}</p>
          </div>
          <div class="grid">
            <p class="col-4 p-2 font-semibold">Auth Agency Response:</p>
            <p class="col-8 p-2">{{ permissionStatuses[expandedPermission.authResponse] }}</p>
          </div>
          <div class="grid">
            <p class="col-4 p-2 font-semibold">Quick Pick:</p>
            <p class="col-8 p-2">{{ expandedPermission.quickPick === true ? 'Yes' : 'No'  }}</p>
          </div>
          <div class="grid">
            <p class="col-4 p-2 font-semibold">Display Name:</p>
            <p class="col-8 p-2">{{ expandedPermission.displayName }}</p>
          </div>
          <div v-if="expandedPermission.exclusions.length > 0">
            <p class="font-semibold">Exclusions</p>
            <DataTable size="small" showGridlines class="w-full" :rowHover="true" :value="expandedPermission.exclusions">
              <Column field="url" header="URL" />
              <Column field="reason" header="Reason" />
            </DataTable>
          </div>
          <div v-if="expandedPermission.annotations.length > 0">
            <p class="font-semibold">Annotations</p>
            <DataTable size="small" showGridlines class="w-full" :rowHover="true" :value="expandedPermission.annotations" >
              <Column field="date" header="Date" />
              <Column field="user" header="User" />
              <Column field="notes" header="Notes" />
            </DataTable>
          </div>
        </div>
      </template>
    </DataTable>
  </div>
</template>
