import { ref } from 'vue';
import { defineStore } from 'pinia';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';

export const useTargetInstanceListSearchStore = defineStore('TargetInstanceListSearchStore', () => {
    // Search conditions
    const targetId = ref(null)

    return { targetId }
})

export const useTargetInstanceListStore = defineStore('TargetInstanceList', () => {
    // const targetInstanceList = ref([]);
    // const searchTerms = useTargetInstanceListSearchStore();
    const loadingTargetInstanceList = ref(false);
    const rest: UseFetchApis = useFetch();
    
    const search = async (searchTerms: any) => { 
        let targetInstanceList: never[] = [];
        // const searchConditions = {
        //     targetId: searchTerms.targetId,
        //   }

          const searchParams = {
            filter: searchTerms,
            offset: 0,
            limit: 2000,
          }
        
          loadingTargetInstanceList.value = true
          rest
            .post('target-instances', searchParams, { header: 'X-HTTP-Method-Override', value: 'GET' })
            .then((data: any) => {
              targetInstanceList = data['targetInstances']
            })
            .catch((err: any) => {
              console.log(err.message)
            }).finally(() => {
              loadingTargetInstanceList.value = false
              console.log(targetInstanceList, searchTerms)
              return targetInstanceList;

            });
            
    }

    return {  search, loadingTargetInstanceList }
});
