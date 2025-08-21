<script setup lang="ts">
// libraries
import { inject, ref } from 'vue';
import { useToast } from 'primevue/usetoast';

// stores
import { useTargetSeedsDTO } from '@/stores/target';

const dialogRef: any = inject('dialogRef');
const toast = useToast();

const targetSeeds = useTargetSeedsDTO();

const newSeed = ref({ seed: '', authorisations: [], primary: false });
const selectedAuthorisationOption = ref('Auto');

const addSeed = () => {
  if (newSeed.value.seed != '') {
    if (targetSeeds.targetSeeds.some((t) => t.seed == newSeed.value.seed)) {
      toast.add({ severity: 'error', summary: 'Seed not added', detail: 'The seed already exists on the target', life: 3000 });
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