import { describe, expect, it, vi } from "vitest";

import {
  ApiRootPath,
  BasePath,
  HomePagePath,
  LoginPagePath,
  sleep,
} from "@/utils/rest.api";

describe("rest.api", () => {
  it("exports the base paths", () => {
    expect(BasePath).toBe("/wct");
    expect(HomePagePath).toBe("/");
    expect(LoginPagePath).toBe("/login");
    expect(ApiRootPath).toBe("/wct");
  });

  it("resolves sleep after the given delay", async () => {
    vi.useFakeTimers();
    const promise = sleep(250);

    await vi.advanceTimersByTimeAsync(250);
    await expect(promise).resolves.toBeUndefined();

    vi.useRealTimers();
  });
});
