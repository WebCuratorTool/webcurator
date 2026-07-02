import { describe, expect, it, vi } from "vitest";

const cronMocks = vi.hoisted(() => ({
  nextRunsMock: vi
    .fn()
    .mockReturnValue([
      new Date("2026-05-01T00:00:00.000Z"),
      new Date("2026-05-08T00:00:00.000Z"),
    ]),
  cronConstructorMock: vi.fn().mockImplementation((expression: string) => ({
    expression,
    nextRuns: cronMocks.nextRunsMock,
  })),
}));

vi.mock("croner", () => ({
  Cron: cronMocks.cronConstructorMock,
}));

import {
  createCronExpression,
  createCustomCronExpression,
  dates,
  days,
  getAlternatingMonths,
  getCronMonths,
  getMonthGroups,
  getNextScheduledTimes,
  months,
  parseCron,
  quartzToUnix,
} from "@/utils/cronParser";

describe("cronParser", () => {
  it("exports days, dates, and months lists", () => {
    expect(days[0]).toBe("Monday");
    expect(dates[0]).toBe("1");
    expect(months[11]).toBe("December");
  });

  it("parses a quartz cron expression", () => {
    expect(parseCron("30 8 ? * MON-FRI *")).toEqual({
      minute: "30",
      hour: "8",
      dayOfMonth: "?",
      month: "*",
      dayOfWeek: "MON-FRI",
      year: "*",
    });
  });

  it("creates a custom cron expression", () => {
    expect(
      createCustomCronExpression({
        minute: "15",
        hour: "10",
        dayOfMonth: "1",
        month: "5",
        dayOfWeek: "MON",
        year: "2026",
      }),
    ).toBe("15 10 1 5 MON 2026");
  });

  it("creates a cron expression from monthly and weekly selections", () => {
    expect(
      createCronExpression({
        time: "09:30",
        months: "January, April, July, October",
        dayOfMonth: "",
        dayOfWeek: "Monday",
      }),
    ).toBe("30 09 ? 1/3 MON *");
  });

  it("creates a cron expression with last day of month", () => {
    expect(
      createCronExpression({
        time: "13:05",
        months: "",
        dayOfMonth: "Last",
        dayOfWeek: "",
      }),
    ).toBe("05 13 L * ? *");
  });

  it("derives cron months from a pattern", () => {
    expect(getCronMonths("0 0 1 1/3 ? *")).toBe(
      "January, April, July, October",
    );
    expect(getCronMonths("0 0 1 6 ? *")).toBe("June");
  });

  it("converts quartz to unix format", () => {
    expect(quartzToUnix("30 8 ? * MON-FRI *")).toBe("30 8 * * MON-FRI");
  });

  it("returns month groups by schedule type", () => {
    expect(getMonthGroups("bimonthly")).toEqual(getAlternatingMonths());
    expect(getMonthGroups("quarterly")).toEqual([
      "January, April, July, October",
      "February, May, August, November",
      "March, June, September, December",
    ]);
    expect(getMonthGroups("half-yearly")).toEqual([
      "January, July",
      "February, August",
      "March, September",
      "April, October",
      "May, November",
      "June, December",
    ]);
    expect(getMonthGroups("annually")).toEqual(months);
  });

  it("uses croner to calculate next scheduled times", () => {
    const nextTimes = getNextScheduledTimes(
      "0 0 ? * MON *",
      2,
      new Date("2026-05-01T00:00:00.000Z"),
    );

    expect(cronMocks.cronConstructorMock).toHaveBeenCalledWith("0 0 * * MON");
    expect(cronMocks.nextRunsMock).toHaveBeenCalledWith(
      2,
      new Date("2026-05-01T00:00:00.000Z"),
    );
    expect(nextTimes).toHaveLength(2);
    expect(nextTimes[0]).toEqual(new Date("2026-05-01T00:00:00.000Z"));
  });
});
