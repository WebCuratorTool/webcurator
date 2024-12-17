import type { Annotation } from '@/types/annotation';

interface Target {
    access: TargetAccess,
    annotations: TargetAnnotations,
    description: TargetDescription,
    general: TargetGeneral,
    groups: TargetGroups
    profile: TargetProfile,
    seeds: TargetSeeds,
}

interface TargetAccess {
    displayTarget: boolean,
    accessZone: number,
    accessZoneText: string,
    displayChangeReason: string,
    displayNote: string
}

interface TargetAnnotations {
    evaluationNote: string,
    harvestType: string,
    annotations: Array<Annotation>,
    selection: []
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

interface TargetGroup {
    id: number,
    name: string
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

interface TargetGroups extends Array<TargetGroup>{}

interface TargetSeed {
    id: number,
    seed: string,
    primary: boolean,
    authorisations: []
}

interface TargetSeeds extends Array<TargetSeed>{}

export type {
    Target,
    TargetAccess,
    TargetAnnotations,
    TargetDescription,
    TargetGeneral,
    TargetGroup,
    TargetGroups,
    TargetProfile,
    TargetProfileOverride,
    TargetSeed,
    TargetSeeds
}