<script setup lang="ts">
import { useConfirm } from "primevue/useconfirm";
import { onMounted } from "vue";
import { watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";

import Loading from "@/components/Loading.vue";
import WctTabViewPanel from "@/components/WctTabViewPanel.vue";
import WctTopLabel from "@/components/WctTopLabel.vue";
import { useAgenciesStore } from "@/stores/agencies";
import {
  formatTargetState,
  showTargetAction,
  stateList,
} from "@/stores/target";
import { useTargetListDataStore } from "@/stores/targetList";
import { useUserProfileStore, useUsersStore } from "@/stores/users";
import { useAlertStore } from "@/utils/alertStore";
import { formatDate } from "@/utils/helper";
import { useFetch, type UseFetchApis } from "@/utils/rest.api";

const router = useRouter();
const confirm = useConfirm();
const alertStore = useAlertStore();
const { t } = useI18n();

const rest: UseFetchApis = useFetch();
const userProfile = useUserProfileStore();
const users = useUsersStore();
const agencies = useAgenciesStore();
const targetListData = useTargetListDataStore();

type TargetTextQueryField = {
  key: "targetName" | "targetSeed" | "targetDescription" | "targetMemberOf";
  labelKey: string;
};

const textQueryFields: TargetTextQueryField[] = [
  {
    key: "targetName",
    labelKey: "target.list.query.targetName",
  },
  {
    key: "targetSeed",
    labelKey: "target.list.query.seed",
  },
  {
    key: "targetDescription",
    labelKey: "common.description",
  },
  {
    key: "targetMemberOf",
    labelKey: "target.list.query.memberOf",
  },
];

const createNew = () => {
  if (router) {
    router.push("/targets/new/");
  }
};

const deleteTarget = (id: number) => {
  confirm.require({
    message: t("target.list.delete.confirmMessage", { id }),
    header: t("target.list.delete.confirmHeader"),
    icon: "pi pi-info-circle",
    rejectLabel: t("common.cancel"),
    acceptLabel: t("common.delete"),
    rejectClass: "p-button-secondary p-button-outlined",
    acceptClass: "p-button-danger",
    accept: () => {
      rest
        .delete("targets/" + id, {})
        .then(() => {
          const message = t("target.list.delete.deleted", { id });
          alertStore.info(message, message, t("target.list.delete.confirmed"));
          targetListData.search();
        })
        .catch((err: unknown) => {
          const msg = err as Error;
          alertStore.error(msg.message);
        });
    },
  });
};

watch(
  () => userProfile.id,
  (newUserId, oldUserId) => {
    if (oldUserId !== undefined && newUserId !== oldUserId) {
      targetListData.resetFilter();
    }
  },
);

onMounted(() => {
  targetListData.search();
});
</script>

<template>
  <Toast />
  <ConfirmDialog></ConfirmDialog>

  <p class="title pt-8">{{ t("target.targets") }}</p>

  <div class="flex flex-col justify-start w-full">
    <h5>{{ t("target.list.query.query") }}</h5>
    <div class="flex items-end justify-between w-full mb-4">
      <div
        class="flex items-center justify-start sm:w-5/6 gap-4"
        id="grid-search"
      >
        <WctTopLabel :label="t('target.list.query.targetId')" class="w-26">
          <InputNumber
            v-model="targetListData.searchTerms.targetId"
            :useGrouping="false"
            pt:pcInputText:root:class="max-w-full"
          />
        </WctTopLabel>
        <WctTopLabel
          v-for="field in textQueryFields"
          :key="field.key"
          :label="t(field.labelKey)"
          class="flex-grow"
        >
          <InputText
            v-model="targetListData.searchTerms[field.key]"
            type="text"
          />
        </WctTopLabel>
      </div>
      <Button
        class="wct-primary-button max-w-25"
        :label="t('common.search')"
        icon="pi pi-search"
        id="search-button"
        @click="targetListData.search()"
      />
    </div>

    <div class="flex items-center justify-between w-full mb-8">
      <div
        class="flex items-center justify-start sm:w-5/6 gap-4"
        id="grid-search"
      >
        <InputGroup>
          <InputGroupAddon pt:root:class="!text-gray-700">{{
            t("common.agency")
          }}</InputGroupAddon>
          <Select
            id="agency"
            v-model="targetListData.searchTerms.targetAgency"
            :options="agencies.agencyListWithEmptyItem"
            optionLabel="name"
            :placeholder="t('target.list.query.selectAgency')"
            showClear
          />
        </InputGroup>

        <InputGroup>
          <InputGroupAddon pt:root:class="!text-gray-700">{{
            t("common.user")
          }}</InputGroupAddon>
          <Select
            id="user"
            v-model="targetListData.searchTerms.targetUser"
            :options="users.userListWithEmptyItem"
            optionLabel="name"
            :placeholder="t('target.list.query.selectUser')"
            showClear
          />
        </InputGroup>

        <InputGroup>
          <InputGroupAddon pt:root:class="!text-gray-700">{{
            t("common.state")
          }}</InputGroupAddon>
          <MultiSelect
            v-model="targetListData.searchTerms.targetState"
            :options="stateList"
            optionLabel="name"
            :placeholder="t('target.list.query.selectStates')"
            :maxSelectedLabels="3"
            showClear
          />
        </InputGroup>

        <div
          class="flex items-center justify-between flex-grow border rounded-md w-2/3"
          style="padding: 0.5rem; border-color: var(--p-inputtext-border-color)"
        >
          <label for="non-display-only">{{
            t("target.list.query.nonDisplayOnly")
          }}</label>
          <Checkbox
            v-model="targetListData.searchTerms.nonDisplayOnly"
            :binary="true"
            inputId="non-display-only"
          />
        </div>
      </div>
      <Button
        @click="targetListData.resetFilter()"
        class="max-w-25"
        :label="t('common.clear')"
        icon="pi pi-times"
        outlined
        fluid
      />
    </div>

    <Divider type="solid" />

    <div class="mb-8">
      <div class="flex justify-between">
        <h4>{{ t("target.list.results") }}</h4>
        <Button
          icon="pi pi-plus"
          :label="t('target.list.createNew')"
          text
          @click="createNew"
        />
      </div>
      <WctTabViewPanel>
        <Loading v-if="targetListData.loadingTargetList" />
        <div v-else>
          <DataTable
            v-if="targetListData.targetList && targetListData.targetList.length"
            class="w-full"
            :value="targetListData.targetList"
            size="small"
            dataKey="oid"
            :rowHover="true"
            filterDisplay="menu"
            columnResizeMode="fit"
            resizableColumns
          >
            <Column
              field="id"
              sortable
              :header="t('common.id')"
              dataType="numeric"
              class="w-26"
            />
            <Column
              field="creationDate"
              :header="t('common.date')"
              sortable
              dataType="date"
              class="w-30"
            >
              <template #body="{ data }">
                {{ formatDate(data.creationDate) }}
              </template>
            </Column>
            <Column field="name" :header="t('common.name')" sortable>
              <template #body="{ data }">
                <router-link :to="`/targets/${data.id}`">{{
                  data.name
                }}</router-link>
              </template>
            </Column>
            <Column
              field="agency"
              :header="t('common.agency')"
              sortable
              class="w-30"
            />
            <Column
              field="owner"
              :header="t('common.owner')"
              sortable
              filterField="owner"
              class="w-30"
            />
            <Column
              field="state"
              :header="t('common.state')"
              sortable
              class="w-30"
            >
              <template #body="{ data }">
                {{ formatTargetState(data.state) }}
              </template>
            </Column>
            <Column :header="t('target.general.seeds.seed')" field="seed">
              <template #body="{ data }">
                <div v-for="seed in data.seeds" :key="seed">
                  <span v-if="seed.primary" style="font-weight: bold">{{
                    seed.seed
                  }}</span>
                  <span v-else>{{ seed.seed }}</span>
                </div>
              </template>
            </Column>
            <Column :header="t('common.action')" field="id">
              <template #body="{ data }">
                <Button
                  v-if="showTargetAction(data.state, 'copy')"
                  icon="pi pi-copy"
                  text
                />
                <Button
                  v-if="showTargetAction(data.state, 'delete')"
                  icon="pi pi-trash"
                  @click="deleteTarget(data.id)"
                  text
                />
              </template>
            </Column>
            <template #footer>
              <div class="flex justify-end w-full">
                <Paginator
                  :pageLinkSize="3"
                  :first="targetListData.pageState.first"
                  :rows="targetListData.pageState.rows"
                  :totalRecords="targetListData.pageState.totalRecords"
                  :rowsPerPageOptions="[10, 20, 50, 100]"
                  @page="targetListData.updatePage($event.first, $event.rows)"
                  template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown CurrentPageReport"
                  :currentPageReportTemplate="
                    t('target.list.pagination.currentPageReportTemplate')
                  "
                >
                </Paginator>
              </div>
            </template>
          </DataTable>
          <div v-else class="text-center">
            <p class="text-500">{{ t("target.list.noTargetsFound") }}</p>
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
