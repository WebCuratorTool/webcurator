<script setup lang="ts">
import { useI18n } from "vue-i18n";

import WctFormField from "@/components/WctFormField.vue";
import WctTabViewPanel from "@/components/WctTabViewPanel.vue";
import {
  formatTargetState,
  useNextStateStore,
  useTargetGeneralDTO,
} from "@/stores/target";
import { useUsersStore } from "@/stores/users";

import TargetTabPanelGeneralGroups from "./TargetTabPanelGeneralGroups.vue";
import TargetTabPanelGeneralSeeds from "./TargetTabPanelGeneralSeeds.vue";

defineProps<{
  editing: boolean;
  validationErrors: string;
}>();

const targetGeneral = useTargetGeneralDTO();
const users = useUsersStore();
const nextStates = useNextStateStore();
const { t } = useI18n();
</script>

<template>
  <!-- References -->
  <h4 class="mt-4">{{ t("target.general.references.references") }}</h4>
  <WctTabViewPanel columns>
    <div class="flex items-start justify-between gap-8 w-full">
      <div class="flex flex-col items-start gap-2 w-full">
        <WctFormField :label="t('common.id')">
          <InputText
            v-if="editing"
            v-model="targetGeneral.id"
            :disabled="true"
          />
          <p v-else class="font-semibold">{{ targetGeneral.id }}</p>
        </WctFormField>

        <WctFormField :label="t('common.name')">
          <InputText
            v-if="editing"
            v-model="targetGeneral.name"
            :disabled="!editing"
            :invalid="!!validationErrors"
            :formControl="{ validateOnValueUpdate: true }"
          />
          <p v-else class="font-semibold">{{ targetGeneral.name }}</p>
          <Message
            v-if="validationErrors"
            severity="error"
            size="small"
            variant="simple"
          >
            {{ validationErrors }}
          </Message>
        </WctFormField>

        <WctFormField :label="t('common.description')">
          <Textarea
            v-if="editing"
            v-model="targetGeneral.description"
            autoResize
            rows="6"
            :disabled="!editing"
          />
          <p v-else class="font-semibold">{{ targetGeneral.description }}</p>
        </WctFormField>
      </div>
      <div class="flex flex-col items-start gap-2 w-full">
        <WctFormField :label="t('target.general.references.owner')">
          <Select
            v-if="editing"
            id="user"
            v-model="targetGeneral.selectedUser"
            :options="users.userListWithEmptyItem"
            optionLabel="name"
            optionValue="code"
            :placeholder="t('target.general.references.selectUser')"
            class="w-full md:w-18rem"
            :disabled="!editing"
          />
          <p v-else class="font-semibold">
            {{ targetGeneral.selectedUserName }}
          </p>
        </WctFormField>

        <WctFormField :label="t('target.general.references.referenceNumber')">
          <InputText
            v-if="editing"
            v-model="targetGeneral.referenceNumber"
            :disabled="!editing"
          />
          <p v-else class="font-semibold">
            {{ targetGeneral.referenceNumber }}
          </p>
        </WctFormField>

        <WctFormField
          :label="t('target.general.references.runOnApproval')"
          inputId="run-on-approval"
        >
          <Checkbox
            v-if="editing"
            id="checkOption1"
            name="option1"
            value="Run on Approval"
            v-model="targetGeneral.runOnApproval"
            :binary="true"
            :disabled="!editing"
            inputId="run-on-approval"
          />
          <p v-else class="font-semibold">
            {{ targetGeneral.runOnApproval ? t("common.yes") : t("common.no") }}
          </p>
        </WctFormField>

        <WctFormField :label="t('common.state')">
          <Select
            v-if="editing"
            id="state"
            v-model="targetGeneral.selectedState"
            :options="nextStates.nextStateList"
            optionLabel="name"
            optionGroupLabel="name"
            optionGroupChildren="items"
            class="w-full md:w-18rem"
            :disabled="!editing"
          />
          <p v-else class="font-semibold">
            {{ formatTargetState(targetGeneral.selectedState) }}
          </p>
        </WctFormField>
      </div>
    </div>
  </WctTabViewPanel>

  <!-- Groups -->
  <TargetTabPanelGeneralGroups :editing="editing" />

  <!-- Seeds -->
  <TargetTabPanelGeneralSeeds :editing="editing" />

  <!-- Archive options -->
  <h4>{{ t("target.general.archiveOptions.archiveOptions") }}</h4>
  <WctTabViewPanel columns>
    <div class="flex items-start justify-between gap-8 w-full">
      <div class="flex flex-col items-start gap-2 w-full">
        <WctFormField
          checkbox
          :label="t('target.general.archiveOptions.useAutomatedQA')"
          inputId="automated-qa"
        >
          <Checkbox
            v-if="editing"
            v-model="targetGeneral.automatedQA"
            :binary="true"
            :disabled="!editing"
            inputId="automated-qa"
          />
          <p v-else class="font-semibold">
            {{ targetGeneral.automatedQA ? t("common.yes") : t("common.no") }}
          </p>
        </WctFormField>
        <WctFormField
          checkbox
          :label="t('target.general.archiveOptions.autoPrune')"
          inputId="auto-prune"
        >
          <Checkbox
            v-if="editing"
            v-model="targetGeneral.autoPrune"
            :binary="true"
            :disabled="!editing"
            inputId="auto-prune"
          />
          <p v-else class="font-semibold">
            {{ targetGeneral.autoPrune ? t("common.yes") : t("common.no") }}
          </p>
        </WctFormField>
        <WctFormField
          checkbox
          :label="t('target.general.archiveOptions.referenceCrawl')"
          inputId="reference-crawl"
        >
          <Checkbox
            v-if="editing"
            v-model="targetGeneral.referenceCrawl"
            :binary="true"
            :disabled="!editing"
            inputId="reference-crawl"
          />
          <p v-else class="font-semibold">
            {{
              targetGeneral.referenceCrawl ? t("common.yes") : t("common.no")
            }}
          </p>
        </WctFormField>
      </div>

      <div class="flex flex-col items-start gap-2 w-full">
        <WctFormField
          :label="t('target.general.archiveOptions.requestToArchivists')"
        >
          <Textarea
            v-if="editing"
            v-model="targetGeneral.requestToArchivists"
            autoResize
            rows="6"
            :disabled="!editing"
          />
          <p v-else class="font-semibold">
            {{ targetGeneral.requestToArchivists }}
          </p>
        </WctFormField>
      </div>
    </div>
  </WctTabViewPanel>
</template>

<style></style>
