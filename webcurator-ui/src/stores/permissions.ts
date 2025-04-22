import { ref } from 'vue';
import { defineStore } from 'pinia';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import type { Permission } from '@/types/permission';

export const usePermissionStatusStore = defineStore('PermissionStatus', () => {
    const loadingPermissionStatuses = ref(false);
    const rest: UseFetchApis = useFetch();

    const fetch = async () => {
        loadingPermissionStatuses.value = true;
        const rsp = await rest.get('/harvest-authorisations/states');
        loadingPermissionStatuses.value = false;
        return rsp;
    }
    return { fetch }
})

export const usePermissionStore = defineStore('Permission', () => {
    const permission = ref();
    const loadingPermission = ref(false);
    const rest: UseFetchApis = useFetch();  

    const fetch = async (id: number): Promise<Permission> => {
        loadingPermission.value = true;
      
        try {
          const response = await rest.get(`permissions/${id}`);
          permission.value = response;
          loadingPermission.value = false;
          return response;
        } catch (err: any) {
          console.log(err.message);
          loadingPermission.value = false;
          return err;
        }
      }

    return { fetch, permission, loadingPermission }
})