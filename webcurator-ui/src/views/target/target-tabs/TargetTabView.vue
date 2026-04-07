<script setup lang="ts">
import { formatTargetState, useTargetGeneralDTO } from "@/stores/target";
import { formatDate } from "@/utils/helper";

import TargetTabPanelAnnotations from "./TargetTabAnnotations/TargetTabPanelAnnotations.vue";
import TargetTabPanelAccess from "./TargetTabPanelAccess.vue";
import TargetTabPanelDescription from "./TargetTabPanelDescription.vue";
import TargetTabPanelGeneral from "./TargetTabPanelGeneral/TargetTabPanelGeneral.vue";
import TargetTabPanelHarvests from "./TargetTabPanelHarvests/TargetTabPanelHarvests.vue";
import TargetTabPanelProfile from "./TargetTabPanelProfile.vue";

defineProps<{
  editing: boolean;
  isTargetAvailable: boolean;
  validationErrors: string;
}>();

const targetGeneral = useTargetGeneralDTO();
</script>

<template>
  <div class="2xl:w-5/6 flex flex-col">
    <div class="flex items-center justify-between w-full px-5 pt-8">
      <router-link to="/targets/">
        <span class="pi pi-arrow-left wct-back-button"></span>
      </router-link>
      <Button
        v-if="!editing"
        class="wct-primary-button"
        icon="pi pi-pencil"
        @click="$emit('setEditing', true)"
        label="Edit"
      />
      <div v-else class="flex gap-2">
        <Button
          class="wct-primary-button"
          icon="pi pi-times"
          @click="$emit('setEditing', false)"
          label="Cancel"
        />
        <Button
          class="wct-primary-button ml-2"
          icon="pi pi-save"
          @click="$emit('save')"
          label="Save"
        />
      </div>
    </div>

    <div
      class="flex items-center justify-start w-7/8"
      style="padding: var(--p-tabs-tabpanel-padding)"
    >
      <span class="title">{{ targetGeneral.name }}</span>
      <span v-if="isTargetAvailable" class="sub-title">{{
        formatDate(targetGeneral.creationDate)
      }}</span>
      <Badge
        v-if="isTargetAvailable"
        :value="formatTargetState(targetGeneral.selectedState)"
      />
    </div>
  </div>

  <div class="2xl:w-5/6">
    <Tabs value="0" class="tabview-custom w-full">
      <TabList>
        <Tab value="0">General</Tab>
        <Tab value="1" :disabled="editing && targetGeneral.name === ''"
          >Description</Tab
        >
        <Tab value="2" :disabled="editing && targetGeneral.name === ''"
          >Profile</Tab
        >
        <Tab value="3" :disabled="editing && targetGeneral.name === ''"
          >Harvests</Tab
        >
        <Tab value="4" :disabled="editing && targetGeneral.name === ''"
          >Annotations</Tab
        >
        <Tab value="5" :disabled="editing && targetGeneral.name === ''"
          >Access</Tab
        >
      </TabList>
      <TabPanels>
        <TabPanel value="0">
          <TargetTabPanelGeneral
            :editing="editing"
            :validationErrors="validationErrors"
          />
        </TabPanel>
        <TabPanel value="1">
          <TargetTabPanelDescription :editing="editing" />
        </TabPanel>
        <TabPanel value="2">
          <TargetTabPanelProfile :editing="editing" />
        </TabPanel>
        <TabPanel value="3">
          <TargetTabPanelHarvests :editing="editing" />
        </TabPanel>
        <TabPanel value="4">
          <TargetTabPanelAnnotations :editing="editing" />
        </TabPanel>
        <TabPanel value="5">
          <TargetTabPanelAccess :editing="editing" />
        </TabPanel>
      </TabPanels>
    </Tabs>
  </div>
</template>

<style scoped>
:deep(.p-badge) {
  outline-style: none !important;
}
</style>
