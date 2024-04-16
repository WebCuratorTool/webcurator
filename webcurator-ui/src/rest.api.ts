import { ref, reactive } from 'vue'
import { useDialog } from 'primevue/usedialog';
import LoginDialog from './components/LoginDialog.vue';

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'HEAD' | 'OPTIONS';

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

export const token = reactive({
    value: ""
});

export const isAuthenticating = reactive ({
    value: false
});

export interface UseFetchApis {
    // methods
    get: (path:string) => any
    post: (path:string, payload: any) => any
    put: (path:string, payload: any) => any
    delete: (path:string, payload: any) => any
    patch: (path:string, payload: any) => any
    head: (path:string) => any
    options: (path:string, payload: any) => any
}

// by convention, composable function names start with "use"
export function useFetch() {
    // state encapsulated and managed by the composable
    const dialog = useDialog();
    
    // a composable can update its managed state over time.
    async function openLoginDialog(dialog:any) {
        const last_token=token.value;
        await sleep(Math.floor((Math.random() * 100) + 1));
        
        if(isAuthenticating.value){
            console.log("The login window is opened");
            return;
        }
        
        if(last_token !== token.value){
            console.log("The token was updated.");
            isAuthenticating.value=false;
            return;
        }

        isAuthenticating.value=true;

        const dialogRef = dialog.open(LoginDialog, {
            props: {
                header: 'Please login',
                closable: false,
                style: {
                    width: '350px',
                },
                modal: true
            },
            
            onClose: (options:any) => {
                console.log(options);
                token.value = options.data;
                isAuthenticating.value=false;
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
        return async (path:string, payload:any=null) => {                                                        
            let ret=null;

            const isFinished=ref(false);
 
            //isFinished.value=false;

            //Retry until it's finished. If the login session is expired, it can be run 2 rounds
            while(!isFinished.value){
                // Waiting until the authentication is finished
                while(isAuthenticating.value){
                    await sleep(1000);
                }

                const reqOptions:RequestInit={
                    method: methodValue,
                    redirect: 'error',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': token.value,
                    }
                }
                if(payload){
                    reqOptions.body=JSON.stringify(payload);
                }

                ret = await fetch('./api/v1/' + path, reqOptions).then(rsp=>{
                    console.log(rsp);
                    if(rsp.status==401){                                                
                        return null;
                    }

                    isFinished.value = true;
                    
                    if(rsp.ok){                        
                        return rsp.json();
                    }else{
                        let statusText = rsp.statusText;
                        if(!statusText || statusText.length===0){              
                            statusText = "Unknown error."                
                        }                        
                        throw new Error(rsp.status + " : " + statusText);
                    }
                });

                if(!isFinished.value){
                    await openLoginDialog(dialog);
                }
            }
            return ret;
        }
    }

    // expose managed state as return value
    return shell;
}