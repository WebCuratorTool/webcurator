import { defineStore } from "pinia";
import { ref } from "vue";

import type { Permission } from "@/types/permission";
import { useFetch, type UseFetchApis } from "@/utils/rest.api";

type Filter =
  | {
      targetId?: number;
      url?: string;
      harvestAuthorisationName?: string;
    }
  | Record<string, unknown>;

type PermissionSearchTerms = {
  filter?: Filter;
  page?: number;
};

type PermissionSearchResponse = {
  filter: Filter;
  amount: number;
  permissions: Array<Permission>;
};

export const usePermissionsStore = defineStore("Permissions", () => {
  const loadingPermissions = ref(false);
  const amount = ref(0);
  const rest: UseFetchApis = useFetch();

  const search = async (searchTerms: PermissionSearchTerms) => {
    let permissions = <Array<Permission>>[];
    loadingPermissions.value = true;
    try {
      const data: PermissionSearchResponse = await rest.post(
        "permissions",
        searchTerms,
        {
          header: "X-HTTP-Method-Override",
          value: "GET",
        },
      );
      permissions = data.permissions;
      amount.value = data.amount;
    } finally {
      loadingPermissions.value = false;
    }

    return permissions;
  };

  return {
    amount,
    loadingPermissions,
    search,
  };
});
