<script setup lang="ts">
import type { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { inject, type Ref, ref } from "vue";
import { useI18n } from "vue-i18n";

import { useTargetSeedsDTO } from "@/stores/target";
import { useAlertStore } from "@/utils/alertStore";

const dialogRef = inject<Ref<DynamicDialogInstance>>("dialogRef");
const alertStore = useAlertStore();
const targetSeeds = useTargetSeedsDTO();
const { t } = useI18n();

const newSeed = ref({ seed: "", authorisations: [], primary: false });
const selectedAuthorisationOption = ref("Auto");

const showErrorMessage = () => {
  const message = t("target.general.seeds.seedExists");
  alertStore.error(message, message, t("target.general.seeds.seedNotAdded"));
};

const addSeed = () => {
  if (newSeed.value.seed != "") {
    if (targetSeeds.targetSeeds.some((t) => t.seed == newSeed.value.seed)) {
      showErrorMessage();
    } else {
      targetSeeds.addSeed(newSeed.value);
      newSeed.value = { seed: "", authorisations: [], primary: false };
      dialogRef?.value.close();
    }
  }
};
</script>

<template>
  <WctTopLabel :label="t('target.general.seeds.seedUrl')">
    <InputText v-model="newSeed.seed" />
  </WctTopLabel>
  <div class="flex items-end justify-between w-full gap-4 my-4">
    <WctTopLabel :label="t('target.general.seeds.authorisation')" class="w-2/3">
      <Select
        v-model="selectedAuthorisationOption"
        :options="[
          t('target.general.seeds.auto'),
          t('target.general.seeds.addLater'),
        ]"
      />
    </WctTopLabel>
    <Button
      class="wct-primary-button"
      :label="t('common.add')"
      @click="addSeed"
    />
  </div>
</template>
