<script setup lang="ts">
import type { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { inject, onMounted, type Ref, ref } from "vue";
import { useI18n } from "vue-i18n";

import Loading from "@/components/Loading.vue";
import WctFormField from "@/components/WctFormField.vue";
import { useHarvestAuthorisationStatusStore } from "@/stores/harvestAuthorisations";
import { usePermissionStore } from "@/stores/permission";
import type { Permission } from "@/types/permission";
import { formatDate } from "@/utils/helper";

const dialogRef = inject<Ref<DynamicDialogInstance>>("dialogRef");
const { t } = useI18n();

const permission = ref<Permission>({} as Permission);
const loading = ref(true);
const permissionStatuses = ref<{ [key: string]: string }>({});

onMounted(async () => {
  try {
    await usePermissionStore().fetch(dialogRef?.value.data.permissionId);
    permission.value = usePermissionStore().permission;
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
      <WctFormField :label="t('target.general.permission.authorisingAgent')">
        <p class="font-semibold">{{ permission.authorisingAgent.name }}</p>
      </WctFormField>
      <WctFormField :label="t('target.general.permission.dates')">
        <p class="font-semibold">
          {{ formatDate(permission.startDate) }} -
          {{ formatDate(permission.endDate) }}
        </p>
      </WctFormField>
      <WctFormField :label="t('common.status')">
        <p class="font-semibold">{{ permissionStatuses[permission.status] }}</p>
      </WctFormField>
      <WctFormField :label="t('target.general.permission.authAgencyResponse')">
        <p class="font-semibold">{{ permission.authResponse }}</p>
      </WctFormField>
      <WctFormField :label="t('target.general.permission.quickPick')">
        <p class="font-semibold">
          {{ permission.quickPick === true ? t("common.yes") : t("common.no") }}
        </p>
      </WctFormField>
      <WctFormField :label="t('target.general.permission.displayName')">
        <p class="font-semibold">{{ permission.displayName }}</p>
      </WctFormField>
      <WctFormField :label="t('target.general.permission.urls')">
        <p
          v-for="(url, index) in permission.urlPatterns"
          :key="index"
          class="font-semibold !mb-0"
        >
          {{ url }}
        </p>
      </WctFormField>

      <p class="font-semibold">
        {{ t("target.general.permission.exclusions") }}
      </p>
      <DataTable
        v-if="permission.exclusions.length > 0"
        size="small"
        showGridlines
        class="mb-4 w-full"
        :rowHover="true"
        :value="permission.exclusions"
      >
        <Column field="url" :header="t('target.general.permission.url')" />
        <Column field="reason" :header="t('common.reason')" />
      </DataTable>
      <div v-else class="text-center mb-4">
        <p class="text-500">
          {{ t("target.general.permission.noExclusions") }}
        </p>
      </div>

      <p class="font-semibold">{{ t("target.annotations") }}</p>
      <DataTable
        v-if="permission.annotations.length > 0"
        size="small"
        showGridlines
        class="w-full"
        :rowHover="true"
        :value="permission.annotations"
      >
        <Column field="date" :header="t('common.date')" />
        <Column field="user" :header="t('common.user')" />
        <Column field="notes" :header="t('common.notes')" />
      </DataTable>
      <div v-else class="text-center">
        <p class="text-500">
          {{ t("target.general.permission.noAnnotations") }}
        </p>
      </div>
    </div>
  </div>
</template>
