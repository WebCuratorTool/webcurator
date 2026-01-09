import type { AuthorisingAgent } from "./authorisingAgent";
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
    annotations: any[]; 
    displayName: string;
    exclusions: {
      [key: string]: any;
    }[];
  }

  export type { Permission }