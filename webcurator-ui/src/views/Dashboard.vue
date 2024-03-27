<template>
  <div class="dashboard">
    <PageHeader title="Dashboard" />
    <Button label="Login" @click="showProducts" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed} from 'vue'

import PageHeader from '@/components/PageHeader.vue';

import {type UseFetchApis, useFetch} from '../rest.api';

const fetch=ref(false);
const method=ref("");
const url=ref("");
const reqdata=ref(null);
const timestamp=ref("");

// const { data, error } = useFetch(options);
const rest: UseFetchApis=useFetch();

const showProducts = () => {
  // console.log(req);
  let curr = new Date();

  // fetch.value=false;
  fetch.value=true;
  method.value="get";
  url.value="./api/v1/targets";
  reqdata.value=null;
  timestamp.value=curr.toLocaleTimeString();

  const rsp= rest.get("./api/v1/targets");
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
../restclient.ts