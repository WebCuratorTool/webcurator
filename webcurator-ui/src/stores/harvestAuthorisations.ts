import { ref } from "vue";
import { defineStore } from "pinia";
import { useFetch, type UseFetchApis } from "@/utils/rest.api";

export const useHarvestAuthorisationStatusStore = defineStore('HarvestAuthorisationStatus', () => {
    const loadingHarvestAuthorisationStatuses = ref(false);
    const statuses = ref();
    const rest: UseFetchApis = useFetch();
  
    const fetch = async () => {
      if (statuses.value) {       
        return statuses.value;
      }
      loadingHarvestAuthorisationStatuses.value = true;
      const rsp = await rest.get('/harvest-authorisations/states');
      loadingHarvestAuthorisationStatuses.value = false;
      statuses.value = rsp;     
      return rsp;
    }
    return { fetch, statuses }
  })
  