// mouse.js
import { ref, reactive, watchEffect, toValue } from 'vue'
import { useDialog } from 'primevue/usedialog';
import LoginDialog from './components/LoginDialog.vue';

export interface FetchOptions {
    fetch?:boolean
    method?: string
    url?:string
    data?:any
    timestamp?:string
}

export const token = reactive({
    value: ""
});

export const dialog = reactive({
    value: null
})

// by convention, composable function names start with "use"
export function useFetch(options:any) {
    // state encapsulated and managed by the composable
    // const token = ref();
    const dialog = useDialog();
    const data = ref()
    const error = ref()
    const previousFetchOptions=ref()

    async function httpFetch(fetchOptions:FetchOptions){
        let url=fetchOptions.url;
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

        let method=fetchOptions.method;
        if(!method){
            error.value="500: the method is expected";
            return;
        }
        method=method.toUpperCase();
        if(method!=="GET" && method!=="POST" && method!=="PUT" && method!=="DELETE" && method!=="PATCH" && method!=="HEAD" && method!=="OPTIONS" && method!=="TRACE" && method!=="CONNECT"){
            error.value="500: unknown method: " + method;
            return;
        }

        let body="";
        if(fetchOptions.data){
            body=JSON.stringify(fetchOptions.data);
        }
        previousFetchOptions.value=fetchOptions;
        fetch(url, {
            method: "GET",
            redirect: 'error',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token.value,
            },
            body: body
        }).then(rsp=>{
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
            error.value=err.message;
        });
    }
  
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
                httpFetch(previousFetchOptions.value);
            }
        });
    }

    // a composable can also hook into its owner component's
    // lifecycle to setup and teardown side effects.
    //   onMounted(() => window.addEventListener('mousemove', update))
    //   onUnmounted(() => window.removeEventListener('mousemove', update))
    watchEffect(async() => {
        data.value = null;
        error.value = null;

        const optionsValue: FetchOptions = toValue(options);
        if(!optionsValue.fetch){
            return;
        }

        console.log("url:" + optionsValue.url);
        await httpFetch(optionsValue);
    })

    // expose managed state as return value
    return { data, error }
}