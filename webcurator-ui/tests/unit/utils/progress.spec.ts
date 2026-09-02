import { createPinia, setActivePinia } from "pinia";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { useProgressStore } from "@/utils/progress";

describe("useProgressStore", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("shows progress after the delay", async () => {
    const progressStore = useProgressStore();

    progressStore.start();
    expect(progressStore.visible).toBe(false);

    await vi.advanceTimersByTimeAsync(100);

    expect(progressStore.visible).toBe(true);
  });

  it("clears visibility and timer when stopped", async () => {
    const progressStore = useProgressStore();

    progressStore.start();
    await vi.advanceTimersByTimeAsync(100);
    expect(progressStore.visible).toBe(true);

    progressStore.end();

    expect(progressStore.visible).toBe(false);
    await vi.advanceTimersByTimeAsync(1000);
    expect(progressStore.visible).toBe(false);
  });
});
