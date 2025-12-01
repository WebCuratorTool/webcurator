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
      const response = await rest.get(`permissions/${id}`);
      permission.value = response;
      loadingPermission.value = false;
    } catch (err: any) {
      console.log(err.message);
      loadingPermission.value = false;
    }
  };

  return { fetch, permission, loadingPermission };
});
