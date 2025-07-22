<script setup lang="ts">
import { watch } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';

import Loading from '@/components/Loading.vue';
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

  <p class="title pt-8">Targets</p>

  <div class="flex flex-col justify-start 2xl:w-5/6">
    <h5>Query</h5>
    <div class="flex items-end justify-between w-full mb-4">
      <div class="flex items-center justify-start w-3/4 2xl:w-5/6 gap-4" id="grid-search">
        <WctTopLabel label="Target ID" class="w-15">
          <InputNumber v-model="targetListData.searchTerms.targetId" :useGrouping="false" pt:pcInputText:root:class="max-w-full" />
        </WctTopLabel>
        <WctTopLabel label="Target Name" class="md:w-xs">
          <InputText v-model="targetListData.searchTerms.targetName" type="text" />
        </WctTopLabel>
        <WctTopLabel label="Seed" class="md:w-xs">
          <InputText v-model="targetListData.searchTerms.targetSeed" type="text" />
        </WctTopLabel>
        <WctTopLabel label="Description" class="md:w-xs ">
          <InputText v-model="targetListData.searchTerms.targetDescription" type="text" />
        </WctTopLabel>
        <WctTopLabel label="Member of">
          <InputText v-model="targetListData.searchTerms.targetMemberOf" type="text" />
        </WctTopLabel>
      </div>
      <Button class="wct-primary-button max-w-25" label="Search" icon="pi pi-search"  id="search-button" @click="targetListData.search()" />
    </div>

    <div class="flex items-center justify-between w-full mb-8" >
      <div class="flex items-center justify-start w-3/4 2xl:w-5/6 gap-4" id="grid-search">
        <InputGroup>
          <InputGroupAddon pt:root:class="!text-gray-700">Agency</InputGroupAddon>
          <Select
            id="agency"
            v-model="targetListData.searchTerms.targetAgency"
            :options="agencies.agencyListWithEmptyItem"
            optionLabel="name"
            placeholder="Select an Agency"
            showClear
          />
        </InputGroup>
  
        <InputGroup>
          <InputGroupAddon pt:root:class="!text-gray-700">User</InputGroupAddon>
          <Select 
            id="user" 
            v-model="targetListData.searchTerms.targetUser" 
            :options="users.userListWithEmptyItem" 
            optionLabel="name" 
            placeholder="Select a User" 
            showClear 
          />
        </InputGroup>
  
        <InputGroup>
          <InputGroupAddon pt:root:class="!text-gray-700">State</InputGroupAddon>
          <MultiSelect 
            v-model="targetListData.searchTerms.targetState" 
            :options="stateList" optionLabel="name" 
            placeholder="Select States" 
            :maxSelectedLabels="3" 
            showClear 
          />
        </InputGroup>
  
        <div class="flex items-center justify-between border rounded-md w-2/3" style="padding: 0.5rem; border-color: var(--p-inputtext-border-color)">
          <label for="non-display-only">Non-Display Only</label>
          <Checkbox v-model="targetListData.searchTerms.nonDisplayOnly" :binary="true" inputId="non-display-only" />
        </div>
      </div>
      <Button @click="targetListData.resetFilter" class="max-w-25" label="Clear" icon="pi pi-times" outlined fluid />
    </div>

    <Divider type="solid" />

    <div class="mb-8">
      <div class="flex justify-between">
        <h4>Results</h4>
        <Button icon="pi pi-plus" label="Create New" text @click="createNew" />
      </div>
      <WctTabViewPanel>
        <Loading v-if="targetListData.loadingTargetList" />
        <div v-else>
          <DataTable
            v-if="targetListData.targetList && targetListData.targetList.length"
            class="w-full"
            :value="targetListData.targetList"
            size="small"
            :paginator="true"
            :rows="10"
            :rowsPerPageOptions="[10, 20, 50, 100]"
            dataKey="oid"
            :rowHover="true"
            filterDisplay="menu"
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
            <Column field="id" sortable header="Id" dataType="numeric" class="w-15"/>
            <Column field="creationDate" header="Date" sortable dataType="date" class="w-30">
              <template #body="{ data }">
                {{ formatDate(data.creationDate) }}
              </template>
            </Column>
            <Column field="name" header="Name" sortable>
              <template #body="{ data }">
                <router-link :to="`/wct/targets/${data.id}`">{{ data.name }}</router-link>
              </template>
            </Column>
            <Column field="agency" header="Agency" sortable class="w-30" />
            <Column field="owner" header="Owner" sortable filterField="owner" class="w-30" />
            <Column field="state" header="Status" sortable class="w-30">
              <template #body="{ data }">
                {{ formatTargetState(data.state) }}
              </template>
            </Column>
            <Column header="Seed" field="seed">
              <template #body="{ data }">
                <div v-for="seed in data.seeds" :key="seed">
                  <span v-if="seed.primary" style="font-weight: bold">{{ seed.seed }}</span>
                  <span v-else>{{ seed.seed }}</span>
                </div>
              </template>
            </Column>
            <Column header="Action" field="id">
              <template #body="{ data }">
                <Button v-if="showTargetAction(data, 'copy')" icon="pi pi-copy" text />
                <Button v-if="showTargetAction(data, 'delete')" icon="pi pi-trash" @click="deleteTarget(data.id)" text />
              </template>
            </Column>
          </DataTable>
          <div v-else class="text-center">
            <p class="text-500">No targets found</p>
          </div>
        </div>
       
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
