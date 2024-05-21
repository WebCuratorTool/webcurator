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

export type {
    TargetGeneral
}