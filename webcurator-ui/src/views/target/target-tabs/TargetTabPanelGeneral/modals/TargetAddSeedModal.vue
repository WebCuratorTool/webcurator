<script setup lang="ts">
import type { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { inject, type Ref, ref } from "vue";

import { useTargetSeedsDTO } from "@/stores/target";
import { useAlertStore } from "@/utils/alertStore";

const dialogRef = inject<Ref<DynamicDialogInstance>>("dialogRef");
const alertStore = useAlertStore();
const targetSeeds = useTargetSeedsDTO();

const newSeed = ref({ seed: "", authorisations: [], primary: false });
const selectedAuthorisationOption = ref("Auto");

const showErrorMessage = () => {
  const message = "The seed already exists on the target";
  alertStore.error(message, message, "Seed not added");
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
  <WctTopLabel label="Seed URL">
    <InputText v-model="newSeed.seed" />
  </WctTopLabel>
  <div class="flex items-end justify-between w-full gap-4 my-4">
    <WctTopLabel label="Authorisation" class="w-2/3">
      <Select
        v-model="selectedAuthorisationOption"
        :options="['Auto', 'Add Later']"
      />
    </WctTopLabel>
    <Button class="wct-primary-button" label="Add" @click="addSeed" />
  </div>
</template>
