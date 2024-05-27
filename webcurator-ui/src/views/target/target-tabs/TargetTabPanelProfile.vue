<script setup lang="ts">
import { ref } from "vue";
import { useTargetProfileDTO } from '@/stores/target';

import WctFormField from '@/components/WctFormField.vue'
import WctTabViewPanel from '@/components/WctTabViewPanel.vue'

defineProps<{
    editing: boolean
}>()

const targetProfile = useTargetProfileDTO();

const timeUnits = ref([
    "SECOND",
    "MINUTE",
    "HOUR",
    "DAY",
    "WEEK"
])

const sizeUnits = ref([
    "B",
    "KB",
    "MB",
    "GB",
])

function camelCaseToTitleCase(s: string) {
    const result = s.replace(/([A-Z])/g, ' $1');
    return result.charAt(0).toUpperCase() + result.slice(1);
}

</script>

<template>
    <WctTabViewPanel label="Base Profile">
        <WctFormField label="Harvester Type">
          <InputText v-model="targetProfile.harvesterType" :disabled="!editing" />
        </WctFormField>
        <WctFormField label="Base Profile">
          <InputText v-model="targetProfile.name" :disabled="!editing" />
        </WctFormField>
    </WctTabViewPanel>

    <WctTabViewPanel>
        <DataTable class="w-full" :rowHover="true" :value="targetProfile.overrides">
            <template #header>
                <span class="text-xl text-900 font-bold">Profile Overrides</span>
            </template>
            <Column field="id" header="Profile Element">
                <template #body="{ data }">
                    {{ camelCaseToTitleCase(data.id) }}
                </template>
            </Column>
            <Column field="value" header="Override Value">
                <template #body="{ data }">
                    <Checkbox v-if="typeof data.value == 'boolean'"
                        v-model="data.value" 
                        :binary="true"
                        :disabled="!editing" 
                    />
                    <InputText v-else-if="Array.isArray(data.value) || typeof data.value == 'string'" 
                        v-model="data.value"
                        :disabled="!editing" 
                    />
                    <div v-else class="flex">
                        <InputNumber
                            class="w-6"
                            v-model="data.value" 
                            inputId="minmax-buttons" 
                            mode="decimal"
                            :minFractionDigits="(data.id == 'dataLimit' || data.id == 'timeLimit') ? 1 : 0"
                            showButtons 
                            :min="0" 
                            :max="100" 
                            :disabled="!editing" 
                        />
                        <Dropdown v-if="data.id == 'dataLimit'"
                            class="ml-2"
                            v-model="data.unit"
                            :options="sizeUnits"
                            :disabled="!editing"
                        />
                        <Dropdown v-else-if="data.id == 'timeLimit'"
                            class="ml-2"
                            v-model="data.unit"
                            :options="timeUnits"
                            :disabled="!editing"
                        />
                    </div>
                </template>
            </Column>
            <Column field="enabled" header="Enable Override">
                <template #body="{ data }">
                    <Checkbox
                        id="checkOption1"
                        name="option1"
                        value="Run on Approval"
                        v-model="data.enabled" 
                        :binary="true"
                        :disabled="!editing" 
                    />
                </template>
            </Column>
        </DataTable>
    </WctTabViewPanel>
</template>

<style>

</style>