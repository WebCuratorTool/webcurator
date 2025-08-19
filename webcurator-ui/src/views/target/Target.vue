<script setup lang="ts">
import { ref } from 'vue';
import { useRoute } from 'vue-router'
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { useToast } from "primevue/usetoast";
import {
    setTarget,
    useTargetDescriptionDTO,
    useTargetGeneralDTO,
    useTargetGropusDTO,
    useTargetProfileDTO,
    useTargetSeedsDTO,
    useTargetHarvestsDTO, 
    useNextStateStore 
} from '@/stores/target';
import TargetTabView from './target-tabs/TargetTabView.vue';

const route = useRoute()
const targetId = route.params.id as string

const rest: UseFetchApis = useFetch();
const toast = useToast();

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
}

const fetchTargetDetails = () => {
    isTargetAvailable.value = false;
    loading.value = true;

    rest.get('targets/' + targetId).then((data: any) => {        
        isTargetAvailable.value = true;
        setTarget(data);
        nextStates.setData(targetGeneral.selectedState, data.general.nextStates || []);
    }).catch((err: any) => {
        console.log(err.message);
        initData();
    }).finally(() => {
        loading.value = false;
    });
}

const save = () => {
  const dataReq = {
    general: targetGeneral.getData(),
    profile: targetProfile.getData(),
    description: targetDescription.getData(),
    groups: targetGroups.getData(),
    seeds: targetSeeds.getData(),
    schedule: targetHarvests.getData()
  }

  dataReq.profile.overrides.forEach((override) => {
    // Ensure blockedUrls and includedUrls are arrays
    if (override.id == 'blockedUrls' || override.id == 'includedUrls') {
      if (!Array.isArray(override.value)) {
        override.value = override.value.toString().split('\n');
      }
    }
  });

  rest.put('targets/' + targetGeneral.id, dataReq)
  .then((response: any) => {
    if (response == 200) {
      showSuccessMessage();
      editing.value = false
    }
  })
  .catch((err: any) => {
    showErrorMessage(err.message)
  })
}

const setEditing = (isEditing: boolean) => {
  editing.value = isEditing;
  if (!isEditing) {
    fetchTargetDetails();
  }
}

const showErrorMessage = (message: string) => {
  toast.add({ severity: 'error', summary: 'Target not saved', detail: message, life: 3000 });
};

const showSuccessMessage = () => {
  toast.add({ severity: 'success', summary: 'Target succesfully saved', life: 3000 });
};
fetchTargetDetails();

</script>

<template>
  <Toast />
  <TargetTabView 
    :editing=editing 
    :isTargetAvailable=isTargetAvailable
    :loading=loading
    @setEditing="setEditing"
    @save="save"    
  />
</template>