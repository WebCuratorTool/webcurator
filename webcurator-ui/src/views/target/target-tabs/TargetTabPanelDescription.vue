<script setup lang="ts">
import { ref } from "vue";
import { useTargetDescriptionDTO } from '@/stores/target';

import WctFormField from '@/components/WctFormField.vue'
import WctTabViewPanel from '@/components/WctTabViewPanel.vue'

defineProps<{
    editing: boolean
}>()

const targetDescription = useTargetDescriptionDTO().targetDescription;

const types = ref([
    "",
    "Collection",
    "Dataset",
    "Event",
    "Image",
    "Interactive Resource",
    "Moving Image",
    "Physical Object",
    "Service",
    "Software",
    "Sound",
    "Still Image",
    "Text",
    "Serial",
    "eSerial"
])

</script>

<template>
    <WctTabViewPanel label="Description">
        <WctFormField label="Identifier">
          <InputText v-model="targetDescription.identifier" :disabled="!editing" />
        </WctFormField>
        <WctFormField label="Description">
          <Textarea v-model="targetDescription.description" :disabled="!editing" />
        </WctFormField>
        <WctFormField label="Subject">
          <Textarea v-model="targetDescription.subject" :disabled="!editing" />
        </WctFormField>
        <WctFormField label="Creator">
          <InputText v-model="targetDescription.creator" :disabled="!editing" />
        </WctFormField>
        <WctFormField label="Contributor">
          <InputText v-model="targetDescription.contributor" :disabled="!editing" />
        </WctFormField>
        <WctFormField label="Type">
          <Dropdown
            v-model="targetDescription.type"
            :options="types"
            :disabled="!editing"
          />
        </WctFormField>
        <WctFormField label="Format">
          <InputText v-model="targetDescription.format" :disabled="!editing" />
        </WctFormField>
        <WctFormField label="Source">
          <InputText v-model="targetDescription.source" :disabled="!editing" />
        </WctFormField>
        <WctFormField label="Language">
          <InputText v-model="targetDescription.language" :disabled="!editing" />
        </WctFormField>
        <WctFormField label="Relation">
          <InputText v-model="targetDescription.relation" :disabled="!editing" />
        </WctFormField>
        <WctFormField label="Coverage">
          <InputText v-model="targetDescription.coverage" :disabled="!editing" />
        </WctFormField>
        <WctFormField label="ISSN">
          <InputText v-model="targetDescription.issn" :disabled="!editing" />
        </WctFormField>
        <WctFormField label="ISBN">
          <InputText v-model="targetDescription.isbn" :disabled="!editing" />
        </WctFormField>
    </WctTabViewPanel>
</template>

<style>

</style>