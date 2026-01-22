import { defineStore } from "pinia";
import { ref } from "vue";

import { useFetch, type UseFetchApis } from "@/utils/rest.api";

export const usePermissionStore = defineStore("Permission", () => {
  const permission = ref();
  const loadingPermission = ref(false);
  const rest: UseFetchApis = useFetch();

  const fetch = async (id: number) => {
    loadingPermission.value = true;

    try {
      permission.value = await rest.get(`permissions/${id}`);
    } finally {
      loadingPermission.value = false;
    }
  };

  return { fetch, permission, loadingPermission };
});
