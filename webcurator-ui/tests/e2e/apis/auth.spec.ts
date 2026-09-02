import axios from "axios";
import { describe, expect, it } from "vitest";

import { apiClient } from "../../setup/apiClient";

describe("Authentication", () => {
  it("has startup token from global setup", () => {
    const token = process.env.VITE_TEST_TOKEN;

    expect(token).toBeTruthy();
    expect(token?.length).toBeGreaterThan(0);
  });

  it("configures global axios auth defaults", () => {
    const token = process.env.VITE_TEST_TOKEN;
    const apiRootPath = process.env.VITE_API_ROOT_PATH || "/wct";

    expect(axios.defaults.baseURL).toBe(`${apiRootPath}/api/v1`);
    expect(axios.defaults.headers.common.Authorization).toBe(token);
  });

  it("configures api client with authenticated base URL", () => {
    const apiRootPath = process.env.VITE_API_ROOT_PATH || "/wct";

    expect(apiClient.defaults.baseURL).toBe(`${apiRootPath}/api/v1`);
  });
});
