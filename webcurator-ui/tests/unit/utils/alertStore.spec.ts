import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";

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

import { useAlertStore } from "@/utils/alertStore";

describe("useAlertStore", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it("adds an info toast", () => {
    const alertStore = useAlertStore();

    alertStore.info("Saved successfully");

    expect(toastRemoveGroupMock).toHaveBeenCalledWith("toast-info");
    expect(toastAddMock).toHaveBeenCalledWith(
      expect.objectContaining({
        group: "toast-info",
        severity: "info",
        summary: "Info",
        detail: "Saved successfully",
      }),
    );
  });

  it("adds a warning toast", () => {
    const alertStore = useAlertStore();

    alertStore.warning("Something happened");

    expect(toastRemoveAllGroupsMock).toHaveBeenCalledTimes(1);
    expect(toastAddMock).toHaveBeenCalledWith(
      expect.objectContaining({
        group: "toast-error",
        severity: "warn",
        summary: "Warning",
        detail: "Something happened",
      }),
    );
  });

  it("opens a confirm dialog for errors", async () => {
    const alertStore = useAlertStore();

    const pending = alertStore.error("Something failed");
    expect(confirmRequireMock).toHaveBeenCalledTimes(1);

    const confirmOptions = confirmRequireMock.mock.calls[0][0];
    confirmOptions.accept();

    await pending;

    expect(toastRemoveAllGroupsMock).toHaveBeenCalledTimes(1);
  });
});
