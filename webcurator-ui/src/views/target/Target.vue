<script setup lang="ts">
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { setTarget, useTargetDescriptionDTO, useTargetGeneralDTO, useTargetGropusDTO, useTargetProfileDTO, useTargetSeedsDTO, useTargetHarvestsDTO, useNextStateStore } from '@/stores/target';
import TargetTabView from './target-tabs/TargetTabView.vue';
import { useAlertStore } from '@/utils/alertStore';

const route = useRoute();
const targetId = route.params.id as string;

const rest: UseFetchApis = useFetch();
const alertStore = useAlertStore();

const targetGeneral = useTargetGeneralDTO();
const targetProfile = useTargetProfileDTO();
const targetDescription = useTargetDescriptionDTO();
const targetSeeds = useTargetSeedsDTO();
const targetGroups = useTargetGropusDTO();
const targetHarvests = useTargetHarvestsDTO();
const nextStates = useNextStateStore();

const editing = ref(false);
const isTargetAvailable = ref(false);
const loading = ref(false);

const initData = () => {
  isTargetAvailable.value = false;
  targetGeneral.initData();
  nextStates.initData();
};

const fetchTargetDetails = () => {
  isTargetAvailable.value = false;
  loading.value = true;

  rest
    .get('targets/' + targetId)
    .then((data: any) => {
      isTargetAvailable.value = true;
      setTarget(data);
      nextStates.setData(targetGeneral.selectedState, data.general.nextStates || []);
    })
    .catch((err: any) => {
      console.log(err.message);
      initData();
    })
    .finally(() => {
      loading.value = false;
    });
};

const save = () => {
  try {
    const dataReq = {
      general: targetGeneral.getData(),
      profile: targetProfile.getData(),
      description: targetDescription.getData(),
      groups: targetGroups.getData(),
      seeds: targetSeeds.getData(),
      schedule: targetHarvests.getData()
    };

    rest
      .put('targets/' + targetGeneral.id, dataReq)
      .then((response: any) => {
        if (response == 200) {
          showSuccessMessage();
          editing.value = false;
        }
      })
      .catch((err: any) => {
        showErrorMessage(err.message);
      });
  } catch (err: any) {
    showErrorMessage(err.message);
  }
};

const setEditing = (isEditing: boolean) => {
  editing.value = isEditing;
  if (!isEditing) {
    fetchTargetDetails();
  }
};

const showErrorMessage = (message: string) => {
  alertStore.error(message, 'Target not saved');
};

const showSuccessMessage = () => {
  alertStore.info('Target succesfully saved');
};
fetchTargetDetails();
</script>

<template>
  <TargetTabView :editing="editing" :isTargetAvailable="isTargetAvailable" :loading="loading" @setEditing="setEditing" @save="save" />
</template>
