<script setup lang="ts">
import { Select } from "primevue";
import { ref } from "vue";
import { useI18n } from "vue-i18n";

import WctFormField from "@/components/WctFormField.vue";
import WctTabViewPanel from "@/components/WctTabViewPanel.vue";
import { useTargetAccessDTO } from "@/stores/target";

const data = useTargetAccessDTO();
const targetAccess = data.targetAccess;
const { t } = useI18n();

defineProps<{
  editing: boolean;
}>();

const accessZones = ref([
  { label: t("target.accessPanel.public"), value: "Public" },
  { label: t("target.accessPanel.onsite"), value: "Onsite" },
  { label: t("target.accessPanel.restricted"), value: "Restricted" },
]);
</script>

<template>
  <WctTabViewPanel>
    <div class="flex items-start justify-between gap-4">
      <div class="w-full">
        <WctFormField
          :label="t('target.accessPanel.displayTarget')"
          inputId="display-target"
        >
          <Checkbox
            v-if="editing"
            v-model="targetAccess.displayTarget"
            :binary="true"
            :disabled="!editing"
            inputId="display-target"
          />
          <p v-else class="font-semibold">
            {{ targetAccess.displayTarget ? t("common.yes") : t("common.no") }}
          </p>
        </WctFormField>
        <WctFormField :label="t('target.accessPanel.accessZone')">
          <Select
            v-if="editing"
            v-model="targetAccess.accessZoneText"
            :options="accessZones"
            optionLabel="label"
            optionValue="value"
          />
          <p v-else class="font-semibold">{{ targetAccess.accessZoneText }}</p>
        </WctFormField>
        <WctFormField :label="t('target.accessPanel.displayNote')">
          <Textarea
            v-if="editing"
            v-model="targetAccess.displayNote"
            :disabled="!editing"
          />
          <p v-else class="font-semibold">{{ targetAccess.displayNote }}</p>
        </WctFormField>
      </div>
      <div class="w-full">
        <WctFormField :label="t('target.accessPanel.displayChangeReason')">
          <Textarea
            v-if="editing"
            v-model="targetAccess.displayChangeReason"
            :disabled="!editing"
          />
          <p v-else class="font-semibold">
            {{ targetAccess.displayChangeReason }}
          </p>
        </WctFormField>
      </div>
    </div>
  </WctTabViewPanel>
</template>

<style></style>
