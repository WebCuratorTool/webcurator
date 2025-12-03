import { defineStore } from "pinia";
import { ref } from "vue";

import router from "@/router";
import { useUserProfileStore } from "@/stores/users";
import type { CustomHeader } from "@/types/customHeader";

import { useAlertStore } from "./alertStore";
import { HttpStatus } from "./rest.http.status";

export const BasePath = "/wct";
export const HomePagePath = "/";
export const LoginPagePath = "/login";
export const ApiRootPath = "/wct";
const ApiContextPath = ApiRootPath + "/api/v1";
const RetryDelay = 20 * 1000;
const MaxRetryTimes = 3;

interface LoginResponse {
  ok: boolean;
  title: string;
  detail: string;
}

interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  path: string;
}

export type { ApiErrorResponse, LoginResponse };

const _login = async (username: string, password: string) => {
  const credentials = "username=" + username + "&password=" + password;
  const rsp = await fetch(ApiRootPath + "/auth/v1/token", {
    method: "POST",
    redirect: "error",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: credentials,
  });

  const feedback = {
    ok: true,
    title: "",
    detail: "",
  } as LoginResponse;

  if (!rsp.ok) {
    const status = rsp.status;
    let statusText = rsp.statusText;
    if (!statusText || statusText.length === 0) {
      if (status === 401) {
        statusText = "Unknown username or password, please try again.";
      } else {
        statusText = "Unknown error.";
      }
    }
    feedback.ok = false;
    feedback.title = "Error: " + status;
    feedback.detail = statusText;
  } else {
    const token = await rsp.text();
    const userProfile = useUserProfileStore();
    userProfile.setToken(username, token);
  }

  return feedback;
};

type HttpMethod =
  | "GET"
  | "POST"
  | "PUT"
  | "DELETE"
  | "PATCH"
  | "HEAD"
  | "OPTIONS";

export const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));
export const useAuthStore = defineStore("AuthStore", () => {
  const isAuthenticating = ref(false);

  const userProfile = useUserProfileStore();
  const redirectPath = ref();
  const setRedirectPath = (path: string) => {
    redirectPath.value = path;
  };

  const isAuthenticated = async () => {
    userProfile.load();
    const token = userProfile.token;
    if (!token) {
      return false;
    }
    const rsp = await fetch(ApiRootPath + "/auth/v1/token/" + token);
    return rsp.ok;
  };

  const authenticate = async (
    routePath: string,
    username: string,
    password: string,
  ) => {
    const feedback = await _login(username, password);
    if (!feedback.ok) {
      return feedback;
    }

    /**
     * If it's opened from the independent page, then go to the previous page after the authentication.
     * Otherwise, close the login dialog and continue the process of the related rest api.
     * */
    if (routePath === LoginPagePath) {
      if (redirectPath.value) {
        router.push(redirectPath.value);
      } else {
        router.push(HomePagePath);
      }
    } else {
      isAuthenticating.value = false;
    }

    return feedback;
  };

  const startLogin = () => {
    isAuthenticating.value = true;
  };

  const logout = async () => {
    userProfile.load();
    const token = userProfile.token;
    userProfile.clear();
    if (token) {
      await fetch(ApiRootPath + "/auth/v1/token/" + token, {
        method: "DELETE",
        redirect: "error",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
      });
    }
    setRedirectPath(HomePagePath);
    router.push(LoginPagePath);
  };

  return {
    startLogin,
    authenticate,
    isAuthenticating,
    isAuthenticated,
    logout,
    setRedirectPath,
  };
});

/* eslint-disable no-unused-vars */
export interface UseFetchApis {
  get<T = unknown>(path: string): Promise<T>;
  post<T = unknown, P = unknown>(
    path: string,
    payload: P,
    customHeader?: CustomHeader,
  ): Promise<T>;
  put<T = unknown, P = unknown>(path: string, payload: P): Promise<T>;
  delete<T = unknown, P = unknown>(path: string, payload?: P): Promise<T>;
  patch<T = unknown, P = unknown>(path: string, payload: P): Promise<T>;
  head<T = unknown>(path: string): Promise<T>;
  options<T = unknown, P = unknown>(path: string, payload?: P): Promise<T>;
}
/* eslint-enable no-unused-vars */

// by convention, composable function names start with "use"
export function useFetch() {
  // state encapsulated and managed by the composable
  const confirm = useAlertStore();

  const shell: UseFetchApis = {
    // method
    get: setMethod("GET"),
    put: setMethod("PUT"),
    post: setMethod("POST"),
    delete: setMethod("DELETE"),
    patch: setMethod("PATCH"),
    head: setMethod("HEAD"),
    options: setMethod("OPTIONS"),
  };

  function setMethod(methodValue: HttpMethod) {
    return async (
      path: string,
      payload: unknown = null,
      customHeader: CustomHeader | null = null,
    ) => {
      // await sleep(1000);

      const userProfile = useUserProfileStore();
      const loginStore = useAuthStore();

      let ret = null;

      const isFinished = ref(false);
      const retriedTimes = ref(0);

      //Retry until it's finished. If the login session is expired, it can be run 2 rounds
      while (!isFinished.value) {
        // Waiting until the authentication is finished
        while (loginStore.isAuthenticating) {
          await sleep(1000);
        }

        userProfile.load(); //Update the info from local storage

        const requestHeaders = new Headers();
        requestHeaders.set("Content-Type", "application/json");
        requestHeaders.set("Authorization", userProfile.token);

        if (customHeader) {
          requestHeaders.set(customHeader.header, customHeader.value);
        }

        const reqOptions = {
          method: methodValue,
          redirect: "error" as const,
          headers: requestHeaders,
          body: null as string | null,
        };
        if (payload !== null && payload !== undefined) {
          reqOptions.body = JSON.stringify(payload);
        }

        let reqPath;
        if (path.startsWith("/")) {
          reqPath = ApiContextPath + path;
        } else {
          reqPath = ApiContextPath + "/" + path;
        }

        let rsp;
        try {
          rsp = await fetch(reqPath, reqOptions);
        } catch (err: unknown) {
          let rspError = "Unknown error";
          if (err instanceof Error) {
            rspError = err.message;
          }

          retriedTimes.value++;
          if (retriedTimes.value >= MaxRetryTimes) {
            const errMsg = `${rspError}`;
            await confirm.error(
              errMsg,
              `Failed to [${methodValue}] ${reqPath}: ${errMsg}`,
            );
            break;
          } else {
            const errMsg = `${rspError}. Will retry in ${RetryDelay / 1000} seconds.`;
            confirm.warning(
              errMsg,
              `Failed to [${methodValue}] ${reqPath}: ${errMsg}`,
            );
            await sleep(RetryDelay);
            continue;
          }
        }

        if (!rsp) {
          //Exception has happened
          const errMsg = "Unknown exception happened.";
          await confirm.error(errMsg, `Failed to [${methodValue}] ${reqPath}`);
          break;
        } else if (rsp.status === 502 || rsp.status === 504) {
          //Upstream error
          const errMsg = await extractErrorMessageFromResponse(rsp);
          retriedTimes.value++;
          if (retriedTimes.value >= MaxRetryTimes) {
            await confirm.error(
              errMsg,
              `Failed to [${methodValue}] ${reqPath}: ${errMsg}`,
            );
            break;
          } else {
            const errForRetry = `${errMsg}. Will retry in ${RetryDelay / 1000} seconds.`;
            confirm.warning(
              errForRetry,
              `Failed to [${methodValue}] ${reqPath}: ${errForRetry}`,
            );
            await sleep(RetryDelay);
            continue;
          }
        } else if (rsp.status === 401) {
          loginStore.startLogin();
          continue;
        } else if (rsp.status === 403) {
          const errMsg = await extractErrorMessageFromResponse(rsp);
          await confirm.error(
            errMsg,
            `User does not have role to [${methodValue}] ${reqPath}: ${errMsg}`,
          );
          continue;
        } else if (!rsp.ok) {
          const errMsg = await extractErrorMessageFromResponse(rsp);
          await confirm.error(
            errMsg,
            `Failed to [${methodValue}] ${reqPath}: ${errMsg}`,
          );
          break;
        }

        const contentType = rsp.headers.get("content-type") || "";
        const contentLength = parseInt(
          rsp.headers.get("content-length") || "-1",
        );

        if (!contentType && contentLength <= 0) {
          ret = rsp.status;
        } else if (contentType.startsWith("application/json")) {
          ret = await rsp.json();
        } else if (
          contentType.startsWith("application") ||
          contentType.startsWith("image") ||
          contentType.startsWith("video") ||
          contentType.startsWith("audio")
        ) {
          ret = await rsp.blob();
        } else {
          ret = await rsp.text();
        }
        confirm.trace(`Succeed to [${methodValue}] ${reqPath}`);
        isFinished.value = true;
      }

      return ret;
    };
  }

  // expose managed state as return value
  return shell;
}

const extractErrorMessageFromResponse = async (rsp: Response) => {
  let err = null;

  //If not able to get the status text, then try to get the error message from the response body.
  const contentType = rsp.headers.get("content-type") || "";
  if (contentType) {
    if (contentType.startsWith("text/html")) {
      const rawHtml = await rsp.text();
      const parser = new DOMParser();
      const doc = parser.parseFromString(rawHtml, "text/html");
      err = doc.body.textContent || "Unknown error";
    } else if (contentType.startsWith("application/json")) {
      const errMessage: ApiErrorResponse = await rsp.json();
      err = errMessage.error;
    } else {
      err = await rsp.text();
    }
  } else {
    err = rsp.statusText;
  }

  if (!err) {
    // If not able to get the response content, then try to guess the status text from the status code
    err = HttpStatus[rsp.status];
  }
  if (!err) {
    if (rsp.status >= 500 && rsp.status <= 599) {
      err = "System error";
    } else if (rsp.status >= 400 && rsp.status <= 499) {
      err = "User request error";
    } else {
      err = "Unknown error";
    }
  }
  return err;
};
