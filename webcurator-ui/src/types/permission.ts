import type { AuthorisingAgent } from "./authorisingAgent";

interface PermissionAnnotation {
  date: number;
  user: string;
  note: string;
}

interface PermissionExclusion {
  url: string;
  reason: string;
}

interface Permission {
  id: number;
  startDate: number;
  endDate: number;
  status: number;
  urlPatterns: string[];
  harvestAuthorisationId: number;
  accessStatus: null | string;
  copyrightStatement: string;
  copyrightUrl: string;
  authResponse: string;
  openAccessDate: null | string;
  authorisingAgent: AuthorisingAgent;
  quickPick: boolean;
  annotations: PermissionAnnotation[];
  displayName: string;
  exclusions: {
    [key: string]: PermissionExclusion;
  }[];
}

export type { Permission, PermissionAnnotation, PermissionExclusion };
