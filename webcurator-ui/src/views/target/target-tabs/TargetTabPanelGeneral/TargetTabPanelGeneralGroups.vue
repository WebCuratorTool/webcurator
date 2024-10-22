<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { useDialog } from 'primevue/usedialog';
import { useTargetGropusDTO } from '@/stores/target'

import WctTabViewPanel from '@/components/WctTabViewPanel.vue';

const targetGroups = useTargetGropusDTO();

const AddGroupsModal = defineAsyncComponent(() => import('./modals/TargetAddGroupsModal.vue'))

defineProps<{
    editing: boolean
}>()

const addGroupsModal = useDialog();

const showAddGroups = () => {
  const modalRef = addGroupsModal.open(AddGroupsModal, {
    props: { header: 'Add Groups', modal: true, dismissableMask: true, style: { width: '50vw' } }
  })
}
</script>

<template>

<div class="flex justify-content-between">
    <h4>Groups</h4>
    <Button v-if="editing" icon="pi pi-plus" label="Add" text @click="showAddGroups" />
  </div>
  <WctTabViewPanel>
    <div class="flex flex-wrap gap-2">
      <Chip class="px-2" v-for="group in targetGroups.targetGroups" :key="group.id">
          <span class="p-2 m-0">{{ group.name }}</span>
          <Button v-if="editing" class="p-0 m-0" icon="pi pi-times-circle" style="width: 2rem;" link @click="targetGroups.removeGroup(group.id)"/>
      </Chip>
    </div>
  </WctTabViewPanel>
</template>