<script setup lang="ts">
import type { DataTableRowClickEvent } from "primevue/datatable";
import type { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { inject, onMounted, reactive, type Ref, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";

import Loading from "@/components/Loading.vue";
import { useHarvestAuthorisationStatusStore } from "@/stores/harvestAuthorisations";
import { usePermissionStore } from "@/stores/permission";
import { usePermissionsStore } from "@/stores/permissions";
import type { Permission } from "@/types/permission";
import { formatDate } from "@/utils/helper";

const dialogRef = inject<Ref<DynamicDialogInstance>>("dialogRef");
const { t } = useI18n();

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
    <h5>{{ t("common.search") }}</h5>
    <div class="flex flex-wrap gap-4 mb-4">
      <div class="flex items-center gap-2">
        <RadioButton
          v-model="searchType"
          inputId="harvestAuthorisationName"
          name="searchType"
          value="harvestAuthorisationName"
        />
        <label for="harvestAuthorisationName">{{
          t("target.general.permission.harvestAuthorisation")
        }}</label>
      </div>
      <div class="flex items-center gap-2">
        <RadioButton
          v-model="searchType"
          inputId="url"
          name="searchType"
          value="url"
        />
        <label for="url">{{ t("target.general.permission.url") }}</label>
      </div>
    </div>
    <div class="flex mb-4">
      <InputText
        v-model="searchTerm"
        type="text"
        :placeholder="t('common.keyword')"
        v-tooltip.bottom="t('target.general.permission.searchForHarvestOrUrl')"
        class="mr-4"
      />
      <Button
        :label="t('common.search') + '\u00A0\u00A0'"
        icon="pi pi-search"
        iconPos="right"
        @click="fetchPermissions()"
      />
      <Button
        class="ml-2 wct-secondary-button"
        :label="t('common.clear')"
        icon="pi pi-times"
        iconPos="right"
        @click="searchTerm && clearSearch()"
      />
    </div>
    <Button
      v-if="seed"
      class="p-0"
      :label="t('target.general.permission.searchForSeed', { seed: seed.seed })"
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
        :header="t('target.general.permission.harvestAuthorisation')"
      />
      <Column
        field="authorisingAgentName"
        :header="t('target.general.permission.authorisingAgent')"
      />
      <Column :header="t('target.general.permission.urlPatterns')">
        <template #body="slotProps">
          <div
            v-for="(urlPattern, index) in slotProps.data.urlPatterns"
            :key="index"
          >
            {{ urlPattern }}
          </div>
        </template>
      </Column>
      <Column
        field="startDate"
        :header="t('target.general.permission.startDate')"
      >
        <template #body="slotProps">
          {{ slotProps.data.startDate && formatDate(slotProps.data.startDate) }}
        </template>
      </Column>
      <Column field="endDate" :header="t('target.general.permission.endDate')">
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
                v-tooltip.bottom="t('target.general.permission.removeFromSeed')"
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
              :label="t('common.add')"
              text
              v-tooltip.bottom="t('target.general.permission.addToSeed')"
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
            <p class="font-semibold">
              {{ t("target.general.permission.status") }}:
            </p>
            <p class="col-span-4">
              {{
                expandedPermission.status &&
                harvestAuthorisationStatuses[expandedPermission.status]
              }}
            </p>
          </div>
          <div class="grid grid-cols-5">
            <p class="font-semibold">
              {{ t("target.general.permission.authAgencyResponse") }}:
            </p>
            <p class="col-span-4">
              {{
                expandedPermission.authResponse &&
                harvestAuthorisationStatuses[expandedPermission.authResponse]
              }}
            </p>
          </div>
          <div class="grid grid-cols-5">
            <p class="font-semibold">
              {{ t("target.general.permission.quickPick") }}:
            </p>
            <p class="col-span-4">
              {{
                expandedPermission.quickPick === true
                  ? t("common.yes")
                  : t("common.no")
              }}
            </p>
          </div>
          <div class="grid grid-cols-5">
            <p class="font-semibold">
              {{ t("target.general.permission.displayName") }}:
            </p>
            <p class="col-span-4">{{ expandedPermission.displayName }}</p>
          </div>
          <div
            v-if="
              expandedPermission.exclusions &&
              expandedPermission.exclusions.length > 0
            "
          >
            <p class="font-semibold">
              {{ t("target.general.permission.exclusions") }}
            </p>
            <DataTable
              size="small"
              showGridlines
              class="w-full"
              :rowHover="true"
              :value="expandedPermission.exclusions"
            >
              <Column
                field="url"
                :header="t('target.general.permission.url')"
              />
              <Column field="reason" :header="t('common.reason')" />
            </DataTable>
          </div>
          <div
            v-if="
              expandedPermission.annotations &&
              expandedPermission.annotations.length > 0
            "
            class="mt-4"
          >
            <p class="font-semibold">{{ t("target.annotations") }}</p>
            <DataTable
              size="small"
              showGridlines
              class="w-full"
              :rowHover="true"
              :value="expandedPermission.annotations"
            >
              <Column field="date" :header="t('common.date')" />
              <Column field="user" :header="t('common.user')" />
              <Column field="notes" :header="t('common.notes')" />
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
