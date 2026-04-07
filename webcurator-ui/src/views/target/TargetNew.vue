<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import z from "zod";

import { useProfiles } from "@/stores/profiles";
import {
  initNewTarget,
  useTargetDescriptionDTO,
  useTargetGeneralDTO,
  useTargetGropusDTO,
  useTargetProfileDTO,
} from "@/stores/target";
import type { NewTarget } from "@/types/target";
import { useAlertStore } from "@/utils/alertStore";
import { useProgressStore } from "@/utils/progress";
import { useFetch, type UseFetchApis } from "@/utils/rest.api";

import TargetTabView from "./target-tabs/TargetTabView.vue";

const router = useRouter();
const progress = useProgressStore();
const alertStore = useAlertStore();

const editing = ref(true);
const loading = ref(false);
const isTargetAvailable = ref(false);
const validationErrors = ref();

const rest: UseFetchApis = useFetch();

const targetDescription = useTargetDescriptionDTO();
const targetGeneral = useTargetGeneralDTO();
const targetGroups = useTargetGropusDTO();
const targetProfile = useTargetProfileDTO();

const targetName = z.string().min(1, "Name is required");

const save = async () => {
  const validationResult = targetName.safeParse(targetGeneral.name);
  if (!validationResult.success) {
    validationErrors.value = z.flattenError(
      validationResult.error,
    ).formErrors[0];
  } else {
    progress.start();
    try {
      const dataReq: NewTarget = {
        description: targetDescription.getData(),
        general: targetGeneral.getData(),
        groups: targetGroups.getData(),
      };

      if (targetProfile.getData().id != null) {
        dataReq.profile = targetProfile.getData();
      }

      const response = await rest.post("targets/", dataReq);
      if (response == 200) {
        showSuccessMessage();
        editing.value = false;
      }
    } catch (err: unknown) {
      const msg = err as Error;
      showErrorMessage(msg.message);
    } finally {
      progress.end();
      router.push("/targets/");
    }
  }
};

const setEditing = (isEditing: boolean) => {
  editing.value = isEditing;
  if (!isEditing) {
    router.push("/targets/");
  }
};

const showErrorMessage = (message: string) => {
  alertStore.error(message, message, "Target not saved");
};

const showSuccessMessage = () => {
  alertStore.info("Target succesfully saved");
};
import { watch } from "vue";

watch(
  () => targetGeneral.getData().name,
  (newName) => {
    const validationResult = targetName.safeParse(newName);
    if (!validationResult.success) {
      validationErrors.value = z.flattenError(
        validationResult.error,
      ).formErrors[0];
    } else {
      validationErrors.value = undefined;
    }
  },
);
initNewTarget();
useProfiles().fetchProfiles();
</script>

<template>
  <TargetTabView
    :editing="editing"
    :isTargetAvailable="isTargetAvailable"
    :loading="useProfiles().loadingProfiles || loading"
    :validationErrors="validationErrors"
    @setEditing="setEditing"
    @save="save"
  />
</template>
