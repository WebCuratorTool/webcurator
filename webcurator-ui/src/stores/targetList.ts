import { ref } from 'vue';
import { defineStore } from 'pinia';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { useUsersStore, useUserProfileStore } from '@/stores/users'

export const useTargetListSearchStore = defineStore('TargetListSearchStore', () => {
    // Search conditions
    const targetId = ref(null)
    const targetName = ref('')
    const targetSeed = ref('')
    const targetDescription = ref('')
    const targetMemberOf = ref('')

    const noneDisplayOnly = ref(false)

    return { 
        targetId,
        targetName,
        targetSeed, 
        targetDescription, 
        targetMemberOf, 
        noneDisplayOnly
    }
}) 

export const useTargetListFiltertore = defineStore('TargetListFilterStore', () => {
    // Filter conditions
    const selectedAgency = ref({ name: "", code: "" })
    const selectedUser = ref({ name: "", code: "" })

    const selectedState = ref([])

    return { 
        selectedAgency, 
        selectedUser, 
        selectedState, 
    }
})

export const useTargetListDataStore = defineStore('TargetListDataStore', () => {
    const userProfile = useUserProfileStore();
    const users = useUsersStore();
    const targetList = ref([]);
    const loadingTargetList = ref(false);
    const filteredTargetList = ref();
    const searchTerms = useTargetListSearchStore();
    const filters = useTargetListFiltertore();
    const rest: UseFetchApis = useFetch();

    const filter = () => {
        const ret = [];
        for (let idx = 0; idx < targetList.value.length; idx++) {
            const target: any = targetList.value[idx];
            if (filters.selectedAgency.code !== "" && target.agency !== filters.selectedAgency.name) {
                continue;
            }

            if (filters.selectedUser.code !== "" && target.owner !== filters.selectedUser.code) {
                continue;
            }

            let isInSelectedStates = false;
            for (const idx in filters.selectedState) {
            const stateOption: any = filters.selectedState[idx];
            if (target.state === stateOption.code) {
                isInSelectedStates = true;
                break;
            }
            }
            if (filters.selectedState.length > 0 && !isInSelectedStates) {
            continue;
            }
            ret.push(target);
        }
        filteredTargetList.value = ret;
    }

    const resetFilter = () => {
      filters.selectedUser = {
        name: userProfile.currUserName,
        code: userProfile.name
      }
    
      filters.selectedAgency = {
        name: userProfile.agency,
        code: userProfile.agency
      }
    
      filters.selectedState = [];
    
      filter();
    }

    const search = () => {
        const searchConditions = {
          targetId: searchTerms.targetId,
          name: searchTerms.targetName,
          seed: searchTerms.targetSeed,
          description: searchTerms.targetDescription,
          groupName: searchTerms.targetMemberOf,
          nonDisplayOnly: searchTerms.noneDisplayOnly,
        }
      
        const searchParams = {
          filter: searchConditions,
          offset: 0,
          limit: -1,
          sortBy: 'creationDate,asc'
        }
      
        loadingTargetList.value = true
        rest
          .post('targets', searchParams)
          .then((data: any) => {
            console.log(data)
            targetList.value = data['targets']
            console.log(targetList.value)
            filter();
          })
          .catch((err: any) => {
            console.log(err.message)
          }).finally(()=>{
            loadingTargetList.value = false
          });
      }

      search();
      
      return {targetList, loadingTargetList, filteredTargetList, filters, searchTerms, filter, resetFilter, search}
})