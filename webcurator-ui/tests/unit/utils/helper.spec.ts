import { afterEach, describe, expect, it, vi } from "vitest";

import {
  camelCaseToTitleCase,
  formatDate,
  formatDatetime,
  formatTime,
} from "@/utils/helper";

afterEach(() => {
  vi.restoreAllMocks();
});

describe("formatDate", () => {
  it("formats using 2-digit day/month and numeric year", () => {
    const toLocaleDateStringSpy = vi
      .spyOn(Date.prototype, "toLocaleDateString")
      .mockReturnValue("01/05/2026");

    const result = formatDate(1714540800000);

    expect(result).toBe("01/05/2026");
    expect(toLocaleDateStringSpy).toHaveBeenCalledWith(undefined, {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  });

  it("accepts string timestamps", () => {
    const toLocaleDateStringSpy = vi
      .spyOn(Date.prototype, "toLocaleDateString")
      .mockReturnValue("31/12/2026");

    const result = formatDate("2026-12-31T00:00:00.000Z");

    expect(result).toBe("31/12/2026");
    expect(toLocaleDateStringSpy).toHaveBeenCalledTimes(1);
  });
});

describe("formatTime", () => {
  it("formats using short 24-hour time", () => {
    const toLocaleTimeStringSpy = vi
      .spyOn(Date.prototype, "toLocaleTimeString")
      .mockReturnValue("14:30");

    const result = formatTime("2026-05-01T14:30:00.000Z");

    expect(result).toBe("14:30");
    expect(toLocaleTimeStringSpy).toHaveBeenCalledWith([], {
      hour12: false,
      timeStyle: "short",
    });
  });

  it("accepts Date instances", () => {
    const toLocaleTimeStringSpy = vi
      .spyOn(Date.prototype, "toLocaleTimeString")
      .mockReturnValue("09:15");

    const result = formatTime(new Date("2026-05-01T09:15:00.000Z"));

    expect(result).toBe("09:15");
    expect(toLocaleTimeStringSpy).toHaveBeenCalledTimes(1);
  });
});

describe("formatDatetime", () => {
  it("returns date and time using locale string", () => {
    const toLocaleStringSpy = vi
      .spyOn(Date.prototype, "toLocaleString")
      .mockReturnValue("5/1/2026, 2:30:00 PM");

    const result = formatDatetime(1714573800000);

    expect(result).toBe("5/1/2026, 2:30:00 PM");
    expect(toLocaleStringSpy).toHaveBeenCalledWith();
  });
});

describe("camelCaseToTitleCase", () => {
  it("converts camelCase to title case", () => {
    expect(camelCaseToTitleCase("targetInstanceList")).toBe(
      "Target Instance List",
    );
  });

  it("capitalizes a single lowercase word", () => {
    expect(camelCaseToTitleCase("dashboard")).toBe("Dashboard");
  });

  it("keeps consecutive uppercase letters split with spaces", () => {
    expect(camelCaseToTitleCase("myURLValue")).toBe("My U R L Value");
  });
});
