import { defineStore } from "pinia";
import { reactive, ref } from "vue";

import { useUserProfileStore } from "@/stores/users";
import type { Target } from "@/types/target";
import { useFetch, type UseFetchApis } from "@/utils/rest.api";

interface SearchConditions {
  targetId: number | null;
  name: string;
  seed: string;
  description: string;
  groupName: string;
  nonDisplayOnly: boolean;
  agency: string | null;
  userId: string | null;
  states: Array<number>;
}

interface TargetListResponse {
  amount: number;
  offset: number;
  limit: number;
  targets: Array<Target>;
}

interface TargetSearchParams {
  filter: SearchConditions;
  offset: number;
  limit: number;
  sortBy: string;
}

export const targetListPageState = reactive({
  totalRecords: 1,
  rows: 10,
  first: 0,
});

export const useTargetListSearchStore = defineStore(
  "TargetListSearchStore",
  () => {
    // Search conditions
    const targetId = ref(null);
    const targetName = ref("");
    const targetSeed = ref("");
    const targetDescription = ref("");
    const targetMemberOf = ref("");
    const nonDisplayOnly = ref(false);
    const targetAgency = ref();
    const targetUser = ref();
    const targetState = ref<Array<{ name: string; code: number }>>([]);
    return {
      targetId,
      targetName,
      targetSeed,
      targetDescription,
      targetMemberOf,
      nonDisplayOnly,
      targetAgency,
      targetUser,
      targetState,
    };
  },
);

export const useTargetListDataStore = defineStore("TargetListDataStore", () => {
  const userProfile = useUserProfileStore();
  const targetList = ref(Array<Target>());
  const loadingTargetList = ref(false);
  const searchRequestId = ref(0);
  const searchTerms = useTargetListSearchStore();
  const searchConditions = ref<SearchConditions>({
    targetId: null,
    name: "",
    seed: "",
    description: "",
    groupName: "",
    nonDisplayOnly: false,
    agency: null,
    userId: null,
    states: Array<number>(),
  });
  const rest: UseFetchApis = useFetch();

  const getSearchConditions = () => {
    const conditions = {
      targetId: searchTerms.targetId,
      name: searchTerms.targetName,
      seed: searchTerms.targetSeed,
      description: searchTerms.targetDescription,
      groupName: searchTerms.targetMemberOf,
      nonDisplayOnly: searchTerms.nonDisplayOnly,
      agency: searchTerms.targetAgency?.name,
      userId: searchTerms.targetUser?.code,
      states: Array<number>(),
    };

    if (searchTerms.targetState?.length > 0) {
      for (const i in searchTerms.targetState) {
        conditions.states.push(searchTerms.targetState[i].code);
      }
    }

    return conditions;
  };

  const resetFilter = () => {
    searchTerms.targetUser = {
      name: userProfile.currUserName,
      code: userProfile.name,
    };

    searchTerms.targetAgency = {
      name: userProfile.agency,
      code: userProfile.agency,
    };

    searchTerms.targetId = null;
    searchTerms.targetName = "";
    searchTerms.targetSeed = "";
    searchTerms.targetDescription = "";
    searchTerms.targetMemberOf = "";
    searchTerms.nonDisplayOnly = false;
    searchTerms.targetState = [];

    search();
  };

  const search = () => {
    searchConditions.value = getSearchConditions();
    targetListPageState.first = 0; // Reset to first page

    updatePage(0, targetListPageState.rows);
  };

  const executeSearch = (searchParams: TargetSearchParams) => {
    const requestId = ++searchRequestId.value;
    loadingTargetList.value = true;

    return rest
      .post<TargetListResponse>("targets", searchParams, {
        header: "X-HTTP-Method-Override",
        value: "GET",
      })
      .then((data: TargetListResponse) => {
        if (requestId !== searchRequestId.value) {
          return undefined;
        }

        return data;
      })
      .finally(() => {
        if (requestId === searchRequestId.value) {
          loadingTargetList.value = false;
        }
      });
  };

  const updatePage = (first: number, rows: number) => {
    const searchParams = {
      filter: searchConditions.value,
      offset: first,
      limit: rows,
      sortBy: "creationDate,asc",
    };

    executeSearch(searchParams).then((data) => {
      if (!data) {
        return;
      }

      targetList.value = data["targets"];
      targetListPageState.totalRecords = data["amount"];
      targetListPageState.first = data["offset"];
      targetListPageState.rows = data["limit"];
    });
  };

  search();

  return {
    targetList,
    loadingTargetList,
    searchTerms,
    resetFilter,
    search,
    updatePage,
    pageState: targetListPageState,
  };
});
