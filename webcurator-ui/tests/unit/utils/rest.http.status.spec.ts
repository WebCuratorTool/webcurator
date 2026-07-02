import { describe, expect, it } from "vitest";

import { HttpStatus } from "@/utils/rest.http.status";

describe("HttpStatus", () => {
  it("maps common success statuses", () => {
    expect(HttpStatus[200]).toBe("OK");
    expect(HttpStatus[201]).toBe("Created");
    expect(HttpStatus[204]).toBe("No Content");
  });

  it("maps common client and server errors", () => {
    expect(HttpStatus[400]).toBe("Bad Request");
    expect(HttpStatus[401]).toBe("Unauthorized");
    expect(HttpStatus[500]).toBe("Internal Server Error");
  });

  it("includes non-standard web server status codes", () => {
    expect(HttpStatus[511]).toBe("Network Authentication Required");
    expect(HttpStatus[508]).toBe("Loop Detected");
  });
});
