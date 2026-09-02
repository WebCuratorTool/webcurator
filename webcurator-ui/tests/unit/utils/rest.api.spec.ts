import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { useUserProfileStore } from "@/stores/users";
import {
  ApiRootPath,
  BasePath,
  HomePagePath,
  LoginPagePath,
  sleep,
  useAuthStore,
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
      confirmRequireMock.mockImplementation(
        (options: { accept?: () => void }) => {
          options.accept?.();
        },
      );
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

  describe("useFetch - exceptional cases", () => {
    beforeEach(() => {
      setActivePinia(createPinia());
      vi.clearAllMocks();
      confirmRequireMock.mockImplementation(
        (options: { accept?: () => void }) => {
          options.accept?.();
        },
      );
      const userProfile = useUserProfileStore();
      userProfile.setToken("testuser", "test-token-123");
    });

    it("retries and fails for reverse proxy 502", async () => {
      vi.useFakeTimers();
      global.fetch = vi.fn().mockImplementation(() =>
        Promise.resolve(
          new Response(JSON.stringify({ error: "Bad Gateway" }), {
            status: 502,
            headers: { "content-type": "application/json" },
          }),
        ),
      );

      const api = useFetch();
      const promise = api.get("/items");

      await vi.advanceTimersByTimeAsync(40000);
      const result = await promise;

      expect(fetch).toHaveBeenCalledTimes(3);
      expect(result).toBeNull();
      expect(confirmRequireMock).toHaveBeenCalledTimes(1);
      expect(toastAddMock).toHaveBeenCalledTimes(2);

      vi.useRealTimers();
    });

    it("retries and fails for reverse proxy 504", async () => {
      vi.useFakeTimers();
      global.fetch = vi.fn().mockImplementation(() =>
        Promise.resolve(
          new Response(JSON.stringify({ error: "Gateway Timeout" }), {
            status: 504,
            headers: { "content-type": "application/json" },
          }),
        ),
      );

      const api = useFetch();
      const promise = api.get("/items");

      await vi.advanceTimersByTimeAsync(40000);
      const result = await promise;

      expect(fetch).toHaveBeenCalledTimes(3);
      expect(result).toBeNull();
      expect(confirmRequireMock).toHaveBeenCalledTimes(1);
      expect(toastAddMock).toHaveBeenCalledTimes(2);

      vi.useRealTimers();
    });

    it("handles non-2xx error when response content is not empty", async () => {
      const mockResponse = new Response("Validation failed", {
        status: 400,
        statusText: "Bad Request",
        headers: { "content-type": "text/plain" },
      });
      global.fetch = vi.fn().mockResolvedValue(mockResponse);

      const api = useFetch();
      const result = await api.get("/items");

      expect(result).toBeNull();
      expect(confirmRequireMock).toHaveBeenCalledTimes(1);
      const confirmOptions = confirmRequireMock.mock.calls[0][0] as {
        message: string;
      };
      expect(confirmOptions.message).toBe("Validation failed");
    });

    it("handles non-2xx error when response content is empty", async () => {
      const mockResponse = new Response("", {
        status: 400,
        statusText: "",
        headers: { "content-type": "text/plain" },
      });
      global.fetch = vi.fn().mockResolvedValue(mockResponse);

      const api = useFetch();
      const result = await api.get("/items");

      expect(result).toBeNull();
      expect(confirmRequireMock).toHaveBeenCalledTimes(1);
      const confirmOptions = confirmRequireMock.mock.calls[0][0] as {
        message: string;
      };
      expect(confirmOptions.message).toBe("Bad Request");
    });

    it("handles non-2xx error when statusText is not empty", async () => {
      const mockResponse = new Response(null, {
        status: 400,
        statusText: "Request failed by app server",
      });
      global.fetch = vi.fn().mockResolvedValue(mockResponse);

      const api = useFetch();
      const result = await api.get("/items");

      expect(result).toBeNull();
      expect(confirmRequireMock).toHaveBeenCalledTimes(1);
      const confirmOptions = confirmRequireMock.mock.calls[0][0] as {
        message: string;
      };
      expect(confirmOptions.message).toBe("Request failed by app server");
    });

    it("handles non-2xx error when statusText is empty", async () => {
      const mockResponse = new Response(null, {
        status: 499,
        statusText: "",
      });
      global.fetch = vi.fn().mockResolvedValue(mockResponse);

      const api = useFetch();
      const result = await api.get("/items");

      expect(result).toBeNull();
      expect(confirmRequireMock).toHaveBeenCalledTimes(1);
      const confirmOptions = confirmRequireMock.mock.calls[0][0] as {
        message: string;
      };
      expect(confirmOptions.message).toBe("User request error");
    });

    it("re-authenticates when response status is 401", async () => {
      const authStore = useAuthStore();
      const startLoginSpy = vi
        .spyOn(authStore, "startLogin")
        .mockImplementation(() => {});

      global.fetch = vi
        .fn()
        .mockResolvedValueOnce(
          new Response(null, {
            status: 401,
            statusText: "Unauthorized",
          }),
        )
        .mockResolvedValueOnce(
          new Response(JSON.stringify({ id: 101, name: "Recovered" }), {
            status: 200,
            headers: { "content-type": "application/json" },
          }),
        );

      const api = useFetch();
      const result = await api.get("/items");

      expect(startLoginSpy).toHaveBeenCalledTimes(1);
      expect(fetch).toHaveBeenCalledTimes(2);
      expect(result).toEqual({ id: 101, name: "Recovered" });
      expect(confirmRequireMock).not.toHaveBeenCalled();
    });

    it("handles forbidden response status 403 then succeeds", async () => {
      global.fetch = vi
        .fn()
        .mockResolvedValueOnce(
          new Response(JSON.stringify({ error: "Forbidden action" }), {
            status: 403,
            headers: { "content-type": "application/json" },
          }),
        )
        .mockResolvedValueOnce(
          new Response(JSON.stringify({ id: 102, name: "Allowed" }), {
            status: 200,
            headers: { "content-type": "application/json" },
          }),
        );

      const api = useFetch();
      const result = await api.get("/items");

      expect(fetch).toHaveBeenCalledTimes(2);
      expect(result).toEqual({ id: 102, name: "Allowed" });
      expect(confirmRequireMock).toHaveBeenCalledTimes(1);
      const confirmOptions = confirmRequireMock.mock.calls[0][0] as {
        message: string;
      };
      expect(confirmOptions.message).toBe("Forbidden action");
    });
  });
});
