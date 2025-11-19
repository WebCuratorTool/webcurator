<script setup lang="ts">
// libraries
import { onMounted, ref } from 'vue';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { useRoute } from 'vue-router';

// components
import WctTabViewPanel from '@/components/WctTabViewPanel.vue';
// stores
import { useTargetInstanceListStore } from '@/stores/targetInstanceList';
// types
import type { TargetInstance } from '@/types/targetInstance';
// utils
import { formatDatetime } from '@/utils/helper';
import { useProgressStore } from '@/utils/progress';

const rest: UseFetchApis = useFetch();
const progress = useProgressStore();

const route = useRoute();
const targetId = route.params.id as string;

const targetInstances = ref(<Array<TargetInstance>>([]));
const emptyMessage = ref('');

const props = defineProps<{
  header: string;
  type: string;
  targetInstanceStates: { [key: string]: string };
  targetId: string
}>();

const fetchTargetInstances = async () => {
  progress.start();
  try {
    const now = new Date();
    const searchParams = {
      filter: {
        targetId: targetId,
        to: props.type == 'latest' ? now : null,
        from: props.type == 'upcoming' ? now : null
      },
      limit: props.type == 'latest' ? 5 : 15
    };

    targetInstances.value = await useTargetInstanceListStore().search(searchParams);
    
  } catch (err: any) {
    console.log(err.message);
  } finally {
    progress.end();
    if (targetInstances.value && targetInstances.value.length == 0) {
      if (props.type == 'latest') {
        emptyMessage.value = 'No recent target instances';
      } else {
        emptyMessage.value = 'No upcoming target instances';
      }
    }
  }
};
onMounted(() => {
  fetchTargetInstances();
});
</script>

<template>
  <div class="mt-4">
    <h4>{{ header }}</h4>
    <WctTabViewPanel>
      <DataTable v-if="targetInstances && targetInstanceStates && targetInstances.length" class="w-full" :rowHover="true" :value="targetInstances" :loading="progress.visible">
        <Column field="id" header="Id" dataType="numeric" style="min-width: 2rem" />
        <Column field="name" header="Name" />
        <Column field="state" header="State">
          <template #body="{ data }">
            {{ targetInstanceStates[data.state] }}
          </template>
        </Column>
        <Column field="harvestDate" header="Harvest Date">
          <template #body="{ data }">
            {{ data.harvestDate ? formatDatetime(data.harvestDate) : '' }}
          </template>
        </Column>
        <Column field="owner" header="Owner" />
      </DataTable>
      <div v-else class="text-center">
        <p class="text-500">{{ emptyMessage }}</p>
      </div>
    </WctTabViewPanel>
  </div>
</template>
