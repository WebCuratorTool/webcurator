<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router';
import { useConfirm } from "primevue/useconfirm";
import { useToast } from "primevue/usetoast";
import { type UseFetchApis, useFetch } from '@/utils/rest.api'
import { formatDatetime } from '@/utils/helper'
import { useUsersStore, useUserProfileStore } from '@/stores/users'
import { useAgenciesStore } from '@/stores/agencies'
import { stateList, formatTargetState, showTargetAction } from '@/stores/target'

import PageHeader from '@/components/PageHeader.vue'

const router = useRouter()
const confirm = useConfirm();
const toast = useToast();

const options = defineProps(['props'])

const emit = defineEmits(['popPage'])

const rest: UseFetchApis = useFetch()
const userProfile = useUserProfileStore()
const users = useUsersStore()
const agencies = useAgenciesStore()

// Search conditions
const targetId = ref(null)
const targetName = ref(null)
const targetSeed = ref(null)
const targetDescription = ref(null)
const targetMemberOf = ref(null)

// Filter conditions
const selectedAgency = ref({ name: "", code: "" })
const selectedUser = ref({ name: "", code: "" })

const noneDisplayOnly = ref(false)
const selectedState = ref([])

const targetList = ref([])
const loadingTargetList = ref(false)
const filteredTargetList = ref()
const filter = () => {
  const ret = [];
  for (let idx = 0; idx < targetList.value.length; idx++) {
    const target: any = targetList.value[idx];
    if (selectedAgency.value.code !== "" && target.agency !== selectedAgency.value.code) {
      continue;
    }

    if (selectedUser.value.code !== "" && target.owner !== selectedUser.value.code) {
      continue;
    }

    let isInSelectedStates = false;
    for (const idx in selectedState.value) {
      const stateOption: any = selectedState.value[idx];
      if (target.state === stateOption.code) {
        isInSelectedStates = true;
        break;
      }
    }
    if (selectedState.value.length > 0 && !isInSelectedStates) {
      continue;
    }
    ret.push(target);
  }
  filteredTargetList.value = ret;
}

const resetFilter = () => {
  selectedUser.value = {
    name: userProfile.currUserName,
    code: userProfile.name
  }

  selectedAgency.value = {
    name: userProfile.agency,
    code: userProfile.agency
  }

  selectedState.value = [];
}


const search = () => {
  const searchConditions = {
    targetId: targetId.value,
    name: targetName.value,
    seed: targetSeed.value,
    description: targetDescription.value,
    groupName: targetMemberOf.value,
    nonDisplayOnly: noneDisplayOnly.value,
  }

  const searchParams = {
    filter: searchConditions,
    offset: 0,
    limit: 1024,
    sortBy: 'creationDate,asc'
  }

  loadingTargetList.value = true
  rest
    .post('targets', searchParams)
    .then((data: any) => {
      console.log(data)
      targetList.value = data['targets']
      console.log(targetList.value)
      filter();
      loadingTargetList.value = false
    })
    .catch((err: any) => {
      console.log(err.message)
      loadingTargetList.value = false
    })
}

const createNew = () => {
  if (router) {
    router.push('/wct/targets/new/')
  }
}

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
          toast.add({ severity: 'info', summary: 'Confirmed', detail: `Target ${id} deleted`, life: 3000 });
          search();
        })
        .catch((err: any) => {
          toast.add({ severity: 'error', summary: 'Error', detail: err.message, life: 3000 });
        })
    }
  })

}

watch(userProfile, (newUserProfile, oldUserProfile) => {
  console.log(userProfile)
  resetFilter();
});

onMounted(() => {
  search();
});
</script>

<template>
  <Toast />
  <ConfirmDialog></ConfirmDialog>

  <div class="targets">
    <PageHeader title="Targets" />
  </div>
  <div class="main-content grid">
    <div class="col-12 surface-section">
      <h5>Query</h5>
      <div class="grid">
        <div class="col-10">
          <div class="p-fluid formgrid grid" id="grid-search">
            <div class="field col-12 md:col-2">
              <label>Target ID</label>
              <InputNumber v-model="targetId" :useGrouping="false" />
            </div>
            <div class="field col-12 md:col-2">
              <label>Target Name</label>
              <InputText v-model="targetName" type="text" />
            </div>
            <div class="field col-12 md:col-2">
              <label>Seed</label>
              <InputText v-model="targetSeed" type="text" />
            </div>
            <div class="field col-12 md:col-2">
              <label>Description</label>
              <InputText v-model="targetDescription" type="text" />
            </div>
            <div class="field col-12 md:col-2">
              <label>Member of</label>
              <InputText v-model="targetMemberOf" type="text" />
            </div>
            <div class="field col-12 md:col-2">
              <label>Non-Display Only</label>
              <Checkbox v-model="noneDisplayOnly" :binary="true" />
            </div>
          </div>
        </div>

        <div class="col-2">
          <Button label="Search&nbsp;&nbsp;" icon="pi pi-search" iconPos="right" id="search-button"
            @click="search()"></Button>
        </div>
      </div>

      <div class="formgroup-inline">
        <div class="field">
          <InputGroup class="w-full md:w-20rem">
            <InputGroupAddon>Agency</InputGroupAddon>
            <Dropdown id="agency" v-model="selectedAgency" :options="agencies.agencyListWithEmptyItem"
              optionLabel="name" placeholder="Select an Agency" class="w-full md:w-18rem">
              <template #value="slotProps">
                <div class="flex align-items-center">
                  <div>{{ selectedAgency.name }}</div>
                </div>
              </template>
              <template #option="slotProps">
                <div class="flex align-items-center">
                  <div>{{ slotProps.option.name }}</div>
                </div>
              </template>
            </Dropdown>
          </InputGroup>
        </div>
        <div class="field">
          <InputGroup class="w-full md:w-20rem">
            <InputGroupAddon>User</InputGroupAddon>
            <Dropdown id="user" v-model="selectedUser" :options="users.userListWithEmptyItem" optionLabel="name"
              placeholder="Select an User" class="w-full md:w-18rem">
              <template #value="slotProps">
                <div class="flex align-items-center">
                  <div>{{ selectedUser.name }}</div>
                </div>
              </template>
              <template #option="slotProps">
                <div class="flex align-items-center">
                  <div>{{ slotProps.option.name }}</div>
                </div>
              </template>
            </Dropdown>
          </InputGroup>
        </div>

        <div class="field">
          <InputGroup class="w-full md:w-20rem">
            <InputGroupAddon>State</InputGroupAddon>
            <MultiSelect v-model="selectedState" :options="stateList" optionLabel="name" placeholder="Select States"
              :maxSelectedLabels="3" class="w-full md:w-20rem" />
          </InputGroup>
        </div>

        <div class="field">
          <Button @click="resetFilter" label="&nbsp;&nbsp;Reset filter" icon="pi pi-times"
            class="wct-secondary-button" />
        </div>
        <div class="field">
          <Button @click="filter" label="&nbsp;&nbsp;Filter" icon="pi pi-filter" class="wct-secondary-button" />
        </div>
      </div>
    </div>

    <Divider type="dotted" />

    <!-- <div class="col-12 surface-section"> -->
    <DataTable class="w-full" :value="filteredTargetList" size="small" :paginator="true" :rows="10" dataKey="oid"
      :rowHover="true" filterDisplay="menu" :loading="loadingTargetList"
      :globalFilterFields="['name', 'country.name', 'representative.name', 'balance', 'status']" resizableColumns
      columnResizeMode="fit" showGridlines>
      <template #header>
        <!-- <div class="flex justify-content-between flex-column sm:flex-row">
          <h5>Results</h5> -->
        <div class="flex flex-wrap align-items-center justify-content-between gap-2">
          <span class="text-xl text-900 font-bold">Results</span>
          <Button severity="primary" @click="createNew">Create new</Button>
        </div>
      </template>
      <template #empty> No targets found. </template>
      <template #loading> Loading target list. Please wait. </template>
      <Column field="id" sortable header="Id" dataType="numeric" style="min-width: 2rem"></Column>
      <Column field="creationDate" header="Date" sortable dataType="date" style="min-width: 5rem">
        <template #body="{ data }">
          {{ formatDatetime(data.creationDate) }}
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
          <Button v-if="showTargetAction(data, 'copy')"  icon="pi pi-copy" text />
          <Button v-if="showTargetAction(data, 'delete')" icon="pi pi-trash" @click="deleteTarget(data.id)" text />
        </template>
      </Column>
    </DataTable>
  </div>
  <!-- </div> -->
</template>

<style>
#grid-search label {
  text-align: left;
}

.btn-sub {
  font-size: 1em;
}

#search-button {
  margin-top: 22px;
  margin-left: 120px;
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
