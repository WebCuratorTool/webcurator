<template>
  <div class="dashboard">
    <PageHeader title="Dashboard" />
    <Button label="Login" @click="showProducts" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed} from 'vue'

import PageHeader from '@/components/PageHeader.vue';

import {type FetchOptions, useFetch} from '../api';

const fetch=ref(false);
const method=ref("");
const url=ref("");
const reqdata=ref(null);
const timestamp=ref("");

const options = computed(() => {
  const optionsValue:FetchOptions={
    fetch: fetch.value,
    method: method.value,
    url:url.value,
    data:reqdata.value,
    timestamp:timestamp.value,
  }
  return optionsValue;
});


const { data, error } = useFetch(options);

const showProducts = () => {
  // console.log(req);
  let curr = new Date();

  // fetch.value=false;
  fetch.value=true;
  method.value="get";
  url.value="./api/v1/targets";
  reqdata.value=null;
  timestamp.value=curr.toLocaleTimeString();


  console.log('fetch: ' + fetch.value);
}
</script>

<style>

</style>
../restclient.ts