<script setup lang="ts">
import { ref } from 'vue';
import { useTargetAccessDTO } from '@/stores/target';

import WctFormField from '@/components/WctFormField.vue'
import WctTabViewPanel from '@/components/WctTabViewPanel.vue'


const data = useTargetAccessDTO();
const targetAccess = data.targetAccess;

defineProps<{
    editing: boolean
}>()

const accessZones = ref([
    "Public",
    "Onsite",
    "Restricted",
])

</script>

<template>
    <WctTabViewPanel columns >
        <div class="col">
            <WctFormField label="Display Target">
                <Checkbox v-if="editing" v-model="targetAccess.displayTarget" :binary="true"
                    :disabled="!editing" />
                <p v-else class="font-semibold">{{ targetAccess.displayTarget ? 'Yes' : 'No' }}</p>
            </WctFormField>
            <WctFormField label="Access Zone">
                <Dropdown v-if="editing"
                    v-model="targetAccess.accessZoneText"
                    :options="accessZones"
                />
                <p v-else class="font-semibold">{{ targetAccess.accessZoneText }}</p>
            </WctFormField>
            <WctFormField label="Target Introductory Display Note">
                <Textarea v-if="editing" 
                    v-model="targetAccess.displayNote"
                    :disabled="!editing" />
                <p v-else class="font-semibold">{{ targetAccess.displayNote }}</p>  
            </WctFormField>
        </div>
        <div class="col">
            <WctFormField label="Reason for Display Change">
                <Textarea v-if="editing" 
                    v-model="targetAccess.displayChangeReason"
                    :disabled="!editing" />
                <p v-else class="font-semibold">{{ targetAccess.displayChangeReason }}</p>  
            </WctFormField>
        </div>
    </WctTabViewPanel>
</template>

<style>

</style>