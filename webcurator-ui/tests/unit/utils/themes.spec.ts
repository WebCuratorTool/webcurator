import { afterEach, describe, expect, it, vi } from "vitest";

const themeMocks = vi.hoisted(() => ({
  updatePresetMock: vi.fn(),
  updateSurfacePaletteMock: vi.fn(),
  toggleDarkModeMock: vi.fn(),
  layoutConfigMock: {
    preset: "Aura",
    primary: "emerald",
    surface: null as string | null,
    darkTheme: false,
  },
  isDarkThemeMock: { value: false },
}));

vi.mock("@primeuix/themes", () => ({
  updatePreset: themeMocks.updatePresetMock,
  updateSurfacePalette: themeMocks.updateSurfacePaletteMock,
}));

vi.mock("@/layout/composables/layout", () => ({
  useLayout: () => ({
    layoutConfig: themeMocks.layoutConfigMock,
    isDarkTheme: themeMocks.isDarkThemeMock,
    toggleDarkMode: themeMocks.toggleDarkModeMock,
  }),
}));

import { togglePreset, toggleSurface, toggleThemeMode } from "@/utils/themes";

afterEach(() => {
  vi.clearAllMocks();
  themeMocks.layoutConfigMock.primary = "emerald";
  themeMocks.layoutConfigMock.surface = null;
  themeMocks.layoutConfigMock.darkTheme = false;
  themeMocks.isDarkThemeMock.value = false;
});

describe("themes", () => {
  it("updates the primary preset", () => {
    togglePreset("blue");

    expect(themeMocks.layoutConfigMock.primary).toBe("blue");
    expect(themeMocks.updatePresetMock).toHaveBeenCalledTimes(1);
  });

  it("updates the surface palette", () => {
    toggleSurface("sky");

    expect(themeMocks.layoutConfigMock.surface).toBe("sky");
    expect(themeMocks.updateSurfacePaletteMock).toHaveBeenCalledTimes(1);
  });

  it("turns dark mode on and off based on requested theme mode", () => {
    toggleThemeMode("dark");
    expect(themeMocks.toggleDarkModeMock).toHaveBeenCalledTimes(1);

    themeMocks.isDarkThemeMock.value = true;
    toggleThemeMode("light");
    expect(themeMocks.toggleDarkModeMock).toHaveBeenCalledTimes(2);
  });
});
