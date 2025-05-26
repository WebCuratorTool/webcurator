<script setup lang="ts">
import { useProfiles } from '@/stores/profiles';
import { useTargetProfileDTO } from '@/stores/target';
import { camelCaseToTitleCase } from '@/utils/helper';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { computed, ref, watch } from 'vue';

import Loading from '@/components/Loading.vue';
import WctFormField from '@/components/WctFormField.vue';
import WctTabViewPanel from '@/components/WctTabViewPanel.vue';
import type { Profile } from '@/types/profile';

const rest: UseFetchApis = useFetch();

const props = defineProps<{
  editing: boolean;
}>();

const loading = ref(false);

const targetProfile = useTargetProfileDTO().targetProfile;

const timeUnits = ref(['SECOND', 'MINUTE', 'HOUR', 'DAY', 'WEEK']);

const sizeUnits = ref(['B', 'KB', 'MB', 'GB']);

const harvesterTypes = ref(['HERITRIX1', 'HERITRIX3']);

const profileStore = useProfiles();
const selectedHarvesterType = ref(targetProfile.harvesterType);
const selectedProfile = ref({} as Profile | undefined);

const onChangeProfile = (event: any) => {
  useTargetProfileDTO().setProfile(event.value);
};

const fetchProfile = async () => {
  loading.value = true;
  try {
    const datasets = await rest.get('profiles/');
    if (!datasets || !datasets.profiles) {
      loading.value = false;
      console.log('Failed to load profiles');
      return;
    }
    profileStore.setProfiles(datasets);
    selectedProfile.value = datasets.profiles.find((profile: any) => profile.id == targetProfile.id);
  } finally {
    loading.value = false;
  }
};

const baseProfileOptions = computed(() => {
  return profileStore.profiles.filter((profile) => profile.type == selectedHarvesterType.value);
});

watch(
  () => props.editing,
  async (newEditing) => {
    if (newEditing) {
      await fetchProfile();
    }
  }
);
</script>

<template>
  <Loading v-if="loading" />
  <div v-else>
    <h4 class="mt-4">Base Profile</h4>
    <WctTabViewPanel>
      <WctFormField label="Harvester Type">
        <Select v-if="editing" v-model="selectedHarvesterType" :options="harvesterTypes" :disabled="!editing" />
        <p v-else class="font-semibold">{{ targetProfile.harvesterType }}</p>
      </WctFormField>
      <WctFormField label="Base Profile">
        <Select v-if="editing" v-model="selectedProfile" :options="baseProfileOptions" optionLabel="name" :disabled="!editing" @change="onChangeProfile" />
        <p v-else class="font-semibold">{{ targetProfile.name }}</p>
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
              <Checkbox v-if="editing" v-model="data.value" :binary="true" :disabled="!editing" />
              <p v-else class="font-semibold">{{ data.value ? 'Yes' : 'No' }}</p>
            </div>
            <div v-else-if="Array.isArray(data.value) || typeof data.value == 'string'">
              <InputText v-if="editing" v-model="data.value" :disabled="!editing" />
              <p v-else class="font-semibold">{{ data.value.toString() }}</p>
            </div>
            <div v-else style="min-width: 30rem">
              <div v-if="editing" class="flex justify-start w-full">
                <InputNumber
                  class="w-6"
                  v-model="data.value"
                  inputId="minmax-buttons"
                  mode="decimal"
                  :minFractionDigits="data.id == 'dataLimit' || data.id == 'timeLimit' ? 1 : 0"
                  showButtons
                  :min="0"
                  :disabled="!editing"
                  style="width: 16rem"
                />
                <Select v-if="data.id == 'dataLimit'" class="ml-2" v-model="data.unit" :options="sizeUnits" :disabled="!editing" />
                <Select v-else-if="data.id == 'timeLimit'" class="ml-2" v-model="data.unit" :options="timeUnits" :disabled="!editing" />
              </div>
              <div v-else>
                <p class="font-semibold">{{ `${data.value ? data.value : ''} ${data.unit ? data.unit : ''}` }}</p>
              </div>
            </div>
          </template>
        </Column>
        <Column field="enabled" header="Enable Override">
          <template #body="{ data }">
            <Checkbox v-if="editing" id="checkOption1" name="option1" value="Run on Approval" v-model="data.enabled" :binary="true" :disabled="!editing" />
            <p v-else class="font-semibold">{{ data.value ? 'Yes' : 'No' }}</p>
          </template>
        </Column>
      </DataTable>
    </WctTabViewPanel>
  </div>
</template>

<style></style>
