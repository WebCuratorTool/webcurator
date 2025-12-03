import { defineStore } from "pinia";
import { ref } from "vue";

import type { Annotation } from "@/types/annotation";
import type { TargetInstance } from "@/types/targetInstance";
import { useFetch, type UseFetchApis } from "@/utils/rest.api";

type Filter = { targetId?: number } | Record<string, unknown>;

type TargetInstanceSearchTerms = {
  filter?: Filter;
  limit?: number;
  includeAnnotations?: boolean;
};

type TargetInstanceSearchResponse = {
  filter?: Filter;
  limit?: number;
  includeAnnotations?: boolean;
  sortBy?: string;
  targetInstances: Array<TargetInstance>;
};

export const useTargetInstanceListSearchStore = defineStore(
  "TargetInstanceListSearchStore",
  () => {
    // Search conditions
    const targetId = ref(null);

    return { targetId };
  },
);

export const useTargetInstanceListStore = defineStore(
  "TargetInstanceList",
  () => {
    const loadingTargetInstanceList = ref(false);
    const rest: UseFetchApis = useFetch();

    const search = async (searchTerms: TargetInstanceSearchTerms) => {
      let targetInstanceList = <Array<TargetInstance>>[];

      loadingTargetInstanceList.value = true;
      try {
        const data: TargetInstanceSearchResponse = await rest.post(
          "target-instances",
          searchTerms,
          {
            header: "X-HTTP-Method-Override",
            value: "GET",
          },
        );
        targetInstanceList = data.targetInstances;
      } finally {
        loadingTargetInstanceList.value = false;
      }

      return targetInstanceList;
    };

    const getTargetInstanceAnnotations = async (targetId: number) => {
      const targetInstanceAnnotations = ref(<Array<Annotation>>[]);
      const targetInstances = await search({
        filter: { targetId: targetId },
        limit: -1,
        includeAnnotations: true,
      });

      targetInstances.forEach((targetInstance: TargetInstance) => {
        if (
          targetInstance.annotations &&
          targetInstance.annotations.length > 0
        ) {
          targetInstance.annotations.forEach((annotation: Annotation) => {
            // add targetInstanceId, used for rendering in Target Annotations view
            annotation.targetInstanceId = targetInstance.id;
            targetInstanceAnnotations.value.push(annotation);
          });
        }
      });

      return targetInstanceAnnotations.value;
    };

    return { search, loadingTargetInstanceList, getTargetInstanceAnnotations };
  },
);
