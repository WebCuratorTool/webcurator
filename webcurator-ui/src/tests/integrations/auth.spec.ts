import { describe, expect, it } from "vitest";

describe("Authentication", () => {
  it("has startup token from global setup", () => {
    const token = process.env.VITE_TEST_TOKEN;

    expect(token).toBeTruthy();
    expect(token?.length).toBeGreaterThan(0);
  });
});
