<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useRoute } from 'vue-router'
import { ref } from 'vue'
import { useTargetStore } from '@/stores/target'

import Tabs from '@/components/Tabs/Tabs.vue'
import Tab from '@/components/Tabs/Tab.vue'
import General from '@/components/Target/General.vue'
import Annotations from '@/components/Target/Annotations.vue'
import Profile from '@/components/Target/Profile.vue'
import EditGeneral from '@/components/Target/edit/EditGeneral.vue'

const { fetchTarget, updateTarget } = useTargetStore()
const { loading, error, target } = storeToRefs(useTargetStore())

const route = useRoute()
const id = route.params.id as string

const editView = ref('')

const setEditView = (view: string) => {
    editView.value = view
}

const onEditTarget = (data: {}) => {
    updateTarget(id, data)
} 

fetchTarget(id)
</script>

<template>
    <div v-if="loading">Loading...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else>
        <div class="page-title">Target {{ target.general.name }}</div>
        <div class="page-container">
            <Tabs v-if="editView == ''">
                <Tab :name="'General'" :selected=true>
                    <General :general=target.general :seeds=target.seeds @on-edit="setEditView" />
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
            <EditGeneral v-else-if="editView == 'general'" :general=target.general @on-edit="setEditView" @on-save="onEditTarget"/>
        </div>
    </div>

</template>