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

export type {
    TargetGeneral,
    TargetDescription
}