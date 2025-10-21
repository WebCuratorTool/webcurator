<script setup lang="ts">
// libraries
import { inject, ref } from 'vue';

// stores
import { useTargetSeedsDTO } from '@/stores/target';
// utils
import { useAlertStore } from '@/utils/alertStore';

const dialogRef: any = inject('dialogRef');
const alertStore = useAlertStore();
const targetSeeds = useTargetSeedsDTO();

const newSeed = ref({ seed: '', authorisations: [], primary: false });
const selectedAuthorisationOption = ref('Auto');

const showErrorMessage = () => {
  const message = 'The seed already exists on the target';
  alertStore.error(message, message, 'Seed not added');
};

const addSeed = () => {
  if (newSeed.value.seed != '') {
    if (targetSeeds.targetSeeds.some((t) => t.seed == newSeed.value.seed)) {
      showErrorMessage();
    } else {
      targetSeeds.addSeed(newSeed.value);
      newSeed.value = { seed: '', authorisations: [], primary: false };
      dialogRef.value.close();
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
      <Select v-model="selectedAuthorisationOption" :options="['Auto', 'Add Later']" />
    </WctTopLabel>
    <Button class="wct-primary-button" label="Add" @click="addSeed" />
  </div>
</template>