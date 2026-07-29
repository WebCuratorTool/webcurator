<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";

import { useTargetGropusDTO } from "@/stores/target";
import type { TargetGroup, TargetGroups } from "@/types/target";
import { useFetch, type UseFetchApis } from "@/utils/rest.api";
const { t } = useI18n();

const rest: UseFetchApis = useFetch();

const targetGroups = useTargetGropusDTO();

const groups = ref(<TargetGroups>[]);
const filteredGroups = ref(<TargetGroups>[]);
const loading = ref(false);

const searchTerm = ref("");

const states: Record<number, string> = {
  8: t("target.general.groups.states.pending"),
  9: t("target.general.groups.states.active"),
  10: t("target.general.groups.states.inactive"),
};

interface TargetGroupsResponse {
  filter: Record<string, unknown>;
  groups: TargetGroups;
  amount: number;
  offset: number;
  limit: number;
}

const fetch = () => {
  const searchParams = {
    offset: 0,
    limit: 1024,
  };

  loading.value = true;

  rest
    .post("groups", searchParams, {
      header: "X-HTTP-Method-Override",
      value: "GET",
    })
    .then((data: unknown) => {
      const resp = data as TargetGroupsResponse;
      groups.value = resp.groups;
      filteredGroups.value = groups.value;
    })
    .finally(() => {
      loading.value = false;
    });
};

const search = () => {
  const lowerCaseSearchTerm = searchTerm.value.toLowerCase();
  filteredGroups.value = groups.value.filter(
    (g: TargetGroup) =>
      g.name.toLowerCase().includes(lowerCaseSearchTerm) ||
      (g.agency && g.agency.toLowerCase().includes(lowerCaseSearchTerm)),
  );
};

const isGroupAdded = (id: number) => {
  return targetGroups.targetGroups.some((t: TargetGroup) => t.id == id);
};

fetch();
</script>

<template>
  <div class="h-full">
    <h5>{{ t("common.search") }}</h5>
    <div class="flex mb-4">
      <InputText v-model="searchTerm" type="text" class="mr-4" />
      <Button
        class="wct-primary-button"
        :label="t('common.search') + '\u00A0\u00A0'"
        icon="pi pi-search"
        iconPos="right"
        @click="search()"
      />
    </div>

    <Divider type="dotted" />

    <div class="flex flex-wrap gap-2">
      <Chip
        v-for="group in targetGroups.targetGroups"
        style="padding: 0 4px"
        :key="group.id"
      >
        <span class="p-2 m-0">{{ group.name }}</span>
        <Button
          class="p-0 m-0"
          icon="pi pi-times-circle"
          style="width: 2rem"
          link
          @click="targetGroups.removeGroup(group.id)"
        />
      </Chip>
    </div>

    <Divider type="dotted" />

    <DataTable
      class="w-full"
      :value="filteredGroups"
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
        },
      }"
    >
      <Column field="name" :header="t('common.name')" />
      <Column field="state" :header="t('common.status')">
        <template #body="{ data }">
          {{ states[data.state] }}
        </template>
      </Column>
      <Column field="agency" :header="t('common.agency')" sortable />
      <Column>
        <template #body="{ data }">
          <div class="flex justify-center">
            <i v-if="isGroupAdded(data.id)" class="pi pi-check" />
            <Button
              v-else
              class="p-0 m-0"
              :label="t('common.add')"
              text
              @click="targetGroups.addGroup(data)"
            />
          </div>
        </template>
      </Column>
    </DataTable>
  </div>
</template>
