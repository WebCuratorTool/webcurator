import type { Annotation } from "@/types/annotation";
import type { HarvestAuthDisplays } from "@/types/harvestAuth";

interface Target {
  access: TargetAccess;
  annotations: TargetAnnotations;
  description: TargetDescription;
  general: TargetGeneral;
  groups: TargetGroups;
  profile: TargetProfile;
  schedule: TargetSchedule;
  seeds: TargetSeeds;
}

interface TargetAccess {
  displayTarget: boolean;
  accessZone: number;
  accessZoneText: string;
  displayChangeReason: string;
  displayNote: string;
}

interface TargetAnnotations {
  evaluationNote: string;
  harvestType: string;
  annotations: Array<Annotation>;
  alert: boolean;
  selection: {
    date: number;
    type: string;
    note: string;
  };
}

interface TargetDescription {
  identifier: string;
  description: string;
  subject: string;
  creator: string;
  publisher: string;
  type: string;
  format: string;
  language: string;
  source: string;
  relation: string;
  contributor: string;
  coverage: string;
  issn: string;
  isbn: string;
}

interface TargetGeneral {
  id: number;
  name: string;
  creationDate: number;
  description: string;
  owner: string;
  referenceNumber: string;
  runOnApproval: boolean;
  automatedQA: boolean;
  selectedUser: string;
  selectedState: { name: string; code: number };
  state: number;
  autoPrune: boolean;
  referenceCrawl: boolean;
  requestToArchivists: string;
  nextStates: [];
}

interface TargetGroup {
  id: number;
  name: string;
  agency?: string;
  state?: number;
  type?: string;
}

interface TargetProfileOverride {
  id: string;
  value: string | number | boolean | string[];
  enabled: boolean;
  unit?: string;
}

interface TargetProfile {
  id: number | null;
  harvesterType: string;
  imported: boolean;
  name: string;
  overrides: Array<TargetProfileOverride>;
}

type TargetGroups = Array<TargetGroup>;

interface TargetSeed {
  id?: number;
  seed: string;
  primary: boolean;
  authorisations: HarvestAuthDisplays;
}

interface TargetSchedule {
  harvestNow: boolean;
  harvsestOptimization: boolean;
  schedules: Array<TargetHarvest>;
}

interface TargetHarvest {
  id?: number;
  cron: string;
  startDate: number | Date;
  endDate: number | null;
  type: number;
  nextExecutionDate: number | Date | null;
  lastProcessedDate?: number | null;
  owner: string;
}

type TargetSeeds = Array<TargetSeed>;

interface NewTarget {
  description: TargetDescription;
  general: TargetGeneral;
  groups: TargetGroups;
  profile?: TargetProfile;
  schedule?: TargetSchedule;
}

export type {
  NewTarget,
  Target,
  TargetAccess,
  TargetAnnotations,
  TargetDescription,
  TargetGeneral,
  TargetGroup,
  TargetGroups,
  TargetHarvest,
  TargetProfile,
  TargetProfileOverride,
  TargetSchedule,
  TargetSeed,
  TargetSeeds,
};
