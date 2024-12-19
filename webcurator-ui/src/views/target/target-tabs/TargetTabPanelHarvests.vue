<script setup lang="ts">
import { formatDatetime } from '@/utils/helper'
import { useTargetHarvestsDTO } from '@/stores/target';

import WctFormField from '@/components/WctFormField.vue'
import WctTabViewPanel from '@/components/WctTabViewPanel.vue'

const targetSchedule = useTargetHarvestsDTO().targetSchedule;

defineProps<{
    editing: boolean
}>()
</script>

<template>
    <h4 class="mt-4">Description</h4>
    <WctTabViewPanel>
        <WctFormField v-if="editing" label="Harvest now" class="ml-2">
            <Checkbox
                v-model="targetSchedule.harvestNow" 
                :binary="true"
            />
        </WctFormField>
        <WctFormField v-if="editing" label="Allow harvest optimization" class="ml-2">
            <Checkbox
                v-model="targetSchedule.harvsestOptimization" 
                :binary="true"
            />
        </WctFormField>
        <DataTable class="w-full" :rowHover="true" :value="targetSchedule.schedules">
            <Column field="cron" header="Schedule" />
            <Column field="owner" header="Owner" />
            <Column field="nextExecutionDate" header="Next Scheduled Time" dataType="date">
                <template #body="{ data }">
                    {{ formatDatetime(data.nextExecutionDate) }}
                </template>
            </Column>
            <Column header="Action">
                <template #body="">
                    <Button class="p-button-text" style="width: 2rem;" icon="pi pi-eye" v-tooltip.bottom="'View Harvest'" text />
                    <Button v-if="editing" class="p-button-text" style="width: 2rem;" icon="pi pi-pencil" v-tooltip.bottom="'Edit Harvest'" text />
                    <Button v-if="editing" class="p-button-text" style="width: 2rem;" icon="pi pi-trash" v-tooltip.bottom="'Remove Harvest'" text />
                </template>
            </Column>
        </DataTable>
    </WctTabViewPanel>
</template>

<style>

</style>