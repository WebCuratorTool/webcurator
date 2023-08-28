<script setup lang="ts">
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useTargetsStore } from '@/stores/targets'

import Tabs from '@/components/Tabs/Tabs.vue'
import Tab from '@/components/Tabs/Tab.vue'
import Table from '@/components/Table.vue'
import AttributeSearch from '@/components/Targets/AttributeSearch.vue'

const { loading, error, targets } = storeToRefs(useTargetsStore())
const { fetchTargets } = useTargetsStore()

const router = useRouter()

fetchTargets()

const fields = [
  {display: 'ID', value:'id'}, 
  {display: 'Created', value: 'creationDate'},
  {display: 'Name', value: 'name'},  
  {display: 'Owner', value: 'owner'},
  {display: 'Status', value: 'state'},
]

const searchTargets = (searchTerms: {}) => {
  fetchTargets(searchTerms)
}

const navigateToTarget = (id: string) => {
  if (router) {
    router.push(`/target/${id}`)
  }
}
</script>

<template>
  <div class="page-title">Targets</div>
  <div class="page-container">
    <div v-if="loading">Loading...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else>
      <Tabs>
        <Tab :name="'Attribute Search'" :selected=true>
          <AttributeSearch @on-search="searchTargets" />
        </Tab>
        <Tab :name="'String Search'"/>
      </Tabs>
      <Table  :fields="fields" :data="targets" @on-click="navigateToTarget"></Table>
    </div>
  </div>
</template>