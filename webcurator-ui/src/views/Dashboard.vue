<template>
  <div class="dashboard">
    <PageHeader title="Dashboard" />
    <Button label="Login" @click="showProducts" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed} from 'vue'

import PageHeader from '@/components/PageHeader.vue';

import {type UseFetchReturn, useFetch} from '../rest.api';

const fetch=ref(false);
const method=ref("");
const url=ref("");
const reqdata=ref(null);
const timestamp=ref("");

// const options = computed(() => {
//   const optionsValue:FetchOptions={
//     fetch: fetch.value,
//     method: method.value,
//     url:url.value,
//     data:reqdata.value,
//     timestamp:timestamp.value,
//   }
//   return optionsValue;
// });


// const { data, error } = useFetch(options);
const rest: UseFetchReturn=useFetch();

const showProducts = () => {
  // console.log(req);
  let curr = new Date();

  // fetch.value=false;
  fetch.value=true;
  method.value="get";
  url.value="./api/v1/targets";
  reqdata.value=null;
  timestamp.value=curr.toLocaleTimeString();

  const { data, error } = rest.get("./api/v1/targets", null);

  console.log('fetch: ' + data.value);
}
</script>

<style>

</style>
../restclient.ts