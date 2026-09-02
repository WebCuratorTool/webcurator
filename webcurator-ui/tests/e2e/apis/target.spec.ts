/* @vitest-environment node */
import { loadEnv } from "vite";
import { afterAll, describe, expect, it } from "vitest";

import {
  delete_target,
  get_target,
  get_target_list,
  post_target,
  put_target,
} from "./_apis";

describe("TargetApi CRUD", () => {
  const env = loadEnv("test", process.cwd(), "");

  const E2E_TIMEOUT = 90000;
  let createdTargetId: number | null = null;

  afterAll(async () => {
    if (!createdTargetId) {
      return;
    }

    const deleted = await delete_target(createdTargetId as number);
    expect(deleted).toBeDefined();
    expect(deleted).toBe(200);
  });

  it(
    "1. adds a new target",
    async () => {
      const targetUrl = await post_target(
        env.VITE_TEST_USERNAME,
        parseInt(env.AUTHORISATION_ID, 10),
        parseInt(env.PROFILE_ID, 10),
        parseInt(env.GROUP_ID, 10),
      );
      expect(targetUrl).toBeDefined();
      const idItem = new URL(targetUrl).pathname
        .split("/")
        .filter(Boolean)
        .pop();

      expect(idItem).toBeDefined();
      createdTargetId = parseInt(idItem as string, 10);
    },
    E2E_TIMEOUT,
  );

  it(
    "2. gets a target by id",
    async () => {
      if (!createdTargetId) {
        throw new Error("Target ID not set from previous test");
      }

      const target = await get_target(createdTargetId);
      expect(target).toBeDefined();
      expect(target.general.id).toBe(createdTargetId);
    },
    E2E_TIMEOUT,
  );

  it(
    "3. updates a target",
    async () => {
      if (!createdTargetId) {
        throw new Error("Target ID not set from previous test");
      }

      const updated = await put_target(createdTargetId);
      expect(updated).toBeDefined();
      expect(updated).toBe(200);
    },
    E2E_TIMEOUT,
  );

  it(
    "4. lists all targets",
    async () => {
      const targetsResponse = await get_target_list();
      const targets = targetsResponse.targets;
      expect(Array.isArray(targets)).toBe(true);
      expect(targets.length).toBeGreaterThanOrEqual(0);
    },
    E2E_TIMEOUT,
  );
});
