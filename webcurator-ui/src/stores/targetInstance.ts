import { defineStore } from "pinia";
import { ref } from "vue";

import { useFetch, type UseFetchApis } from "@/utils/rest.api";

export const useTargetInstanceStateStore = defineStore(
  "TargetInstanceState",
  () => {
    const loadingTargetInstanceStates = ref(false);
    const rest: UseFetchApis = useFetch();

    const fetch = async () => {
      loadingTargetInstanceStates.value = true;
      const rsp: Record<number, string> = await rest.get(
        "/target-instances/states",
      );
      loadingTargetInstanceStates.value = false;
      return rsp;
    };
    return { fetch };
  },
);
