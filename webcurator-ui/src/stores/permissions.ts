import { ref } from 'vue';
import { defineStore } from 'pinia';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';

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