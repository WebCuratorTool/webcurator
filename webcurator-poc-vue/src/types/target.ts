interface Target {
    general: General;
    seeds: Seed[];
    annotations: [];
    profile: Profile;
}

interface General {
    name: string;
    owner: string;
    id: number;
    referenceNumber: string
}

interface Seed {
    id: number;
    seed: string;
}

interface Profile {
    id: number;
    harvesterType: string;
    name: string;
    overrides: []
}

export type {
    Target, General, Seed, Profile
}
