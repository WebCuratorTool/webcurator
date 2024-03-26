import { ref, reactive, watchEffect, toValue } from 'vue'
import { useDialog } from 'primevue/usedialog';
import LoginDialog from './components/LoginDialog.vue';

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'HEAD' | 'OPTIONS';

export interface FetchOptions {
    method?: string
    path?:string
    payload?:any
    timestamp?:string
}

export const token = reactive({
    value: ""
});

export const dialog = reactive({
    value: null
});

export interface UseFetchReturn {
    // methods
    get: (path:string, payload: any | null) => any
    post: (path:string, payload: any) => any
    put: (path:string, payload: any) => any
    delete: (path:string, payload: any) => any
    patch: (path:string, payload: any) => any
    head: (path:string, payload: any) => any
    options: (path:string, payload: any) => any
}

// by convention, composable function names start with "use"
export function useFetch() {
    // state encapsulated and managed by the composable
    // const token = ref();
    const dialog = useDialog();
    const data = ref()
    const error = ref()
    const previousFetchOptions=ref()
 
    // a composable can update its managed state over time.
    async function openLoginDialog(dialog:any) {
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
                // httpFetch(previousFetchOptions.value);
            }
        });
    }

    const shell: UseFetchReturn = {
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
        return (path:string, payload:any) => {
            let url=path;
            if(!url){
                error.value="500: the url is expected";
                return;
            }
            if(url?.startsWith('/')){
                url='.'+url;
            }else if(url?.startsWith('./') || url?.startsWith('../')){
                //Do nothing
            }else{
                url='./'+url;
            }

            // let method=fetchOptions.method;
            // if(!method){
            //     error.value="500: the method is expected";
            //     return;
            // }
            // method=method.toUpperCase();
            // if(method!=="GET" && method!=="POST" && method!=="PUT" && method!=="DELETE" && method!=="PATCH" && method!=="HEAD" && method!=="OPTIONS" && method!=="TRACE" && method!=="CONNECT"){
            //     error.value="500: unknown method: " + method;
            //     return;
            // }

            let body="";
            if(payload){
                body=JSON.stringify(payload);
            }
            previousFetchOptions.value={
                method: methodValue,
                path: path,
                payload: payload
            };

            fetch(url, {
                method: methodValue,
                redirect: 'error',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': token.value,
                },
            }).then(rsp=>{
                console.log(rsp);
                if(rsp.ok){
                    return rsp.json();
                }

                if(rsp.status==401){
                    openLoginDialog(dialog);
                    return null;
                }

                let statusText = rsp.statusText;
                if(!statusText || statusText.length===0){              
                    statusText = "Unknown error."                
                }
                throw new Error(rsp.status + " : " + statusText);
            }).then(jsonData=>{
                if(jsonData){
                    data.value=jsonData;
                }
            }).catch(err=>{
                console.log(err);
                error.value=err.message;
            });

            return { data, error };
        }
    }

    // expose managed state as return value
    return shell;
}