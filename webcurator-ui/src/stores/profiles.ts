import { defineStore } from "pinia";
import { ref } from "vue";

import type { Profiles } from "@/types/profile";
import { useFetch, type UseFetchApis } from "@/utils/rest.api";

interface ProfilesResponse {
  filter?: Record<string, unknown>;
  amount?: number;
  profiles: Profiles;
}

export const useProfiles = defineStore("Profiles", () => {
  const profiles = ref([] as Profiles);
  const loadingProfiles = ref(false);
  const rest: UseFetchApis = useFetch();
  const fetchProfiles = async () => {
    loadingProfiles.value = true;
    try {
      const data: ProfilesResponse = await rest.get("profiles/");
      profiles.value = data.profiles;
    } catch (err: any) {
      console.log(err.message);
    } finally {
      loadingProfiles.value = false;
    }
  };

  return { profiles, loadingProfiles, fetchProfiles };
});
