export const NEW_TEST_PROFILE = {
  id: 0,
  name: "Default Heritrix Profile",
  description: "Baseline crawl profile for standard collections",
  profile:
    "<crawl-order><meta><name>default-profile</name></meta></crawl-order>",
  level: 1,
  state: 1,
  default: false,
  agency: "bootstrap",
  harvesterType: "HERITRIX3",
  dataLimitUnit: "GB",
  maxFileSizeUnit: "MB",
  imported: false,
};

export const NEW_TEST_PROFILE2 = {
  harvesterType: "HERITRIX3",
  imported: false,
  name: "H3Profile22",
  overrides: [
    {
      id: "documentLimit",
      value: null,
      enabled: false,
    },
    {
      id: "dataLimit",
      value: 0.0,
      enabled: false,
      unit: "B",
    },
    {
      id: "timeLimit",
      value: 5.0,
      enabled: true,
      unit: "MINUTE",
    },
    {
      id: "maxPathDepth",
      value: null,
      enabled: false,
    },
    {
      id: "maxHops",
      value: null,
      enabled: false,
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
      value: [""],
      enabled: false,
    },
    {
      id: "includedUrls",
      value: [""],
      enabled: false,
    },
  ],
};

export const OVERRIDDEN_TEST_PROFILE = {
  id: null,
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
};
