import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { useUserProfileStore } from "@/stores/users";
import {
  ApiRootPath,
  BasePath,
  HomePagePath,
  LoginPagePath,
  sleep,
  useFetch,
} from "@/utils/rest.api";

const toastRemoveGroupMock = vi.fn();
const toastRemoveAllGroupsMock = vi.fn();
const toastAddMock = vi.fn();
const confirmRequireMock = vi.fn();

vi.mock("primevue", () => ({
  useToast: () => ({
    removeGroup: toastRemoveGroupMock,
    removeAllGroups: toastRemoveAllGroupsMock,
    add: toastAddMock,
  }),
  useConfirm: () => ({
    require: confirmRequireMock,
  }),
}));

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

  describe("useFetch - HTTP requests", () => {
    beforeEach(() => {
      setActivePinia(createPinia());
      vi.clearAllMocks();
      const userProfile = useUserProfileStore();
      userProfile.setToken("testuser", "test-token-123");
    });

    it("performs a GET request", async () => {
      const mockData = { id: 1, name: "Test Item" };
      const mockResponse = new Response(JSON.stringify(mockData), {
        status: 200,
        headers: { "content-type": "application/json" },
      });

      global.fetch = vi.fn(() => Promise.resolve(mockResponse));

      const api = useFetch();
      const result = await api.get("/items");

      expect(fetch).toHaveBeenCalledWith(
        "/wct/api/v1/items",
        expect.objectContaining({
          method: "GET",
        }),
      );
      expect(result).toEqual(mockData);
    });

    it("performs a POST request with payload", async () => {
      const mockData = { id: 1, name: "Created Item" };
      const payload = { name: "New Item" };
      const mockResponse = new Response(JSON.stringify(mockData), {
        status: 200,
        headers: { "content-type": "application/json" },
      });

      global.fetch = vi.fn(() => Promise.resolve(mockResponse));

      const api = useFetch();
      const result = await api.post("/items", payload);

      expect(fetch).toHaveBeenCalledWith(
        "/wct/api/v1/items",
        expect.objectContaining({
          method: "POST",
          body: JSON.stringify(payload),
        }),
      );
      expect(result).toEqual(mockData);
    });

    it("performs a PUT request with payload", async () => {
      const mockData = { id: 1, name: "Updated Item" };
      const payload = { name: "Updated Item" };
      const mockResponse = new Response(JSON.stringify(mockData), {
        status: 200,
        headers: { "content-type": "application/json" },
      });

      global.fetch = vi.fn(() => Promise.resolve(mockResponse));

      const api = useFetch();
      const result = await api.put("/items/1", payload);

      expect(fetch).toHaveBeenCalledWith(
        "/wct/api/v1/items/1",
        expect.objectContaining({
          method: "PUT",
          body: JSON.stringify(payload),
        }),
      );
      expect(result).toEqual(mockData);
    });

    it("performs a DELETE request", async () => {
      const mockResponse = new Response(JSON.stringify({ success: true }), {
        status: 200,
        headers: { "content-type": "application/json" },
      });

      global.fetch = vi.fn(() => Promise.resolve(mockResponse));

      const api = useFetch();
      const result = await api.delete("/items/1");

      expect(fetch).toHaveBeenCalledWith(
        "/wct/api/v1/items/1",
        expect.objectContaining({
          method: "DELETE",
        }),
      );
      expect(result).toEqual({ success: true });
    });

    it("performs a PATCH request with payload", async () => {
      const mockData = { id: 1, status: "patched" };
      const payload = { status: "patched" };
      const mockResponse = new Response(JSON.stringify(mockData), {
        status: 200,
        headers: { "content-type": "application/json" },
      });

      global.fetch = vi.fn(() => Promise.resolve(mockResponse));

      const api = useFetch();
      const result = await api.patch("/items/1", payload);

      expect(fetch).toHaveBeenCalledWith(
        "/wct/api/v1/items/1",
        expect.objectContaining({
          method: "PATCH",
          body: JSON.stringify(payload),
        }),
      );
      expect(result).toEqual(mockData);
    });

    it("includes Authorization header in requests", async () => {
      const mockResponse = new Response(JSON.stringify({ ok: true }), {
        status: 200,
        headers: { "content-type": "application/json" },
      });

      global.fetch = vi.fn(() => Promise.resolve(mockResponse));

      const api = useFetch();
      await api.get("/items");

      const call = vi.mocked(fetch).mock.calls[0];
      const options = call[1] as { headers: Headers };
      const headers = options.headers as Headers;

      expect(headers.get("Authorization")).toBe("test-token-123");
      expect(headers.get("Content-Type")).toBe("application/json");
    });

    it("handles JSON response", async () => {
      const mockData = { id: 1, data: "test" };
      const mockResponse = new Response(JSON.stringify(mockData), {
        status: 200,
        headers: { "content-type": "application/json" },
      });

      global.fetch = vi.fn(() => Promise.resolve(mockResponse));

      const api = useFetch();
      const result = await api.get("/items");

      expect(result).toEqual(mockData);
    });

    it("handles empty response with no content", async () => {
      const mockResponse = new Response(null, {
        status: 204,
        headers: {},
      });

      global.fetch = vi.fn(() => Promise.resolve(mockResponse));

      const api = useFetch();
      const result = await api.delete("/items/1");

      expect(result).toBe(204);
    });
  });
});
