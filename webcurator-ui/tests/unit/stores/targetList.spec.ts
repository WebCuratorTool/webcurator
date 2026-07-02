import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";

import type { Target } from "@/types/target";

const mockPost = vi.fn();

vi.mock("@/utils/rest.api", () => ({
  useFetch: () => ({
    post: mockPost,
  }),
}));

vi.mock("@/stores/users", () => ({
  useUserProfileStore: () => ({
    currUserName: "Test User (tester)",
    name: "tester",
    agency: "Test Agency",
  }),
}));

import {
  targetListPageState,
  useTargetListDataStore,
} from "@/stores/targetList";

const flushPromises = async () => {
  await Promise.resolve();
  await Promise.resolve();
};

const deferred = <T>() => {
  // eslint-disable-next-line no-unused-vars
  let resolve!: (arg: T) => void;
  const promise = new Promise<T>((r) => {
    resolve = r;
  });

  return { promise, resolve };
};

describe("useTargetListDataStore", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockPost.mockReset();
    targetListPageState.first = 0;
    targetListPageState.rows = 10;
    targetListPageState.totalRecords = 1;
  });

  it("executes an initial search and populates page state", async () => {
    mockPost.mockResolvedValueOnce({
      amount: 2,
      offset: 0,
      limit: 10,
      targets: [{} as Target, {} as Target],
    });

    const store = useTargetListDataStore();
    await vi.waitFor(() => {
      expect(store.pageState.totalRecords).toBe(2);
    });

    expect(mockPost).toHaveBeenCalledTimes(1);
    expect(mockPost).toHaveBeenCalledWith(
      "targets",
      {
        filter: {
          targetId: null,
          name: "",
          seed: "",
          description: "",
          groupName: "",
          nonDisplayOnly: false,
          agency: undefined,
          userId: undefined,
          states: [],
        },
        offset: 0,
        limit: 10,
        sortBy: "creationDate,asc",
      },
      {
        header: "X-HTTP-Method-Override",
        value: "GET",
      },
    );
    expect(store.targetList).toHaveLength(2);
    expect(store.pageState.first).toBe(0);
    expect(store.pageState.rows).toBe(10);
    expect(store.loadingTargetList).toBe(false);
  });

  it("resetFilter resets terms to user defaults and triggers search", async () => {
    mockPost
      .mockResolvedValueOnce({
        amount: 0,
        offset: 0,
        limit: 10,
        targets: [],
      })
      .mockResolvedValueOnce({
        amount: 0,
        offset: 0,
        limit: 10,
        targets: [],
      });

    const store = useTargetListDataStore();
    await flushPromises();

    store.searchTerms.targetId = 100;
    store.searchTerms.targetName = "example";
    store.searchTerms.targetSeed = "https://example.org";
    store.searchTerms.targetDescription = "desc";
    store.searchTerms.targetMemberOf = "group";
    store.searchTerms.nonDisplayOnly = true;
    store.searchTerms.targetAgency = { name: "Other", code: "Other" };
    store.searchTerms.targetUser = { name: "Other User", code: "other" };
    store.searchTerms.targetState = [{ name: "Running", code: 2 }];

    store.resetFilter();
    await flushPromises();

    expect(store.searchTerms.targetId).toBeNull();
    expect(store.searchTerms.targetName).toBe("");
    expect(store.searchTerms.targetSeed).toBe("");
    expect(store.searchTerms.targetDescription).toBe("");
    expect(store.searchTerms.targetMemberOf).toBe("");
    expect(store.searchTerms.nonDisplayOnly).toBe(false);
    expect(store.searchTerms.targetState).toEqual([]);
    expect(store.searchTerms.targetUser).toEqual({
      name: "Test User (tester)",
      code: "tester",
    });
    expect(store.searchTerms.targetAgency).toEqual({
      name: "Test Agency",
      code: "Test Agency",
    });

    const lastCallPayload = mockPost.mock.calls.at(-1)?.[1];
    expect(lastCallPayload).toMatchObject({
      filter: {
        agency: "Test Agency",
        userId: "tester",
      },
      offset: 0,
      limit: 10,
      sortBy: "creationDate,asc",
    });
  });

  it("ignores stale search responses and keeps latest page data", async () => {
    const older = deferred<{
      amount: number;
      offset: number;
      limit: number;
      targets: Array<Target>;
    }>();
    const newer = deferred<{
      amount: number;
      offset: number;
      limit: number;
      targets: Array<Target>;
    }>();

    mockPost
      .mockResolvedValueOnce({
        amount: 1,
        offset: 0,
        limit: 10,
        targets: [{} as Target],
      })
      .mockImplementationOnce(() => older.promise)
      .mockImplementationOnce(() => newer.promise);

    const store = useTargetListDataStore();
    await flushPromises();

    store.updatePage(0, 10);
    store.updatePage(20, 20);
    expect(store.loadingTargetList).toBe(true);

    newer.resolve({
      amount: 99,
      offset: 20,
      limit: 20,
      targets: [{} as Target, {} as Target],
    });
    await vi.waitFor(() => {
      expect(store.pageState.totalRecords).toBe(99);
    });

    expect(store.pageState.totalRecords).toBe(99);
    expect(store.pageState.first).toBe(20);
    expect(store.pageState.rows).toBe(20);
    expect(store.targetList).toHaveLength(2);
    expect(store.loadingTargetList).toBe(false);

    older.resolve({
      amount: 5,
      offset: 0,
      limit: 10,
      targets: [{} as Target],
    });
    await flushPromises();

    expect(store.pageState.totalRecords).toBe(99);
    expect(store.pageState.first).toBe(20);
    expect(store.pageState.rows).toBe(20);
    expect(store.targetList).toHaveLength(2);
  });
});
