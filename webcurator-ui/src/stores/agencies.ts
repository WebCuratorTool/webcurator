import { defineStore } from "pinia";
import { computed, ref } from "vue";

import type { Agencies } from "@/types/agency";
import { useFetch, type UseFetchApis } from "@/utils/rest.api";

export const useAgenciesStore = defineStore("agencies", () => {
  const data = ref();
  const initialFetch = () => {
    const rest: UseFetchApis = useFetch();
    rest.get<Agencies>("agencies").then((rsp) => {
      data.value = rsp;
    });
  };

  const agencyList = computed(() => {
    const formatedData = [];
    for (let i = 0; i < data.value?.length; i++) {
      const item = data.value[i];
      formatedData.push({
        name: item["name"],
        code: item["id"],
      });
    }
    return formatedData;
  });

  const agencyListWithEmptyItem = computed(() => {
    const formatedData = [];
    for (let i = 0; i < data.value?.length; i++) {
      const item = data.value[i];
      formatedData.push({
        name: item["name"],
        code: item["id"],
      });
    }
    return formatedData;
  });

  initialFetch();

  return { data, initialFetch, agencyList, agencyListWithEmptyItem };
});
