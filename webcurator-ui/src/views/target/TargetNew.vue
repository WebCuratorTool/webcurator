<script setup lang="ts">
import { type UseFetchApis, useFetch } from "@/utils/rest.api";
import {
  initNewTarget,
  useNextStateStore,
  useTargetDescriptionDTO,
  useTargetGeneralDTO,
  useTargetGropusDTO,
  useTargetProfileDTO,
} from "@/stores/target";
import TargetTabView from "./target-tabs/TargetTabView.vue";
import { ref } from "vue";
import { useProfiles } from "@/stores/profiles";
import { useRouter } from "vue-router";
import { useTargetListDataStore } from "@/stores/targetList";
const targetListData = useTargetListDataStore();

const router = useRouter();

const editing = ref(true);
const loading = ref(false);
const isTargetAvailable = ref(false);

const rest: UseFetchApis = useFetch();

const targetDescription = useTargetDescriptionDTO();
const targetGeneral = useTargetGeneralDTO();
const targetGroups = useTargetGropusDTO();
const targetProfile = useTargetProfileDTO();
const nextStates = useNextStateStore();

const fetchProfile = () => {
  loading.value = true;
  // const data = await rest.get('proflies/');
  rest
    .get("profiles/")
    .then((data: any) => {
      useProfiles().setProfiles(data);
    })
    .catch((err: any) => {
      console.log(err.message);
    })
    .finally(() => {
      loading.value = false;
    });
};

const save = () => {
  const dataReq: any = {
    description: targetDescription.getData(),
    general: targetGeneral.getData(),
    groups: targetGroups.getData(),
  };

  if (targetProfile.getData().id != null) {
    dataReq.profile = targetProfile.getData();
  }

  rest
    .post("targets", dataReq)
    .then((data: any) => {
      console.log(data);
      targetListData.search();
    })
    .catch((err: any) => {
      console.log(err.message);
    })
    .finally(() => {
      editing.value = false;
      router.push("/targets/");
    });
};

const setEditing = (isEditing: boolean) => {
  editing.value = isEditing;
  if (!isEditing) {
    router.push("/targets/");
  }
};

initNewTarget();
fetchProfile();
</script>

<template>
  <TargetTabView
    :editing="editing"
    :isTargetAvailable="isTargetAvailable"
    :loading="loading"
    @setEditing="setEditing"
    @save="save"
  />
</template>
