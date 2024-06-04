import { ref } from 'vue';
import { defineStore } from 'pinia';
import type { Profiles } from '@/types/profile';

export const useProfiles = defineStore('Profiles', () => {
    const profiles = ref([] as Profiles);

    const setProfiles = (data: any) => {
        profiles.value = data.profiles;
    }

    return { profiles, setProfiles } 
})