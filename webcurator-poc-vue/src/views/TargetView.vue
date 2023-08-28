<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useRoute } from 'vue-router'
import { useTargetStore } from '@/stores/target'

import Tabs from '@/components/Tabs/Tabs.vue'
import Tab from '@/components/Tabs/Tab.vue'
import General from '@/components/Target/General.vue'
import Annotations from '@/components/Target/Annotations.vue'
import Profile from '@/components/Target/Profile.vue'

const { fetchTarget } = useTargetStore()
const { loading, error, target } = storeToRefs(useTargetStore())

const route = useRoute()
const id = route.params.id as string

fetchTarget(id)
</script>

<template>
    <div v-if="loading">Loading...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else>
        <div class="page-title">Target {{ target.general.name }}</div>
        <div class="page-container">
            <Tabs>
                <Tab :name="'General'" :selected=true>
                    <General :general=target.general :seeds=target.seeds />
                </Tab>
                <Tab :name="'Description'">Description</Tab>
                <Tab :name="'Profile'">
                    <Profile :profile=target.profile />
                </Tab>
                <Tab :name="'Harvests'">Harvests</Tab>
                <Tab :name="'Annotations'">
                    <Annotations :annotations=target.annotations />
                </Tab>
                <Tab :name="'Access'">Access</Tab>
            </Tabs>
        </div>
    </div>

</template>