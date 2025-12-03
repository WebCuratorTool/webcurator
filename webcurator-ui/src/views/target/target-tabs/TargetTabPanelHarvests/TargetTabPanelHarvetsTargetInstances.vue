<script setup lang="ts">
import { onMounted, ref } from "vue";

import WctTabViewPanel from "@/components/WctTabViewPanel.vue";
import { useTargetInstanceListStore } from "@/stores/targetInstanceList";
import type { TargetInstance } from "@/types/targetInstance";
import { formatDatetime } from "@/utils/helper";
import { useProgressStore } from "@/utils/progress";

const progress = useProgressStore();

const targetInstances = ref(<Array<TargetInstance>>[]);
const emptyMessage = ref("");

const props = defineProps<{
  header: string;
  type: string;
  targetInstanceStates: { [key: string]: string };
  targetId: string;
}>();

const fetchTargetInstances = async () => {
  progress.start();
  try {
    const now = new Date();
    const searchParams = {
      filter: {
        targetId: props.targetId,
        to: props.type == "latest" ? now : null,
        from: props.type == "upcoming" ? now : null,
      },
      limit: props.type == "latest" ? 5 : 15,
    };

    targetInstances.value =
      await useTargetInstanceListStore().search(searchParams);
  } catch (err: unknown) {
    const msg = err as Error;
    console.log(msg.message);
  } finally {
    progress.end();
    if (targetInstances.value && targetInstances.value.length == 0) {
      if (props.type == "latest") {
        emptyMessage.value = "No recent target instances";
      } else {
        emptyMessage.value = "No upcoming target instances";
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
      <DataTable
        v-if="targetInstances && targetInstanceStates && targetInstances.length"
        class="w-full"
        :rowHover="true"
        :value="targetInstances"
        :loading="progress.visible"
      >
        <Column
          field="id"
          header="Id"
          dataType="numeric"
          style="min-width: 2rem"
        />
        <Column field="name" header="Name" />
        <Column field="state" header="State">
          <template #body="{ data }">
            {{ targetInstanceStates[data.state] }}
          </template>
        </Column>
        <Column field="harvestDate" header="Harvest Date">
          <template #body="{ data }">
            {{ data.harvestDate ? formatDatetime(data.harvestDate) : "" }}
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
