<script setup lang="ts">
import { ref } from "vue";
import { useTargetDescriptionDTO } from '@/stores/target';

import WctFormField from '@/components/WctFormField.vue'
import WctTabViewPanel from '@/components/WctTabViewPanel.vue'

const props = defineProps<{
    editing: boolean
}>()

const targetDescription = ref(useTargetDescriptionDTO().targetDescription);

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
  <h4 class="mt-4">Description</h4>
  <WctTabViewPanel>
    <WctFormField label="Identifier">
      <InputText v-if="editing" v-model="targetDescription.identifier" :disabled="!editing" />
      <p v-else class="font-semibold">{{ targetDescription.identifier}}</p>
    </WctFormField>
    <WctFormField label="Description">
      <Textarea v-if="editing" v-model="targetDescription.description" :disabled="!editing" />
      <p v-else class="font-semibold">{{ targetDescription.description }}</p>
    </WctFormField>
    <WctFormField label="Subject">
      <Textarea v-if="editing" v-model="targetDescription.subject" :disabled="!editing" />
      <p v-else class="font-semibold">{{ targetDescription.subject }}</p>
    </WctFormField>
    <WctFormField label="Creator">
      <InputText v-if="editing" v-model="targetDescription.creator" :disabled="!editing" />
      <p v-else class="font-semibold">{{ targetDescription.creator}}</p>
    </WctFormField>
    <WctFormField label="Contributor">
      <InputText v-if="editing" v-model="targetDescription.contributor" :disabled="!editing" />
      <p v-else class="font-semibold">{{ targetDescription.contributor }}</p>
    </WctFormField>
    <WctFormField label="Type">
      <Dropdown v-if="editing"
        v-model="targetDescription.type"
        :options="types"
        :disabled="!editing"
      />
      <p v-else class="font-semibold">{{ targetDescription.type }}</p>
    </WctFormField>
    <WctFormField label="Format">
      <InputText v-if="editing" v-model="targetDescription.format" :disabled="!editing" />
      <p v-else class="font-semibold">{{ targetDescription.format}}</p>
    </WctFormField>
    <WctFormField label="Source">
      <InputText v-if="editing" v-model="targetDescription.source" :disabled="!editing" />
      <p v-else class="font-semibold">{{ targetDescription.source }}</p>
    </WctFormField>
    <WctFormField label="Language">
      <InputText v-if="editing" v-model="targetDescription.language" :disabled="!editing" />
      <p v-else class="font-semibold">{{ targetDescription.language }}</p>
    </WctFormField>
    <WctFormField label="Relation">
      <InputText v-if="editing" v-model="targetDescription.relation" :disabled="!editing" />
      <p v-else class="font-semibold">{{ targetDescription.relation }}</p>
    </WctFormField>
    <WctFormField label="Coverage">
      <InputText v-if="editing" v-model="targetDescription.coverage" :disabled="!editing" />
      <p v-else class="font-semibold">{{ targetDescription.coverage }}</p>
    </WctFormField>
    <WctFormField label="ISSN">
      <InputText v-if="editing" v-model="targetDescription.issn" :disabled="!editing" />
      <p v-else class="font-semibold">{{ targetDescription.issn }}</p>
    </WctFormField>
    <WctFormField label="ISBN">
      <InputText v-if="editing" v-model="targetDescription.isbn" :disabled="!editing" />
      <p v-else class="font-semibold">{{ targetDescription.isbn }}</p>
    </WctFormField>
  </WctTabViewPanel>
</template>

<style>

</style>