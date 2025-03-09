<script setup lang="ts">
import { useRouter } from 'vue-router'
import { formatDatetime } from '@/utils/helper';
import { formatTargetState, useTargetGeneralDTO } from '@/stores/target';

import Loading from '@/components/Loading.vue'

import TargetTabPanelAccess from './TargetTabPanelAccess.vue';
import TargetTabPanelAnnotations from './TargetTabPanelAnnotations.vue';
import TargetTabPanelDescription from './TargetTabPanelDescription.vue';
import TargetTabPanelHarvests from './TargetTabPanelHarvests/TargetTabPanelHarvests.vue';
import TargetTabPanelGeneral from './TargetTabPanelGeneral/TargetTabPanelGeneral.vue'
import TargetTabPanelProfile from './TargetTabPanelProfile.vue'

const router = useRouter()

defineProps<{
    editing: boolean
    isTargetAvailable: boolean,
    loading: boolean
}>()

const targetGeneral = useTargetGeneralDTO()

const emit = defineEmits(['setEditing', 'save'])

const navigateBack = () => {
  if (router) {
    router.push('/wct/targets/')
  }
}

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
          <Button icon="pi pi-times" @click="$emit('setEditing', false)"  label="Cancel" />
          <Button class="ml-2" icon="pi pi-save" @click="$emit('save')" label="Save" />
        </template>
      </Toolbar>

      <div class="w-full">
        <span class="title">{{ targetGeneral.name }}</span>
        <div v-if="isTargetAvailable" class="subtitle-container p-overlay-badge">
          <span class="sub-title">{{ formatDatetime(targetGeneral.creationDate) }}</span>
          <span class="p-badge p-component p-badge-secondary" data-pc-name="badge" data-pc-section="root">{{
            formatTargetState(targetGeneral.selectedState) }}</span>
        </div>
      </div>
    </div>
  </div>
  <div class="main-content">
    <Loading v-if="loading" />
    <TabView v-else class="tabview-custom">
      <TabPanel header="Genaral">
        <TargetTabPanelGeneral :editing="editing" />
      </TabPanel>
      <TabPanel header="Description">
        <TargetTabPanelDescription :editing="editing" />
      </TabPanel>
      <TabPanel header="Profile">
        <TargetTabPanelProfile :editing="editing" />
      </TabPanel>
      <TabPanel header="Harvests">
        <TargetTabPanelHarvests :editing="editing" />
      </TabPanel>
      <TabPanel header="Annotations">
        <TargetTabPanelAnnotations :editing="editing" />
      </TabPanel>
      <TabPanel header="Access">
        <TargetTabPanelAccess :editing="editing" />
      </TabPanel>
    </TabView>
  </div>
</template>

<style>
.tabview-custom {
  width: 80vw;
  min-height: 60vh;
}
</style>
