import type { AuthorisingAgent } from "./authorisingAgent";
import type { Permission } from "./permission";

interface HarvestAuth {
  id: number;
  name: string;
  authorisingAgents: AuthorisingAgent[];
  permissions: Permission[];
}

interface HarvestAuthDisplay {
  id: number;
  name: string;
  agent: string;
  permissionId: number;
  startDate: number;
  endDate: number;
  urlPatterns: string[];
}

interface HarvestAuthSearchResponse {
  Filter: Record<string, unknown>;
  amount: number;
  offset: number;
  limit: number;
  sortBy: string;
  harvestAuthorisations: HarvestAuth[];
}

type HarvestAuths = Array<HarvestAuth>;

export type {
  HarvestAuth,
  HarvestAuthDisplay,
  HarvestAuths,
  HarvestAuthSearchResponse,
};
