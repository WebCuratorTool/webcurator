<script setup lang="ts">
import Loading from '@/components/Loading.vue';
import { formatTargetState, useTargetGeneralDTO } from '@/stores/target';
import { formatDate } from '@/utils/helper';
import { useRouter } from 'vue-router';
import TargetTabPanelAccess from './TargetTabPanelAccess.vue';
import TargetTabPanelAnnotations from './TargetTabAnnotations/TargetTabPanelAnnotations.vue';
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
  <div class="2xl:w-5/6 flex flex-col "> 
    <!-- <Toolbar>
      <template #start>
        <Button icon="pi pi-arrow-left" @click="navigateBack" text />
      </template>
      <template v-if="!editing" #end>
        <Button class="wct-primary-button" icon="pi pi-pencil" @click="$emit('setEditing', true)" label="Edit" />
      </template>
      <template v-else #end>
        <div class="flex gap-2">
          <Button class="wct-primary-button" icon="pi pi-times" @click="$emit('setEditing', false)" label="Cancel" />
          <Button class="wct-primary-button ml-2" icon="pi pi-save" @click="$emit('save')" label="Save" />
        </div>
      </template>
    </Toolbar> -->

    <div class="flex items-center justify-between w-full px-5 pt-8">
      <!-- <Button icon="pi pi-arrow-left" @click="navigateBack" text /> -->
      <router-link to="/wct/targets/">
        <span class="pi pi-arrow-left wct-back-button"></span>
      </router-link>
      <Button v-if="!editing" class="wct-primary-button" icon="pi pi-pencil" @click="$emit('setEditing', true)" label="Edit" />
      <div v-else class="flex gap-2">
          <Button class="wct-primary-button" icon="pi pi-times" @click="$emit('setEditing', false)" label="Cancel" />
          <Button class="wct-primary-button ml-2" icon="pi pi-save" @click="$emit('save')" label="Save" />
        </div>
    </div>

    <div class="flex items-center justify-start w-7/8"  style="padding: var(--p-tabs-tabpanel-padding);">
      <span class="title">{{ targetGeneral.name }}</span>
      <span v-if="isTargetAvailable" class="sub-title">{{ formatDate(targetGeneral.creationDate) }}</span>
      <Badge v-if="isTargetAvailable" :value="formatTargetState(targetGeneral.selectedState)" />
    </div>
  </div>

  <div class="2xl:w-5/6">
    <Loading v-if="loading" />
    <Tabs v-else value="0" class="tabview-custom w-full">
      <TabList>
        <Tab value="0">General</Tab>
        <Tab value="1">Description</Tab>
        <Tab value="2">Profile</Tab>
        <Tab value="3">Harvests</Tab>
        <Tab value="4">Annotations</Tab>
        <Tab value="5">Access</Tab>
      </TabList>
      <TabPanels>
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

<style scoped>
:deep(.p-badge) {
  outline-style: none !important;
}
</style>

