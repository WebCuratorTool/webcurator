import router from '@/router';
import { useUserProfileStore } from '@/stores/users';
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useAlertStore } from './alertStore';
import { HttpStatus } from './rest.http.status';

export const BasePath = '/wct';
export const HomePagePath = '/';
export const LoginPagePath = '/login';
export const ApiRootPath = '/wct';
const ApiContextPath = ApiRootPath + '/api/v1';
const RetryDelay = 20 * 1000;
const MaxRetryTimes = 3;

interface LoginResponse {
  ok: boolean;
  title: string;
  detail: string;
}
export type { LoginResponse };

const _login = async (username: string, password: string) => {
  const credentials = 'username=' + username + '&password=' + password;
  const rsp = await fetch(ApiRootPath + '/auth/v1/token', {
    method: 'POST',
    redirect: 'error',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: credentials
  });

  const feedback = {
    ok: true,
    title: '',
    detail: ''
  } as LoginResponse;

  if (!rsp.ok) {
    const status = rsp.status;
    let statusText = rsp.statusText;
    if (!statusText || statusText.length === 0) {
      if (status === 401) {
        statusText = 'Unknown username or password, please try again.';
      } else {
        statusText = 'Unknown error.';
      }
    }
    feedback.ok = false;
    feedback.title = 'Error: ' + status;
    feedback.detail = statusText;
  } else {
    const token = await rsp.text();
    const userProfile = useUserProfileStore();
    userProfile.setToken(username, token);
  }

  return feedback;
};

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'HEAD' | 'OPTIONS';

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));
export const useAuthStore = defineStore('AuthStore', () => {
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
    const rsp = await fetch(ApiRootPath + '/auth/v1/token/' + token);
    return rsp.ok;
  };

  const authenticate = async (routePath: string, username: string, password: string) => {
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
      await fetch(ApiRootPath + '/auth/v1/token/' + token, {
        method: 'DELETE',
        redirect: 'error',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      });
    }
    setRedirectPath(HomePagePath);
    router.push(LoginPagePath);
  };

  return { startLogin, authenticate, isAuthenticating, isAuthenticated, logout, setRedirectPath };
});

export interface UseFetchApis {
  // methods
  get: (path: string) => any;
  post: (path: string, payload: any, customHeader?: any) => any;
  put: (path: string, payload: any) => any;
  delete: (path: string, payload: any) => any;
  patch: (path: string, payload: any) => any;
  head: (path: string) => any;
  options: (path: string, payload: any) => any;
}

// by convention, composable function names start with "use"
export function useFetch() {
  // state encapsulated and managed by the composable
  const confirm = useAlertStore();

  const shell: UseFetchApis = {
    // method
    get: setMethod('GET'),
    put: setMethod('PUT'),
    post: setMethod('POST'),
    delete: setMethod('DELETE'),
    patch: setMethod('PATCH'),
    head: setMethod('HEAD'),
    options: setMethod('OPTIONS')
  };

  function setMethod(methodValue: HttpMethod) {
    return async (path: string, payload: any = null, customHeader: any = null) => {
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

        const requestHeaders: HeadersInit = new Headers();
        requestHeaders.set('Content-Type', 'application/json');
        requestHeaders.set('Authorization', userProfile.token);

        if (customHeader) {
          requestHeaders.set(customHeader.header, customHeader.value);
        }

        const reqOptions: RequestInit = {
          method: methodValue,
          redirect: 'error',
          headers: requestHeaders
        };
        if (payload !== null && payload !== undefined) {
          reqOptions.body = JSON.stringify(payload);
        }

        let reqPath;
        if (path.startsWith('/')) {
          reqPath = ApiContextPath + path;
        } else {
          reqPath = ApiContextPath + '/' + path;
        }

        let rsp;
        try {
          rsp = await fetch(reqPath, reqOptions);
        } catch (err: any) {
          retriedTimes.value++;
          if (retriedTimes.value >= MaxRetryTimes) {
            confirm.error(`Failed to [${methodValue}] ${reqPath}: ${err.message}`);
            break;
          } else {
            confirm.warning(`Failed to [${methodValue}] ${reqPath}: ${err.message}.  Will retry in ${RetryDelay / 1000} seconds.`);
            await sleep(RetryDelay);
            continue;
          }
        }

        if (!rsp) {
          //Exception has happened
          confirm.error('Failed to [' + methodValue + '] ' + reqPath);
          break;
        } else if (rsp.status === 502 || rsp.status === 504) {
          //Upstream error
          const err = await extractErrorMessageFromResponse(rsp);
          retriedTimes.value++;
          if (retriedTimes.value >= MaxRetryTimes) {
            confirm.error(`Failed to [${methodValue}] ${reqPath}: ${err}`);
            break;
          } else {
            confirm.warning(`${err}. Will retry in ${RetryDelay / 1000} seconds.`);
            await sleep(RetryDelay);
            continue;
          }
        } else if (rsp.status === 401) {
          loginStore.startLogin();
          continue;
        } else if (rsp.status === 403) {
          const err = await extractErrorMessageFromResponse(rsp);
          confirm.error(`User does not have role to [${methodValue}] ${reqPath}: ${err}`);
          continue;
        } else if (!rsp.ok) {
          const err = await extractErrorMessageFromResponse(rsp);
          confirm.error(`Failed to [${methodValue}] ${reqPath}: ${err}`);
          break;
        }

        const contentType = rsp.headers.get('content-type') || '';
        const contentLength = parseInt(rsp.headers.get('content-length') || '-1');

        if (!contentType && contentLength <= 0) {
          ret = rsp.status;
        } else if (contentType.startsWith('application/json')) {
          ret = await rsp.json();
        } else if (contentType.startsWith('application') || contentType.startsWith('image') || contentType.startsWith('video') || contentType.startsWith('audio')) {
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

const extractErrorMessageFromResponse = async (rsp: any) => {
  let err = rsp.statusText;

  if (!err) {
    //If not able to get the status text, then try to get the error message from the response body.
    const contentType = rsp.headers.get('content-type') || '';
    if (contentType) {
      if (contentType.startsWith('text/html')) {
        const rawHtml = await rsp.text();
        const parser = new DOMParser();
        const doc = parser.parseFromString(rawHtml, 'text/html');
        err = doc.body.textContent || 'Unknown error';
      } else if (contentType.startsWith('application/json')) {
        const errMessage = await rsp.json();
        err = errMessage.error;
      } else {
        err = await rsp.text();
      }
    }
  }

  if (!err) {
    // If not able to get the response content, then try to guess the status text from the status code
    err = HttpStatus[rsp.status];
  }
  if (!err) {
    if (rsp.status >= 500 && rsp.status <= 599) {
      err = 'System error';
    } else if (rsp.status >= 400 && rsp.status <= 499) {
      err = 'User request error';
    } else {
      err = 'Unknown error';
    }
  }
  return `[${rsp.status}] ${err}`;
};
