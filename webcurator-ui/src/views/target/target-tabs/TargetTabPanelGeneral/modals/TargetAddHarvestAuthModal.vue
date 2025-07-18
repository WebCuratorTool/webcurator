<script setup lang="ts">
import { formatDate } from '@/utils/helper';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { inject, onMounted, ref } from 'vue';

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

const harvestAuths = ref<{ id: number; name: string; agent: ''; permissionId: number; startDate: ''; endDate: ''; urlPatterns: [] }[]>([]);
const filteredHarvestAuths = ref<{ id: number; name: string; agent: ''; permissionId: number; startDate: ''; endDate: ''; urlPatterns: [] }[]>([]);

const searchTerm = ref('');

const seed = ref();

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
      });
    }
  });
  filteredHarvestAuths.value = harvestAuths.value;
};

const fetch = () => {
  const searchParams = {
    offset: 0,
    limit: 1024
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
  filteredHarvestAuths.value = harvestAuths.value.filter(
    (g: any) =>
      g.name.toLowerCase().includes(lowerCaseSearchTerm) ||
      g.urlPatterns.some((urlPattern: string) => {
        // Ignore trailing slashes
        const trimmedUrlPattern = urlPattern.replace(/\/$/, '');
        const trimmedSearchTerm = lowerCaseSearchTerm.replace(/\/$/, '');
        return trimmedUrlPattern.includes(trimmedSearchTerm);
      })
  );
};

const isAuthAdded = (authPermissionId: number) => seed.value.authorisations.some((a: { permissionId: number }) => a.permissionId === authPermissionId);

onMounted(() => {
  seed.value = dialogRef.value.data.seed;
});

fetch();
</script>

<template>
  <div class="h-full">
    <h5>Search</h5>
    <div class="flex mb-4 gap-4">
      <InputText v-model="searchTerm" type="text" placeholder="Keyword" v-tooltip.bottom="'Search names and URL patterns'" />
      <Button class="wct-primary-button" label="Search&nbsp;&nbsp;" icon="pi pi-search" iconPos="right" @click="search()" />
      <Button
        class="ml-2 wct-primary-button"
        label="Clear"
        icon="pi pi-times"
        iconPos="right"
        @click="
          searchTerm = '';
          search();
        "
      />
    </div>
    <Button
      v-if="seed"
      class="p-0"
      :label="`Search for ${seed.seed}`"
      text
      iconPos="right"
      @click="
        searchTerm = seed.seed;
        search();
      "
    />

    <DataTable
      class="w-full mt-4"
      :value="filteredHarvestAuths"
      size="small"
      paginator
      :rows="10"
      scrollHeight="100%"
      :loading="loading"
      pt:wrapper:class="h-26rem"
      :pt="{
        // Use 'pcPaginator' to target the internal Paginator component to align to the right side
        pcPaginator: {
          root: '!flex !justify-end !items-center !p-4 w-full',
          paginatorContainer: '!border-none'
        }
      }"
    >
      <Column expander style="width: 5rem" />
      <Column field="name" header="Name" />
      <Column field="agent" header="Authorising Agent" />
      <Column header="URL Patterns">
        <template #body="{ data }">
          <div v-for="(urlPattern, index) in data.urlPatterns" :key="index">
            {{ urlPattern }}
          </div>
        </template>
      </Column>
      <Column field="startDate" header="Start Date">
        <template #body="{ data }">
          {{ data.startDate && formatDate(data.startDate) }}
        </template>
      </Column>
      <Column field="endDate" header="End Date">
        <template #body="{ data }">
          {{ data.endDate && formatDate(data.endDate) }}
        </template>
      </Column>
      <Column>
        <template #body="{ data }">
          <div class="flex items-center justify-center">
            <div v-if="isAuthAdded(data.permissionId)" class="flex items-center">
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
                  id: data.id,
                  name: data.name,
                  agent: data.agent,
                  permissionId: data.permissionId,
                  startDate: data.startDate,
                  endDate: data.endDate
                })
              "
            />
          </div>
        </template>
      </Column>
    </DataTable>
  </div>
</template>
