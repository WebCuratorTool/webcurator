interface Target {
    general: TargetGeneral,
    description: TargetDescription,
    profile: TargetProfile,
    seeds: TargetSeeds,
    access: TargetAccess,
    groups: TargetGroups
}

interface TargetGeneral {
    id: number,
    name: string,
    creationDate: number,
    description: string,
    referenceNumber: string,
    runOnApproval: boolean,
    automatedQA: boolean,
    selectedUser: string,
    selectedState: {},
    autoPrune: boolean,
    referenceCrawl: boolean,
    requestToArchivists: string,
    nextStates: []
}

interface TargetSeeds {
    id: number,
    seed: string,
    primary: boolean,
    authorisations: []
}

interface TargetDescription {
    identifier: string,
    description: string,
    subject: string,
    creator: string,
    publisher: string,
    type: string,
    format: string,
    language: string,
    source: string,
    relation: string,
    contributor: string,
    coverage: string,
    issn: string,
    isbn: string,
}

interface TargetProfileOverride {
    id: string;
    value: string | number | boolean | any[];
    enabled: boolean;
    unit?: string; 
}

interface TargetProfile {
    id: number | null,
    harvesterType: string,
    imported: boolean,
    name: string,
    overrides: Array<TargetProfileOverride>
}

interface TargetGroup {
    id: number,
    name: string
}

interface TargetGroups extends Array<TargetGroup>{}

interface TargetAccess {
    displayTarget: boolean,
    accessZone: number,
    accessZoneText: string,
    displayChangeReason: string,
    displayNote: StreamPipeOptions
}

export type {
    Target,
    TargetAccess,
    TargetDescription,
    TargetGeneral,
    TargetGroup,
    TargetGroups,
    TargetProfile,
    TargetProfileOverride,
    TargetSeeds
}