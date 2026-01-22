<script setup lang="ts">
import { ref, toRaw } from "vue";
import { useRoute, useRouter } from "vue-router";

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
import type { Target } from "@/types/target";
import { useAlertStore } from "@/utils/alertStore";
import { useProgressStore } from "@/utils/progress";
import { useFetch, type UseFetchApis } from "@/utils/rest.api";

import TargetTabView from "./target-tabs/TargetTabView.vue";

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

let originalTarget: Target | null = null;

const fetchTargetDetails = async () => {
  isTargetAvailable.value = false;
  progress.start();
  try {
    const data: Target = await rest.get("targets/" + targetId);
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
  } catch (err: unknown) {
    const msg = err as Error;
    showErrorMessage(msg.message);
  } finally {
    progress.end();
  }
};

const setEditing = (isEditing: boolean) => {
  if (isEditing) {
    originalTarget = {
      access: JSON.parse(JSON.stringify(toRaw(targetAccess.getData()))),
      annotations: JSON.parse(
        JSON.stringify(toRaw(targetAnnotations.getData())),
      ),
      description: JSON.parse(
        JSON.stringify(toRaw(targetDescription.getData())),
      ),
      general: JSON.parse(JSON.stringify(toRaw(targetGeneral.getData()))),
      groups: JSON.parse(JSON.stringify(toRaw(targetGroups.getData()))),
      schedule: JSON.parse(JSON.stringify(toRaw(targetHarvests.getData()))),
      profile: JSON.parse(JSON.stringify(toRaw(targetProfile.getData()))),
      seeds: JSON.parse(JSON.stringify(toRaw(targetSeeds.getData()))),
    };
  } else {
    if (originalTarget) {
      targetAccess.setData(originalTarget.access);
      targetAnnotations.setData(originalTarget.annotations);
      targetDescription.setData(originalTarget.description);
      targetGeneral.setData(originalTarget.general);
      targetGroups.setData(originalTarget.groups);
      targetHarvests.setData(originalTarget.schedule);
      targetProfile.setData(originalTarget.profile);
      targetSeeds.setData(originalTarget.seeds);
    }
  }
  editing.value = isEditing;
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
