import { useUserProfileStore, useUsersStore } from '@/stores/users'
import { type UseFetchApis, useFetch } from '@/utils/rest.api'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTargetListSearchStore = defineStore('TargetListSearchStore', () => {

  // Search conditions
  const targetId = ref(null)
  const targetName = ref('')
  const targetSeed = ref('')
  const targetDescription = ref('')
  const targetMemberOf = ref('')
  const nonDisplayOnly = ref(false)
  const targetAgency = ref()
  const targetUser = ref()
  const targetState = ([] as any);
  return {
    targetId,
    targetName,
    targetSeed,
    targetDescription,
    targetMemberOf,
    nonDisplayOnly,
    targetAgency,
    targetUser,
    targetState
  }
})

export const useTargetListFiltertore = defineStore('TargetListFilterStore', () => {
  // Filter conditions
  const selectedAgency = ref({ name: '', code: '' })
  const selectedUser = ref({ name: '', code: '' })

  const selectedState = ref([])

  return {
    selectedAgency,
    selectedUser,
    selectedState
  }
})

export const useTargetListDataStore = defineStore('TargetListDataStore', () => {
  const userProfile = useUserProfileStore()
  const users = useUsersStore()
  const targetList = ref([])
  const loadingTargetList = ref(false)
  const searchTerms = useTargetListSearchStore()
  const filters = useTargetListFiltertore()
  const rest: UseFetchApis = useFetch()

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

    searchTerms.targetId = null;
    searchTerms.targetName = '';
    searchTerms.targetSeed = '';
    searchTerms.targetDescription = '';
    searchTerms.targetMemberOf = '';
    searchTerms.nonDisplayOnly = false;
    searchTerms.targetAgency = null;
    searchTerms.targetUser = null;
    searchTerms.targetState = [];

    search();
  }

    const search = () => {
      const searchConditions = {
        targetId: searchTerms.targetId,
        name: searchTerms.targetName,
        seed: searchTerms.targetSeed,
        description: searchTerms.targetDescription,
        groupName: searchTerms.targetMemberOf,
        nonDisplayOnly: searchTerms.nonDisplayOnly,
        agency: searchTerms.targetAgency?.name,
        userId: searchTerms.targetUser?.code,
        states: [] as any
      }

      if (searchTerms.targetState?.length > 0) {
        for (const i in searchTerms.targetState) {
          searchConditions.states.push(searchTerms.targetState[i].code)
        }
      }
    
      const searchParams = {
        filter: searchConditions,
        offset: 0,
        limit: 10,
        sortBy: 'creationDate,asc'
      }
    
      loadingTargetList.value = true
      rest
        .post('targets', searchParams, { header: 'X-HTTP-Method-Override', value: 'GET' })
        .then((data: any) => {
          targetList.value = data['targets'];
        })
        .catch((err: any) => {
          console.log(err.message)
        }).finally(()=>{
          loadingTargetList.value = false;
        });
    }

      search();

      return { targetList, loadingTargetList, filters, searchTerms, resetFilter, search }
})
