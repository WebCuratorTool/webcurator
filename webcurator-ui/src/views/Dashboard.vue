<template>
  <div class="dashboard">
    <PageHeader title="Dashboard" />
    <Button label="Fetch Targets" @click="showTargets" />
  </div>
</template>

<script setup lang="ts">

import PageHeader from '@/components/PageHeader.vue';

import {type UseFetchApis, useFetch} from '../rest.api';

const rest: UseFetchApis=useFetch();

const showTargets = () => {
  const rsp= rest.post("targets", {});
  rsp.then((data:any)=>{
    console.log('fetch: ');
    console.log(data);
  }).catch((err:any)=>{
      console.log(err.message);
  });
}
</script>

<style>

</style>
