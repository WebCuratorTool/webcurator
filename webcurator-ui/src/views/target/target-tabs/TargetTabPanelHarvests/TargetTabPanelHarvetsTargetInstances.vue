<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";

import WctTabViewPanel from "@/components/WctTabViewPanel.vue";
import { useTargetInstanceListStore } from "@/stores/targetInstanceList";
import type { TargetInstance } from "@/types/targetInstance";
import { formatDatetime } from "@/utils/helper";
import { useProgressStore } from "@/utils/progress";

const progress = useProgressStore();
const { t } = useI18n();

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
        emptyMessage.value = t("target.harvestsPanel.noRecentTargetInstances");
      } else {
        emptyMessage.value = t(
          "target.harvestsPanel.noUpcomingTargetInstances",
        );
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
          :header="t('common.id')"
          dataType="numeric"
          style="min-width: 2rem"
        />
        <Column field="name" :header="t('common.name')" />
        <Column field="state" :header="t('common.state')">
          <template #body="{ data }">
            {{ targetInstanceStates[data.state] }}
          </template>
        </Column>
        <Column
          field="harvestDate"
          :header="t('target.harvestsPanel.harvestDate')"
        >
          <template #body="{ data }">
            {{ data.harvestDate ? formatDatetime(data.harvestDate) : "" }}
          </template>
        </Column>
        <Column field="owner" :header="t('common.owner')" />
      </DataTable>
      <div v-else class="text-center">
        <p class="text-500">{{ emptyMessage }}</p>
      </div>
    </WctTabViewPanel>
  </div>
</template>
