interface Profile {
  agency: string;
  name: string;
  description: string;
  id: number;
  state: number;
  type: string;
}

type Profiles = Array<Profile>;

interface ProfilesResponse {
  filter: {
    showOnlyActive: boolean;
    agency: string;
    type: string;
  };
  amount: number;
  profiles: Profiles;
}

export type { Profile, Profiles, ProfilesResponse };
