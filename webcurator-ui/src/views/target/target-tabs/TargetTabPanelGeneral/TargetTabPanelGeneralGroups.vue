<script setup lang="ts">
import { useTargetGropusDTO } from '@/stores/target';
import { useDialog } from 'primevue/usedialog';
import { defineAsyncComponent } from 'vue';

import WctTabViewPanel from '@/components/WctTabViewPanel.vue';

const targetGroups = useTargetGropusDTO();

const AddGroupsModal = defineAsyncComponent(() => import('./modals/TargetAddGroupsModal.vue'));

defineProps<{
  editing: boolean;
}>();

const addGroupsModal = useDialog();

const showAddGroups = () => {
  addGroupsModal.open(AddGroupsModal, {
    props: { header: 'Add Groups', modal: true, dismissableMask: true, style: { width: '50vw' } }
  });
};
</script>

<template>
  <div class="flex justify-between">
    <h4>Groups</h4>
    <Button v-if="editing" icon="pi pi-plus" label="Add" text @click="showAddGroups" />
  </div>
  <WctTabViewPanel>
    <div v-if="targetGroups.targetGroups.length > 0" class="flex flex-wrap gap-2">
      <Chip v-for="group in targetGroups.targetGroups" class="px-2" :key="group.id">
        <span class="p-2 m-0">{{ group.name }}</span>
        <Button v-if="editing" class="p-0 m-0" icon="pi pi-times-circle" style="width: 2rem" link @click="targetGroups.removeGroup(group.id)" />
      </Chip>
    </div>
    <div v-else class="text-center">
      <p class="text-500">This target does not belong to any groups</p>
    </div>
  </WctTabViewPanel>
</template>
