import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

export const useAgenciesStore = defineStore('agencies', () => {
  const data = ref([]);
  const initialFetch = () => {
    const rest: UseFetchApis = useFetch();
    rest
      .get('agencies')
      .then((rsp: any) => {
        data.value = rsp;
      })
      .catch((err: any) => {
        console.log(err.message);
      });
  };

  const agencyList = computed(() => {
    const formatedData = [];
    for (let i = 0; i < data.value.length; i++) {
      const item = data.value[i];
      formatedData.push({
        name: item['name'],
        code: item['id']
      });
    }
    return formatedData;
  });

  const agencyListWithEmptyItem = computed(() => {
    const formatedData = [];
    for (let i = 0; i < data.value.length; i++) {
      const item = data.value[i];
      formatedData.push({
        name: item['name'],
        code: item['id']
      });
    }
    return formatedData;
  });

  initialFetch();

  return { data, initialFetch, agencyList, agencyListWithEmptyItem };
});
