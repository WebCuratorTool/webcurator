import { ref, reactive } from 'vue'
import { useDialog } from 'primevue/usedialog';
import LoginDialog from '@/components/LoginDialog.vue';
import { useUserProfileStore } from '@/stores/users';

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'HEAD' | 'OPTIONS';

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

export const isAuthenticating = reactive({
    value: false
});

export interface UseFetchApis {
    // methods
    get: (path: string) => any
    post: (path: string, payload: any, customHeader?: any) => any
    put: (path: string, payload: any) => any
    delete: (path: string, payload: any) => any
    patch: (path: string, payload: any) => any
    head: (path: string) => any
    options: (path: string, payload: any) => any
}

// by convention, composable function names start with "use"
export function useFetch() {
    // state encapsulated and managed by the composable
    const dialog = useDialog();

    // a composable can update its managed state over time.
    async function openLoginDialog(dialog: any) {
        const userProfile = useUserProfileStore();
        const last_token = userProfile.token;
        await sleep(Math.floor((Math.random() * 100) + 1));

        if (isAuthenticating.value) {
            console.log("The login window is opened");
            return;
        }

        if (last_token !== userProfile.token) {
            console.log("The token was updated.");
            isAuthenticating.value = false;
            return;
        }

        isAuthenticating.value = true;

        const dialogRef = dialog.open(LoginDialog, {
            props: {
                header: 'Please login',
                closable: false,
                style: {
                    width: '350px',
                },
                modal: true
            },

            onClose: (options: any) => {
                //console.log(options);
                // token.setToken(options.data);
                isAuthenticating.value = false;
            }
        });
    }

    const shell: UseFetchApis = {
        // method
        get: setMethod('GET'),
        put: setMethod('PUT'),
        post: setMethod('POST'),
        delete: setMethod('DELETE'),
        patch: setMethod('PATCH'),
        head: setMethod('HEAD'),
        options: setMethod('OPTIONS'),
    }

    function setMethod(methodValue: HttpMethod) {
        return async (path: string, payload: any = null, customHeader: any = null) => {
            const userProfile = useUserProfileStore();

            let ret = null;

            const isFinished = ref(false);

            //isFinished.value=false;

            //Retry until it's finished. If the login session is expired, it can be run 2 rounds
            while (!isFinished.value) {
                // Waiting until the authentication is finished
                while (isAuthenticating.value) {
                    await sleep(1000);
                }

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
                }
                if (payload) {
                    reqOptions.body = JSON.stringify(payload);
                }                
                
                // const response = await fetch('/wct/api/v1/' + path, reqOptions);
                // if (response.status == 401) {
                //     return null;
                // }

                // isFinished.value = true;

                // if (response.ok) {
                //     try {
                //         const text = await response.text();
                //         const data = JSON.parse(text);
                //         return data;
                //     } catch {
                //         return { status: "success"}
                //     }
                // } else {                    
                //     let errorMessage
                //     const error = await response.json()
                //     if (!error || error.length === 0) {
                //         errorMessage = "Unknown error."
                //     } else {
                //         errorMessage = error.Error
                //     }
                //     throw new Error(response.status + " : " + errorMessage);
                // }
                

                ret = await fetch('/wct/api/v1/' + path, reqOptions).then(async rsp => {
                    // console.log(rsp);
                    if (rsp.status == 401) {
                        return null;
                    }

                    isFinished.value = true;

                    if (rsp.ok) {
                        try {
                            const text = await rsp.text();
                            const data = JSON.parse(text);
                            return data;
                        } catch {
                            return "Success";
                        }
                    } else {                    
                        let errorMessage
                        const error = await rsp.json()
                        if (!error || error.length === 0) {
                            errorMessage = "Unknown error."
                        } else {
                            errorMessage = error.Error
                        }
                        throw new Error(rsp.status + " : " + errorMessage);
                    }
                });

                if (!isFinished.value) {
                    await openLoginDialog(dialog);
                }
            }
            return ret;
        }
    }

    // expose managed state as return value
    return shell;
}