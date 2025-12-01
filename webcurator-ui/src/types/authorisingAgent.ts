import type { Permission } from "./permission";

interface AuthorisingAgent {
  id: number;
  name: string;
  permissions: Permission[];
}

export type { AuthorisingAgent };
