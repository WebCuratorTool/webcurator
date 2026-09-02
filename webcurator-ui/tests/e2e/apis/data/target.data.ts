export const NEW_TEST_TARGET = {
  general: {
    owner: "",
    runOnApproval: false,
    referenceNumber: "",
    requestToArchivists: "",
    name: "Test Target",
    description: "",
    referenceCrawl: false,
    state: 6,
  },
  schedule: {
    harvestOptimization: false,
    schedules: [
      {
        cron: "00 21 ? * MON *",
        startDate: "2023-09-17T13:36:36.707+00:00",
        type: 1,
        nextExecutionDate: "2023-09-17T13:36:36.707+00:00",
        owner: "",
      },
    ],
  },
  access: {
    displayTarget: false,
    accessZone: 0,
    displayChangeReason: "None given",
    displayNote: null,
    accessZoneText: "Public",
  },
  seeds: [
    {
      seed: "https://www.dbnl.org/",
      authorisations: [{ id: 0 }],
      primary: "true",
    },
  ],
  profile: {
    id: 0,
    overrides: [
      {
        id: "documentLimit",
        value: null,
        enabled: false,
      },
      {
        unit: "GB",
        id: "dataLimit",
        value: null,
        enabled: false,
      },
      {
        unit: "seconds",
        id: "timeLimit",
        value: null,
        enabled: false,
      },
      {
        id: "maxPathDepth",
        value: null,
        enabled: false,
      },
      {
        id: "maxHops",
        value: 5,
        enabled: true,
      },
      {
        id: "maxTransitiveHops",
        value: null,
        enabled: false,
      },
      {
        id: "ignoreRobots",
        value: false,
        enabled: false,
      },
      {
        id: "extractJs",
        value: false,
        enabled: false,
      },
      {
        id: "ignoreCookies",
        value: false,
        enabled: false,
      },
      {
        id: "blockedUrls",
        value: [],
        enabled: false,
      },
      {
        id: "includedUrls",
        value: [],
        enabled: false,
      },
    ],
    harvesterType: "HERITRIX3",
  },
  annotations: {
    selection: {
      date: "2023-05-17T13:37:13.919+00:00",
      note: null,
      type: "Producer type",
    },
    annotations: [
      {
        date: "2023-05-17T13:37:13.919+00:00",
        note: "Test annotation",
        user: "",
        alert: true,
      },
    ],
    evaluationNote: null,
    harvestType: "Subject",
  },
  description: {
    coverage: null,
    identifier: null,
    creator: null,
    subject: null,
    isbn: null,
    format: null,
    description: null,
    language: "Dutch",
    source: null,
    type: "",
    relation: null,
    contributor: null,
    issn: null,
    publisher: null,
  },
  groups: [{ id: 0 }],
};

export const UPDATED_TEST_TARGET = {
  general: {
    description: "Updated description from API test",
  },
  profile: {
    overrides: [
      {
        id: "dataLimit",
        unit: "GB",
        value: 1,
        enabled: false,
      },
    ],
  },
};
