<script setup lang="ts">
import { inject, onMounted, ref } from 'vue';
import { formatDate } from '@/utils/helper';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';

const dialogRef: any = inject('dialogRef');

const rest: UseFetchApis = useFetch();

interface HarvestAuth {
  id: number;
  name: string;
  authorisingAgents: any[];
  permissions: any[];
}

const harvestAuths = ref<HarvestAuth[]>([]);
const filteredHarvestAuths = ref<HarvestAuth[]>([]);
const loading = ref(false);

const preppedAuths = ref<{ id: number, name: string, agent: '', permissionId: number, startDate: '', endDate: '' }[]>([]);

const searchTerm = ref('');

const seed = ref();

const prepareData = (data: HarvestAuth[]) => {
  data.forEach((harvestAuth: HarvestAuth) => {
    if (harvestAuth.authorisingAgents.length > 0) {
      harvestAuth.authorisingAgents.forEach((authorisingAgent: any) => {
        if (authorisingAgent.permissions.length > 0) {
          preppedAuths.value.push({
            id: harvestAuth.id,
            name: harvestAuth.name,
            agent: authorisingAgent.name,
            permissionId: authorisingAgent.permissions[0].id,
            startDate: authorisingAgent.permissions[0].startDate,
            endDate: authorisingAgent.permissions[0].endDate
          });
        }
      })
    }
  });
};

const fetch = () => {
  const searchParams = {
    offset: 0,
    limit: 1024,
  };

  loading.value = true;

  rest
    .post('harvest-authorisations', searchParams, { header: 'X-HTTP-Method-Override', value: 'GET' })
    .then((data: any) => {
      harvestAuths.value = data['harvestAuthorisations'];
      filteredHarvestAuths.value = harvestAuths.value;
      prepareData(harvestAuths.value);
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
    g.name.toLowerCase().includes(lowerCaseSearchTerm)
  );
};

const isAuthAdded = (authPermissionId: number) => 
  seed.value.authorisations.some((a: { permissionId: number }) => a.permissionId === authPermissionId);

onMounted(() => {
  seed.value = dialogRef.value.data.seed;
});

fetch();
</script>

<template>
  <div class="h-full">
    <h5>Search</h5>
    <div class="flex mb-4">
      <InputText v-model="searchTerm" type="text" class="mr-4" />
      <Button label="Search&nbsp;&nbsp;" icon="pi pi-search" iconPos="right" @click="search()" />
    </div>

    <DataTable class="w-full" :value="preppedAuths" size="small" paginator :rows="10" scrollHeight="100%" :loading="loading" pt:wrapper:class="h-26rem">
      <Column expander style="width: 5rem" />
      <Column field="name" header="Name" />
      <Column field="agent" header="Authorising Agent" />
      <Column field="startDate" header="Start Date">
        <template #body="{ data }">
          {{ formatDate(data.startDate) }}
        </template>
      </Column>
      <Column field="endDate" header="End Date">
        <template #body="{ data }">
          {{ formatDate(data.endDate) }}
        </template>
      </Column>
      <Column>
        <template #body="{ data }">
          <div class="flex justify-content-center">
            <div v-if="isAuthAdded(data.permissionId)" class="flex align-items-center">
              <i class="pi pi-check" />
              <Button icon="pi pi-trash" text @click="seed.authorisations = seed.authorisations.filter((auth: any) => auth.permissionId !== data.permissionId)" />
            </div>
            <Button
              v-else
              class="p-0 m-0"
              label="Add"
              text
              @click="
                seed.authorisations.push({
                  id: data.id,
                  name: data.name,
                  agent: data.agent,
                  permissionId: data.permissionId,
                  startDate: data.startDate,
                  endDate: data.endDate,
                })
              "
            />
          </div>
        </template>
      </Column>
    </DataTable>
  </div>
</template>
