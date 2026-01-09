<template>
  <div class="dashboard">
    <p class="title pt-8">Targets</p>

    <Button label="Fetch Targets" @click="showTargets" />
  </div>
</template>

<script setup lang="ts">

import {type UseFetchApis, useFetch} from '../utils/rest.api';

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
