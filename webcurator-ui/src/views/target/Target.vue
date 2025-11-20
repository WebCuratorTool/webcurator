<script setup lang="ts">
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { type UseFetchApis, useFetch } from "@/utils/rest.api";
import {
  setTarget,
  useNextStateStore,
  useTargetAccessDTO,
  useTargetAnnotationsDTO,
  useTargetDescriptionDTO,
  useTargetGeneralDTO,
  useTargetGropusDTO,
  useTargetHarvestsDTO,
  useTargetProfileDTO,
  useTargetSeedsDTO,
} from "@/stores/target";
import TargetTabView from "./target-tabs/TargetTabView.vue";
import { useAlertStore } from "@/utils/alertStore";
import { useProgressStore } from "@/utils/progress";

const router = useRouter();
const route = useRoute();
const rest: UseFetchApis = useFetch();
const alertStore = useAlertStore();
const progress = useProgressStore();
const targetId = route.params.id as string;

const targetAccess = useTargetAccessDTO();
const targetAnnotations = useTargetAnnotationsDTO();
const targetDescription = useTargetDescriptionDTO();
const targetGeneral = useTargetGeneralDTO();
const targetGroups = useTargetGropusDTO();
const targetHarvests = useTargetHarvestsDTO();
const targetProfile = useTargetProfileDTO();
const targetSeeds = useTargetSeedsDTO();
const nextStates = useNextStateStore();

const editing = ref(false);
const isTargetAvailable = ref(false);
const isPageAvailable = ref(true);

const initData = () => {
  isTargetAvailable.value = false;
  targetGeneral.initData();
  nextStates.initData();
};

const fetchTargetDetails = async () => {
  isTargetAvailable.value = false;
  progress.start();
  try {
    const data = await rest.get("targets/" + targetId);
    if (data) {
      isTargetAvailable.value = true;
      setTarget(data);
      nextStates.setData(
        targetGeneral.selectedState,
        data.general.nextStates || [],
      );
    } else {
      isPageAvailable.value = false;
      router.push("/targets/");
    }
  } finally {
    progress.end();
  }
};

const save = async () => {
  progress.start();
  try {
    const dataReq = {
      access: targetAccess.getData(),
      annotations: targetAnnotations.getData(),
      general: targetGeneral.getData(),
      profile: targetProfile.getData(),
      description: targetDescription.getData(),
      groups: targetGroups.getData(),
      seeds: targetSeeds.getData(),
      schedule: targetHarvests.getData(),
    };

    dataReq.profile.overrides.forEach((override) => {
      // Ensure blockedUrls and includedUrls are arrays
      if (override.id == "blockedUrls" || override.id == "includedUrls") {
        if (!Array.isArray(override.value)) {
          override.value = override.value.toString().split("\n");
        }
      }
    });

    const response = await rest.put("targets/" + targetGeneral.id, dataReq);
    if (response == 200) {
      showSuccessMessage();
      editing.value = false;
    }
  } catch (err: any) {
    showErrorMessage(err.message);
  } finally {
    progress.end();
  }
};

const setEditing = (isEditing: boolean) => {
  editing.value = isEditing;
  if (!isEditing) {
    fetchTargetDetails();
  }
};

const showErrorMessage = (message: string) => {
  alertStore.error(message, message, "Target not saved");
};

const showSuccessMessage = () => {
  alertStore.info("Target succesfully saved");
};

await fetchTargetDetails();
</script>

<template>
  <TargetTabView
    v-if="isPageAvailable"
    :editing="editing"
    :isTargetAvailable="isTargetAvailable"
    @setEditing="setEditing"
    @save="save"
  />
</template>
