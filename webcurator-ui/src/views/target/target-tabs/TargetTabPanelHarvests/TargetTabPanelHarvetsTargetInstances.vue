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

const props = defineProps<{
  header: string
  type: string
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
    limit: props.type == 'latest' ? 5 : 1024,
  }

  rest.post('target-instances', searchParams, { header: 'X-HTTP-Method-Override', value: 'GET' })
    .then((data: any) => {
      targetInstances.value = data.targetInstances;
    }).catch((err: any) => {
      console.log(err.message);
    }).finally(() => {
      loading.value = false;
    });
}

fetchTargetInstances();

</script>

<template>
  <h4 class="mt-4">{{ header}}</h4>
  <WctTabViewPanel>
    <DataTable class="w-full" :rowHover="true" :value=targetInstances :loading=loading>
      <Column field="id" header="Id" dataType="numeric" style="min-width: 2rem" />
      <Column field="name" header="Name" />
      <Column field="state" header="State" />
      <Column field="harvestDate" header="Harvest Date">
        <template #body="{ data }">
          {{ data.harvestDate ? formatDatetime(data.harvestDate) : '' }}
        </template>
      </Column>
      <Column field="owner" header="Owner" />
    </DataTable>
  </WctTabViewPanel>
</template>
