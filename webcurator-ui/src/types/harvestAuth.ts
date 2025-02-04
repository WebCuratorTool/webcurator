interface HarvestAuth {
    id: number;
    name: string;
    agent: string;
    permissionId: number;
    startDate: number;
    endDate: number;
}

interface HarvestAuths extends Array<HarvestAuth> {}

export type { HarvestAuth, HarvestAuths };