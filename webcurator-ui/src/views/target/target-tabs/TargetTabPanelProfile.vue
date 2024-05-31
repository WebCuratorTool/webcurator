<script setup lang="ts">
import { ref } from "vue";
import { useTargetProfileDTO } from '@/stores/target';

import WctFormField from '@/components/WctFormField.vue'
import WctTabViewPanel from '@/components/WctTabViewPanel.vue'

defineProps<{
    editing: boolean
}>()

const targetProfile = useTargetProfileDTO().getData();
// const targetProfile = data.targetProfile;

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
    <h4 class="mt-4">Base Profile</h4>
    <WctTabViewPanel>
        <WctFormField label="Harvester Type">
          <InputText v-if="editing" v-model="targetProfile.harvesterType" :disabled="!editing" />
          <p v-else class="font-semibold">{{ targetProfile.harvesterType}}</p>
        </WctFormField>
        <WctFormField label="Base Profile">
          <InputText v-if="editing" v-model="targetProfile.name" :disabled="!editing" />
          <p v-else class="font-semibold">{{ targetProfile.name}}</p>
        </WctFormField>
    </WctTabViewPanel>

    <h4 class="mt-4">Profile Overrides</h4>
    <WctTabViewPanel>
        <DataTable class="w-full" :rowHover="true" :value="targetProfile.overrides">
            <Column field="id" header="Profile Element">
                <template #body="{ data }">
                    {{ camelCaseToTitleCase(data.id) }}
                </template>
            </Column>
            <Column field="value" header="Override Value">
                <template #body="{ data }">
                    <div v-if="typeof data.value == 'boolean'">
                        <Checkbox v-if="editing"
                            v-model="data.value" 
                            :binary="true"
                            :disabled="!editing" 
                        />
                        <p v-else class="font-semibold">{{ data.value ? 'Yes' : 'No' }}</p>
                    </div>
                    <div v-else-if="Array.isArray(data.value) || typeof data.value == 'string'">
                        <InputText v-if="editing" 
                            v-model="data.value"
                            :disabled="!editing" 
                        />
                        <p v-else class="font-semibold">{{ data.value.toString() }}</p>
                    </div>
                    <div v-else>
                        <div v-if="editing" class="flex">
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
                        <div v-else>
                            <p class="font-semibold">{{ `${ data.value ? data.value : ''} ${ data.unit ? data.unit : ''}`}}</p>
                        </div>
                    </div>
                </template>
            </Column>
            <Column field="enabled" header="Enable Override">
                <template #body="{ data }">
                    <Checkbox v-if="editing"
                        id="checkOption1"
                        name="option1"
                        value="Run on Approval"
                        v-model="data.enabled" 
                        :binary="true"
                        :disabled="!editing" 
                    />
                    <p v-else class="font-semibold">{{ data.value ? 'Yes' : 'No' }}</p>
                </template>
            </Column>
        </DataTable>
    </WctTabViewPanel>
</template>

<style>

</style>