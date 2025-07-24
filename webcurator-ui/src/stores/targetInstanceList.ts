import { ref } from 'vue';
import { defineStore } from 'pinia';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';

export const useTargetInstanceListSearchStore = defineStore('TargetInstanceListSearchStore', () => {
    // Search conditions
    const targetId = ref(null)

    return { targetId }
})

export const useTargetInstanceListStore = defineStore('TargetInstanceList', () => {
    const loadingTargetInstanceList = ref(false);
    const rest: UseFetchApis = useFetch();
  
    const search = async (searchTerms: any) => { 
      let targetInstanceList: never[] = [];
    
      loadingTargetInstanceList.value = true
      try {
        const data = await rest.post('target-instances', searchTerms, { header: 'X-HTTP-Method-Override', value: 'GET' })
        targetInstanceList = data['targetInstances']
      } catch (err: any) {
        console.log(err.message)
      } finally {
        loadingTargetInstanceList.value = false
      }
    
      return targetInstanceList;
    }

    const getTargetInstanceAnnotations = async (targetId: number) => {
      const targetInstanceAnnotations = ref([] as any);
      const targetInstances = await search({ filter: { targetId: targetId }, limit: -1, includeAnnotations: true });
      targetInstances.forEach((targetInstance: any) => {

        if (targetInstance.annotations && targetInstance.annotations.length > 0) {
          targetInstanceAnnotations.value.push(targetInstance);
          // targetInstance.annotations.forEach((annotation: any) => {
          //   targetInstanceAnnotations.value.push(annotation);
          // })
        }
      })
      return targetInstanceAnnotations.value;
    }

    return { search, loadingTargetInstanceList, getTargetInstanceAnnotations }
});
