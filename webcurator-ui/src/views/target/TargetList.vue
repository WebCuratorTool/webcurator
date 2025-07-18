<script setup lang="ts">
import { watch } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';

import PageHeader from '@/components/PageHeader.vue';
import WctTabViewPanel from '@/components/WctTabViewPanel.vue';
import WctTopLabel from '@/components/WctTopLabel.vue';
import { useAgenciesStore } from '@/stores/agencies';
import { formatTargetState, showTargetAction, stateList } from '@/stores/target';
import { useTargetListDataStore } from '@/stores/targetList';
import { useUserProfileStore, useUsersStore } from '@/stores/users';
import { formatDate } from '@/utils/helper';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';

const router = useRouter();
const confirm = useConfirm();
const toast = useToast();

const rest: UseFetchApis = useFetch();
const userProfile = useUserProfileStore();
const users = useUsersStore();
const agencies = useAgenciesStore();
const targetListData = useTargetListDataStore();

const createNew = () => {
  if (router) {
    router.push('/wct/targets/new/');
  }
};

const deleteTarget = (id: number) => {
  confirm.require({
    message: `Are you sure you want to delete target ${id}?`,
    header: 'Confirm Delete',
    icon: 'pi pi-info-circle',
    rejectLabel: 'Cancel',
    acceptLabel: 'Delete',
    rejectClass: 'p-button-secondary p-button-outlined',
    acceptClass: 'p-button-danger',
    accept: () => {
      rest
        .delete('targets/' + id, {})
        .then((rsp: any) => {
          toast.add({
            severity: 'info',
            summary: 'Confirmed',
            detail: `Target ${id} deleted`,
            life: 3000
          });
          targetListData.search();
        })
        .catch((err: any) => {
          toast.add({ severity: 'error', summary: 'Error', detail: err.message, life: 3000 });
        });
    }
  });
};

watch(userProfile, (newUserProfile, oldUserProfile) => {
  console.log(userProfile);
  targetListData.resetFilter();
});
</script>

<template>
  <Toast />
  <ConfirmDialog></ConfirmDialog>

  <div class="targets">
    <PageHeader title="Targets" />
  </div>
  <div class="flex flex-col justify-start w-5/6 ">
    



    <h5>Query</h5>
    <div class="flex items-end justify-between w-full gap-2 mb-4">
      <div class="flex items-center justify-start w-full gap-4" id="grid-search">
        <WctTopLabel label="Target ID" class="w-15">
          <InputNumber v-model="targetListData.searchTerms.targetId" :useGrouping="false" pt:pcInputText:root:class="max-w-full" />
        </WctTopLabel>
        <WctTopLabel label="Target Name" class="w-2xs">
          <InputText v-model="targetListData.searchTerms.targetName" type="text" />
        </WctTopLabel>
        <WctTopLabel label="Seed" class="w-2xs">
          <InputText v-model="targetListData.searchTerms.targetSeed" type="text" />
        </WctTopLabel>
        <WctTopLabel label="Description" class="w-2xs">
          <InputText v-model="targetListData.searchTerms.targetDescription" type="text" />
        </WctTopLabel>
        <WctTopLabel label="Member of">
          <InputText v-model="targetListData.searchTerms.targetMemberOf" type="text" />
        </WctTopLabel>
        <!-- <WctTopLabel label="Non-Display Only">
          <div class="flex items-center justify-center gap-4 border" style="padding: 0.5rem; border-color: var(--p-inputtext-border-color)">
            <label for="none-display-only">Option</label>
            <Checkbox v-model="targetListData.searchTerms.noneDisplayOnly" :binary="true" inputId="none-display-only" />
          </div>
        </WctTopLabel> -->
      </div>
      <Button class="wct-primary-button" label="Search&nbsp;&nbsp;" icon="pi pi-search" iconPos="right" id="search-button" @click="targetListData.search()" />
    </div>

    <div class="flex items-center justify-start gap-2" style="width: 70%">
      <InputGroup class="w-full md:w-20rem">
        <InputGroupAddon pt:root:class="!text-gray-700">Agency</InputGroupAddon>
        <Select
          id="agency"
          v-model="targetListData.filters.selectedAgency"
          :options="agencies.agencyListWithEmptyItem"
          optionLabel="name"
          placeholder="Select an Agency"
          class="w-full md:w-18rem"
          showClear
        />
      </InputGroup>

      <InputGroup class="w-full md:w-20rem">
        <InputGroupAddon pt:root:class="!text-gray-700">User</InputGroupAddon>
        <Select 
          id="user" 
          v-model="targetListData.filters.selectedUser" 
          :options="users.userListWithEmptyItem" 
          optionLabel="name" 
          placeholder="Select a User" 
          class="w-full md:w-18rem" 
          showClear 
        />
      </InputGroup>

      <InputGroup class="w-full md:w-20rem">
        <InputGroupAddon pt:root:class="!text-gray-700">State</InputGroupAddon>
        <MultiSelect 
          v-model="targetListData.filters.selectedState" 
          :options="stateList" optionLabel="name" 
          placeholder="Select States" 
          :maxSelectedLabels="3" 
          class="w-full md:w-20rem" 
          showClear 
        />
      </InputGroup>

      <div class="flex items-center justify-between border rounded-md w-3/5" style="padding: 0.5rem; border-color: var(--p-inputtext-border-color)">
        <label for="non-display-only">Non-Display Only</label>
        <Checkbox v-model="targetListData.searchTerms.noneDisplayOnly" :binary="true" inputId="non-display-only" />
      </div>

      <!-- <div class="flex items-center justify-center gap-2 w-full">
        <Button @click="targetListData.resetFilter" label="&nbsp;&nbsp;Reset filter" icon="pi pi-times" outlined fluid />
        <Button @click="targetListData.filter" label="&nbsp;&nbsp;Filter" icon="pi pi-filter" outlined fluid />
      </div> -->
    </div>

    <Divider type="solid" />

    <div class="mb-8">
      <div class="flex justify-between">
        <h4>Results</h4>
        <Button icon="pi pi-plus" label="Create New" text @click="createNew" />
      </div>
      <WctTabViewPanel>
        <DataTable
          class="w-full"
          :value="targetListData.filteredTargetList"
          size="small"
          :paginator="true"
          :rows="10"
          :rowsPerPageOptions="[10, 20, 50, 100]"
          dataKey="oid"
          :rowHover="true"
          filterDisplay="menu"
          :loading="targetListData.loadingTargetList"
          :globalFilterFields="['name', 'country.name', 'representative.name', 'balance', 'status']"
          resizableColumns
          columnResizeMode="fit"
          :pt="{
            // Use 'pcPaginator' to target the internal Paginator component to align to the right side
            pcPaginator: {
              root: '!flex !justify-end !items-center !p-4 w-full border-none',
              paginatorContainer: '!border-none'
            }
          }"
        >
          <!-- <template #header>
            <div class="flex items-center justify-between gap-2 p-2">
              <span class="text-xl text-900 font-bold">Results</span>
              <Button class="wct-primary-button" @click="createNew" label="Create new" />
            </div>
          </template> -->
          <template #empty> No targets found. </template>
          <template #loading> Loading target list. Please wait. </template>
          <Column field="id" sortable header="Id" dataType="numeric" style="min-width: 2rem"></Column>
          <Column field="creationDate" header="Date" sortable dataType="date" style="min-width: 5rem">
            <template #body="{ data }">
              {{ formatDate(data.creationDate) }}
            </template>
          </Column>
          <Column field="name" header="Name" sortable style="min-width: 8rem">
            <template #body="{ data }">
              <router-link :to="`/wct/targets/${data.id}`">{{ data.name }}</router-link>
            </template>
          </Column>
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
                <span v-if="seed.primary" style="font-weight: bold">{{ seed.seed }}</span>
                <span v-else>{{ seed.seed }}</span>
              </div>
            </template>
          </Column>
          <Column header="Action" field="id" style="max-width: 8rem">
            <template #body="{ data }">
              <Button v-if="showTargetAction(data, 'copy')" icon="pi pi-copy" text />
              <Button v-if="showTargetAction(data, 'delete')" icon="pi pi-trash" @click="deleteTarget(data.id)" text />
            </template>
          </Column>
        </DataTable>
      </WctTabViewPanel>
    </div>
  </div>
</template>

<style lang="scss" scoped>
#grid-search label {
  text-align: left;
}

.btn-sub {
  font-size: 1em;
}

#search-button {
  /* margin-top: 22px;
  margin-left: 120px; */
  padding: 0.5rem 2rem;
}

.toolbar {
  border: 0px;
  margin: 0px;
  padding-top: 0;
  padding-bottom: 0;
}

#actions img {
  width: 1rem;
  height: 1rem;
  padding: 0;
}

#actions button {
  padding: 0 0.5rem;
}
</style>
