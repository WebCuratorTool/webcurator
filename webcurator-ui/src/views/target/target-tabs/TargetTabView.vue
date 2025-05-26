<script setup lang="ts">
import { formatTargetState, useTargetGeneralDTO } from '@/stores/target';
import { formatDatetime } from '@/utils/helper';
import { useRouter } from 'vue-router';

import Loading from '@/components/Loading.vue';

import TargetTabPanelAccess from './TargetTabPanelAccess.vue';
import TargetTabPanelAnnotations from './TargetTabPanelAnnotations.vue';
import TargetTabPanelDescription from './TargetTabPanelDescription.vue';
import TargetTabPanelGeneral from './TargetTabPanelGeneral/TargetTabPanelGeneral.vue';
import TargetTabPanelHarvests from './TargetTabPanelHarvests/TargetTabPanelHarvests.vue';
import TargetTabPanelProfile from './TargetTabPanelProfile.vue';

const router = useRouter();

defineProps<{
  editing: boolean;
  isTargetAvailable: boolean;
  loading: boolean;
}>();

const targetGeneral = useTargetGeneralDTO();

const emit = defineEmits(['setEditing', 'save']);

const navigateBack = () => {
  if (router) {
    router.push('/wct/targets/');
  }
};
</script>

<template>
  <div class="main-header">
    <div class="target-header-container">
      <Toolbar style="border: none; background: transparent">
        <template #start>
          <Button icon="pi pi-arrow-left" @click="navigateBack" text />
        </template>
        <template v-if="!editing" #end>
          <Button icon="pi pi-pencil" @click="$emit('setEditing', true)" label="Edit" />
        </template>
        <template v-else #end>
          <Button icon="pi pi-times" @click="$emit('setEditing', false)" label="Cancel" />
          <Button class="ml-2" icon="pi pi-save" @click="$emit('save')" label="Save" />
        </template>
      </Toolbar>

      <div class="flex items-center justify-start w-full">
        <span class="title">{{ targetGeneral.name }}</span>
        <OverlayBadge v-if="isTargetAvailable" :value="formatTargetState(targetGeneral.selectedState)">
          <span class="sub-title">{{ formatDatetime(targetGeneral.creationDate) }}</span>
        </OverlayBadge>
      </div>
    </div>
  </div>
  <div class="main-content">
    <Loading v-if="loading" />
    <Tabs v-else value="0">
      <TabList>
        <Tab value="0">Genaral</Tab>
        <Tab value="1">Description</Tab>
        <Tab value="2">Profile</Tab>
        <Tab value="3">Harvests</Tab>
        <Tab value="4">Annotations</Tab>
        <Tab value="5">Access</Tab>
      </TabList>
      <TabPanels class="tabview-custom">
        <TabPanel value="0">
          <TargetTabPanelGeneral :editing="editing" />
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

<style>
.tabview-custom {
  width: 80vw;
  min-height: 60vh;
}
</style>
