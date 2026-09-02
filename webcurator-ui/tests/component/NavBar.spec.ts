import { mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";

const logoutMock = vi.fn();
const toggleThemeModeMock = vi.fn();

vi.mock("@/utils/rest.api", () => ({
  useAuthStore: () => ({
    logout: logoutMock,
  }),
}));

vi.mock("@/layout/composables/layout", () => ({
  useLayout: () => ({
    layoutConfig: {
      darkTheme: {
        value: false,
      },
    },
  }),
}));

vi.mock("@/utils/themes", () => ({
  toggleThemeMode: (mode: "dark" | "light") => toggleThemeModeMock(mode),
}));

import NavBar from "@/components/NavBar.vue";

const mountNavBar = () => {
  return mount(NavBar, {
    global: {
      stubs: {
        "router-link": {
          props: ["to"],
          template: '<a :data-to="to"><slot /></a>',
        },
        Button: {
          props: ["label"],
          emits: ["click"],
          template: "<button @click=\"$emit('click')\">{{ label }}</button>",
        },
        ToggleSwitch: {
          props: ["modelValue"],
          emits: ["update:modelValue"],
          template:
            '<input type="checkbox" :checked="modelValue" @change="$emit(\'update:modelValue\', $event.target.checked)" />',
        },
      },
    },
  });
};

describe("NavBar", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders navigation links and logout button", () => {
    const wrapper = mountNavBar();

    expect(wrapper.text()).toContain("Dashboard");
    expect(wrapper.text()).toContain("Targets");
    expect(wrapper.text()).toContain("Logout");
    expect(wrapper.find("img.logo").exists()).toBe(true);
  });

  it("calls logout when logout button is clicked", async () => {
    const wrapper = mountNavBar();

    await wrapper.find("button").trigger("click");

    expect(logoutMock).toHaveBeenCalledTimes(1);
  });

  it("switches to dark mode when toggle is enabled", async () => {
    const wrapper = mountNavBar();

    await wrapper.find('input[type="checkbox"]').setValue(true);

    expect(toggleThemeModeMock).toHaveBeenCalledWith("dark");
  });

  it("switches to light mode when toggle is disabled", async () => {
    const wrapper = mountNavBar();
    const toggle = wrapper.find('input[type="checkbox"]');

    await toggle.setValue(true);
    await toggle.setValue(false);

    expect(toggleThemeModeMock).toHaveBeenCalledWith("light");
  });
});
