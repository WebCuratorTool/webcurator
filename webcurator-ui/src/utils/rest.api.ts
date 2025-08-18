import router from '@/router';
import { useUserProfileStore } from '@/stores/users';
import { defineStore } from 'pinia';
import { useToast } from 'primevue/usetoast';
import { ref } from 'vue';

export const RootPath = '/wct';
const RootContextPath = RootPath + '/api/v1';
const ToastLife = 30 * 1000;
const RetryDelay = 3 * 1000;

interface LoginResponse {
  ok: boolean;
  title: string;
  detail: string;
}
export type { LoginResponse };

const _login = async (username: string, password: string) => {
  const credentials = 'username=' + username + '&password=' + password;
  const rsp = await fetch(RootPath + '/auth/v1/token', {
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
    detail: '',
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
  }

  const token = await rsp.text();
  const userProfile = useUserProfileStore();
  userProfile.setToken(username, token);

  return feedback;
};

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'HEAD' | 'OPTIONS';

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));
export const usePageAuthStore = defineStore('PageAuthStore', () => {
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
    const rsp = await fetch(RootPath + '/auth/v1/token/' + token);
    return rsp.ok;
  };

  const login = async (username: string, password: string) => {
    const feedback = await _login(username, password);
    if (!feedback.ok) {
      return feedback;
    }

    if (redirectPath.value) {
      router.push(redirectPath.value);
    } else {
      router.push(RootPath);
    }

    return feedback;
  };

  const logout = async () => {
    userProfile.load();
    const token = userProfile.token;
    userProfile.clear();
    if (token) {
      await fetch(RootPath + '/auth/v1/token/' + token, {
        method: 'DELETE',
        redirect: 'error',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      });
    }
    setRedirectPath(RootPath);
    router.push(RootPath + '/login');
  };

  return { isAuthenticated, login, logout, setRedirectPath };
});

export const useApiAuthStore = defineStore('ApiAuthStore', () => {
  const isAuthenticating = ref(false);

  const startLogin = () => {
    isAuthenticating.value = true;
  };

  const authenticate = async (username: string, password: string) => {
    const feedback = await _login(username, password);
    if (!feedback.ok) {
      return feedback;
    }
    isAuthenticating.value = false;
    return feedback;
  };

  return { startLogin, authenticate, isAuthenticating };
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
  const toast = useToast();

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
      const loginStore = useApiAuthStore();

      let ret = null;

      const isFinished = ref(false);

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
          reqPath = RootContextPath + path;
        } else {
          reqPath = RootContextPath + '/' + path;
        }

        const rsp = await fetch(reqPath, reqOptions).catch((err: any) => {
          toast.removeAllGroups();
          toast.add({
            severity: 'warn',
            summary: 'Warning! ',
            detail: err.message,
            life: ToastLife
          });
        });

        //Exception has happened
        if (!rsp) {
          // await sleep(RetryDelay);
          // continue;
          break;
        }

        //Need authentication, forward to login page
        if (rsp.status === 401) {
          loginStore.startLogin();
          continue;
        }

        //Upstream error
        if (rsp.status >= 500 && rsp.status <= 599) {
          toast.removeAllGroups();
          toast.add({
            severity: 'warn',
            summary: 'Warning!',
            detail: '[' + rsp.status + '] System Error! Will retry in ' + RetryDelay / 1000 + ' seconds.',
            life: ToastLife
          });
          await sleep(RetryDelay);
          continue;
        }

        if (rsp.ok) {
          const contentType = rsp.headers.get('content-type') || '';
          const contentLength = parseInt(rsp.headers.get('content-length') || '-1');

          if (contentType.length === 0 && contentLength <= 0) {
            ret = rsp.status;
          } else if (contentType.startsWith('application/json')) {
            ret = await rsp.json();
          } else if (contentType.startsWith('application') || contentType.startsWith('image') || contentType.startsWith('video') || contentType.startsWith('audio')) {
            ret = await rsp.blob();
          } else {
            ret = await rsp.text();
          }
        } else {
          let error = '';
          let e = await rsp.text();
          if (!e) {
            e = 'Unknown error';
          }
          error = '[' + path + '] ' + e + ' StatusCode: ' + rsp.status;
          toast.removeAllGroups();
          toast.add({ severity: 'error', summary: 'Error!', detail: error, life: ToastLife });
          ret = undefined;
        }
        isFinished.value = true;
      }
      return ret;
    };
  }

  // expose managed state as return value
  return shell;
}
