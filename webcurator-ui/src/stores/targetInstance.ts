import { ref } from 'vue';
import { defineStore } from 'pinia';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';

export const useTargetInstanceStateStore = defineStore('TargetInstanceState', () => {
    const loadingTargetInstanceStates = ref(false);
    const rest: UseFetchApis = useFetch();

    const fetch = async () => {
        loadingTargetInstanceStates.value = true;
        const rsp = await rest.get('/target-instances/states');
        loadingTargetInstanceStates.value = false;
        return rsp;
    }
    return { fetch }
})