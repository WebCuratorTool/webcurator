interface Profile {
    agency: string,
    name: string,
    description: string,
    id: number,
    state: number,
    type: string
}

interface Profiles extends Array<Profile>{}

export type { Profile, Profiles }