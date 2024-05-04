import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';

export const useAgenciesStore = defineStore('agencies', () => {
  const data = ref([]);
  const initialFetch = () => {
    const rest: UseFetchApis = useFetch();
    rest.get("agencies").then((rsp: any) => {
      data.value = rsp;
    }).catch((err: any) => {
      console.log(err.message);
    });
  }

  const agencyList = computed(() => {
    const formatedData = [];
    for (var i = 0; i < data.value.length; i++) {
      var item = data.value[i];
      formatedData.push({
        "name": item["name"],
        "code": item["id"],
      });
    }
    return formatedData;
  });

  const agencyListWithEmptyItem = computed(() => {
    const formatedData = [{
      "name": " ",
      "code": "",
    }];
    for (var i = 0; i < data.value.length; i++) {
      var item = data.value[i];
      formatedData.push({
        "name": item["name"],
        "code": item["id"],
      });
    }
    return formatedData;
  });

  return { data, initialFetch, agencyList, agencyListWithEmptyItem }
});
