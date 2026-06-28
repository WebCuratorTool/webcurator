import { loadEnv } from "vite";

export default async function globalSetup() {
  const env = loadEnv("test", process.cwd(), "");

  const username = env.VITE_TEST_USERNAME || process.env.VITE_TEST_USERNAME;
  const password = env.VITE_TEST_PASSWORD || process.env.VITE_TEST_PASSWORD;
  const apiRootPath = env.VITE_API_ROOT_PATH || process.env.VITE_API_ROOT_PATH;

  if (!username || !password || !apiRootPath) {
    throw new Error(
      "Missing test auth env vars. Set VITE_TEST_USERNAME, VITE_TEST_PASSWORD, and VITE_API_ROOT_PATH.",
    );
  }

  const credentials = new URLSearchParams({ username, password });
  const response = await fetch(`${apiRootPath}/auth/v1/token`, {
    method: "POST",
    redirect: "error",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: credentials.toString(),
  });

  if (!response.ok) {
    const errorBody = await response.text();
    throw new Error(
      `Test login failed (${response.status} ${response.statusText}): ${errorBody}`,
    );
  }

  const token = (await response.text()).trim();

  if (!token) {
    throw new Error("Test login returned an empty token.");
  }

  // Make token available to all tests after a single startup login.
  process.env.VITE_TEST_TOKEN = token;
}
