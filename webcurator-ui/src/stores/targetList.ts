// libraries
import { reactive, ref } from 'vue';
import { defineStore } from 'pinia';

// stores
import { useUserProfileStore } from '@/stores/users';
// utils
import { type UseFetchApis, useFetch } from '@/utils/rest.api';

export const targetListPageState = reactive({ totalRecords: 1, rows: 10, first: 0 });
export const useTargetListSearchStore = defineStore('TargetListSearchStore', () => {
  // Search conditions
  const targetId = ref(null);
  const targetName = ref('');
  const targetSeed = ref('');
  const targetDescription = ref('');
  const targetMemberOf = ref('');
  const nonDisplayOnly = ref(false);
  const targetAgency = ref();
  const targetUser = ref();
  const targetState = [] as any;
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
  };
});

export const useTargetListDataStore = defineStore('TargetListDataStore', () => {
  const userProfile = useUserProfileStore();
  const targetList = ref([]);
  const loadingTargetList = ref(false);
  const searchTerms = useTargetListSearchStore();
  const searchConditions = ref({} as any);
  const rest: UseFetchApis = useFetch();

  const resetFilter = () => {
    searchTerms.targetUser = {
      name: userProfile.currUserName,
      code: userProfile.name
    };

    searchTerms.targetAgency = {
      name: userProfile.agency,
      code: userProfile.agency
    };

    searchTerms.targetId = null;
    searchTerms.targetName = '';
    searchTerms.targetSeed = '';
    searchTerms.targetDescription = '';
    searchTerms.targetMemberOf = '';
    searchTerms.nonDisplayOnly = false;
    searchTerms.targetState = [];

    search();
  };

  const search = () => {
    const conditions = {
      targetId: searchTerms.targetId,
      name: searchTerms.targetName,
      seed: searchTerms.targetSeed,
      description: searchTerms.targetDescription,
      groupName: searchTerms.targetMemberOf,
      nonDisplayOnly: searchTerms.nonDisplayOnly,
      agency: searchTerms.targetAgency?.name,
      userId: searchTerms.targetUser?.code,
      states: [] as any
    };

    if (searchTerms.targetState?.length > 0) {
      for (const i in searchTerms.targetState) {
        conditions.states.push(searchTerms.targetState[i].code);
      }
    }

    searchConditions.value = conditions;
    targetListPageState.first = 0; // Reset to first page

    updatePage(0, targetListPageState.rows);
  };

  const updatePage = (first: number, rows: number) => {
    const searchParams = {
      filter: searchConditions.value,
      offset: first,
      limit: rows,
      sortBy: 'creationDate,asc'
    };

    loadingTargetList.value = true;
    rest
      .post('targets', searchParams, { header: 'X-HTTP-Method-Override', value: 'GET' })
      .then((data: any) => {
        targetList.value = data['targets'];
        targetListPageState.totalRecords = data['amount'];
        targetListPageState.first = data['offset'];
        targetListPageState.rows = data['limit'];
      })
      .catch((err: any) => {
        console.log(err.message);
      })
      .finally(() => {
        loadingTargetList.value = false;
      });
  };

  search();

  return { targetList, loadingTargetList, searchTerms, resetFilter, search, updatePage };
});
