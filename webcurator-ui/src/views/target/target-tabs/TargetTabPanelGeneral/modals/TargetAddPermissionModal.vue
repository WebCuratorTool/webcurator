<script setup lang="ts">
import type { DataTableRowClickEvent } from "primevue/datatable";
import type { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { inject, onMounted, reactive, type Ref, ref } from "vue";
import { useRoute } from "vue-router";

import Loading from "@/components/Loading.vue";
import { useHarvestAuthorisationStatusStore } from "@/stores/harvestAuthorisations";
import { usePermissionStore } from "@/stores/permission";
import { usePermissionsStore } from "@/stores/permissions";
import type { Permission } from "@/types/permission";
import { formatDate } from "@/utils/helper";

const dialogRef = inject<Ref<DynamicDialogInstance>>("dialogRef");

const route = useRoute();
const targetId = route.params.id as string;

const loadingPermission = ref(false);

const permissions = ref<Permission[]>([]);

const searchTerm = ref("");
const searchType = ref("harvestAuthorisationName");
const currentPage = ref(0);

const seed = ref();
let expandedPermission = reactive<Permission>({} as Permission);
const expandedRows = ref<Permission[]>([]);
const harvestAuthorisationStatuses = ref();

const setExpandedRow = async (event: DataTableRowClickEvent) => {
  loadingPermission.value = true;
  const isExpanded = (expandedRows.value as Permission[]).find(
    (p) => p.id === event.data.id,
  );

  if (isExpanded?.id) {
    expandedRows.value = [event.data] as Permission[];
    await usePermissionStore().fetch(event.data.id);
    expandedPermission = usePermissionStore().permission;
  } else {
    expandedRows.value = [];
    expandedPermission = {} as Permission;
  }

  loadingPermission.value = false;
};

const fetchPermissions = async () => {
  try {
    const searchParams = {
      page: currentPage.value,
      filter: {
        targetId: targetId,
        url: searchType.value === "url" ? searchTerm.value : undefined,
        harvestAuthorisationName:
          searchType.value === "harvestAuthorisationName"
            ? searchTerm.value
            : undefined,
      },
    };
    permissions.value = await usePermissionsStore().search(searchParams);
  } catch (error) {
    console.error("Error fetching permissions:", error);
  }
};

const clearSearch = () => {
  searchTerm.value = "";
  currentPage.value = 0;
  fetchPermissions();
};

const isAuthAdded = (authPermissionId: number) =>
  seed.value.authorisations.some(
    (a: { permissionId: number }) => a.permissionId === authPermissionId,
  );

onMounted(async () => {
  seed.value = dialogRef?.value.data.seed;
  const statuses = await useHarvestAuthorisationStatusStore().fetch();
  harvestAuthorisationStatuses.value = statuses;
});

fetchPermissions();
</script>

<template>
  <div class="h-full">
    <h5>Search</h5>
    <div class="flex flex-wrap gap-4 mb-4">
      <div class="flex items-center gap-2">
        <RadioButton
          v-model="searchType"
          inputId="harvestAuthorisationName"
          name="searchType"
          value="harvestAuthorisationName"
        />
        <label for="harvestAuthorisationName">Harvest Authorisation</label>
      </div>
      <div class="flex items-center gap-2">
        <RadioButton
          v-model="searchType"
          inputId="url"
          name="searchType"
          value="url"
        />
        <label for="url">URL</label>
      </div>
    </div>
    <div class="flex mb-4">
      <InputText
        v-model="searchTerm"
        type="text"
        placeholder="Keyword"
        v-tooltip.bottom="'Search for Harvest Authorisation or URL pattern'"
        class="mr-4"
      />
      <Button
        label="Search&nbsp;&nbsp;"
        icon="pi pi-search"
        iconPos="right"
        @click="fetchPermissions()"
      />
      <Button
        class="ml-2 wct-secondary-button"
        label="Clear"
        icon="pi pi-times"
        iconPos="right"
        @click="searchTerm && clearSearch()"
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
        searchType = 'url';
        fetchPermissions();
      "
    />

    <DataTable
      v-model:expandedRows="expandedRows"
      class="w-full mt-4"
      :value="permissions"
      size="small"
      :rows="10"
      scrollHeight="100%"
      :loading="usePermissionsStore().loadingPermissions"
      @rowExpand="setExpandedRow"
    >
      <Column expander style="width: 5rem" />
      <Column
        field="harvestAuthorisation.name"
        header="Harvest Authorisation"
      />
      <Column field="authorisingAgentName" header="Authorising Agent" />
      <Column header="URL Patterns">
        <template #body="slotProps">
          <div
            v-for="(urlPattern, index) in slotProps.data.urlPatterns"
            :key="index"
          >
            {{ urlPattern }}
          </div>
        </template>
      </Column>
      <Column field="startDate" header="Start Date">
        <template #body="slotProps">
          {{ slotProps.data.startDate && formatDate(slotProps.data.startDate) }}
        </template>
      </Column>
      <Column field="endDate" header="End Date">
        <template #body="slotProps">
          {{ slotProps.data.endDate && formatDate(slotProps.data.endDate) }}
        </template>
      </Column>
      <Column>
        <template #body="slotProps">
          <div class="flex justify-center">
            <div
              v-if="isAuthAdded(slotProps.data.permissionId)"
              class="flex items-center"
            >
              <i class="pi pi-check" />
              <Button
                icon="pi pi-trash"
                text
                v-tooltip.bottom="'Remove from Seed'"
                @click="
                  seed.authorisations = seed.authorisations.filter(
                    (auth: any) =>
                      auth.permissionId !== slotProps.data.permissionId,
                  )
                "
              />
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
          <div class="grid grid-cols-5">
            <p class="font-semibold">Status:</p>
            <p class="col-span-4">
              {{
                expandedPermission.status &&
                harvestAuthorisationStatuses[expandedPermission.status]
              }}
            </p>
          </div>
          <div class="grid grid-cols-5">
            <p class="font-semibold">Auth Agency Response:</p>
            <p class="col-span-4">
              {{
                expandedPermission.authResponse &&
                harvestAuthorisationStatuses[expandedPermission.authResponse]
              }}
            </p>
          </div>
          <div class="grid grid-cols-5">
            <p class="font-semibold">Quick Pick:</p>
            <p class="col-span-4">
              {{ expandedPermission.quickPick === true ? "Yes" : "No" }}
            </p>
          </div>
          <div class="grid grid-cols-5">
            <p class="font-semibold">Display Name:</p>
            <p class="col-span-4">{{ expandedPermission.displayName }}</p>
          </div>
          <div
            v-if="
              expandedPermission.exclusions &&
              expandedPermission.exclusions.length > 0
            "
          >
            <p class="font-semibold">Exclusions</p>
            <DataTable
              size="small"
              showGridlines
              class="w-full"
              :rowHover="true"
              :value="expandedPermission.exclusions"
            >
              <Column field="url" header="URL" />
              <Column field="reason" header="Reason" />
            </DataTable>
          </div>
          <div
            v-if="
              expandedPermission.annotations &&
              expandedPermission.annotations.length > 0
            "
            class="mt-4"
          >
            <p class="font-semibold">Annotations</p>
            <DataTable
              size="small"
              showGridlines
              class="w-full"
              :rowHover="true"
              :value="expandedPermission.annotations"
            >
              <Column field="date" header="Date" />
              <Column field="user" header="User" />
              <Column field="notes" header="Notes" />
            </DataTable>
          </div>
        </div>
      </template>
      <template #footer>
        <div class="flex justify-end w-full">
          <Paginator
            :first="currentPage * 10"
            :rows="10"
            :totalRecords="usePermissionsStore().amount"
            @page="((currentPage = $event.page), fetchPermissions())"
          />
        </div>
      </template>
    </DataTable>
  </div>
</template>
