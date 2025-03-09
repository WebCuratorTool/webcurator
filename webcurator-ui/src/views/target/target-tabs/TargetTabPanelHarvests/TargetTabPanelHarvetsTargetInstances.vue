<script setup lang="ts">
import { ref } from 'vue';
import { formatDatetime } from '@/utils/helper';
import { useRoute } from 'vue-router';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';

import WctTabViewPanel from '@/components/WctTabViewPanel.vue'

const rest: UseFetchApis = useFetch();

const route = useRoute()
const targetId = route.params.id as string

const targetInstances = ref();
const loading = ref(true);
const emptyMessage = ref('');

const props = defineProps<{
  header: string
  type: string
  targetInstanceStates: {[key: string]: string}
}>()

const fetchTargetInstances = () => {
  loading.value = true;

  const now = new Date();
  const searchParams = {
    filter: {        
      targetId: targetId,
      to: props.type == 'latest' ? now : null,
      from: props.type == 'upcoming' ? now : null
    },
    limit: props.type == 'latest' ? 5 : 15,
  }

  rest.post('target-instances', searchParams, { header: 'X-HTTP-Method-Override', value: 'GET' })
    .then((data: any) => {
      targetInstances.value = data.targetInstances;
    }).catch((err: any) => {
      console.log(err.message);
    }).finally(() => {
      if (targetInstances.value && targetInstances.value.length == 0) {
        if (props.type == 'latest') {
          emptyMessage.value = 'No recent target instances';
        } else {
          emptyMessage.value = 'No upcoming target instances';
        }
      }
      loading.value = false;
    });
}

fetchTargetInstances();

</script>

<template>
  <h4 class="mt-4">{{ header }}</h4>
  <WctTabViewPanel>
    <DataTable v-if="targetInstances && targetInstanceStates && targetInstances.length" class="w-full" :rowHover="true" :value=targetInstances :loading=loading>
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
</template>
