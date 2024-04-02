
<template>
	<div>
        <!-- <div class="text-center mb-5" style="padding: 0px 100px 5px 100px;">
            <img src="@/assets/wct_logo.png" alt="Image" height="50" class="mb-3" />            
        </div> -->

        <div class="flex align-items-center gap-3 mb-3">
            <label for="username" class="font-semibold w-6rem">Username</label>
            <br/>
            <InputText id="username" type="text" placeholder="Username" class="w-full mb-3" v-model="username"/>
        </div>
        <br/>
        <div class="flex align-items-center gap-3 mb-3">
            <label for="password" class="font-semibold w-6rem">Password</label>
            <br/>
            <InputText id="password" type="password" placeholder="Password" class="w-full mb-3" v-model="password"/>
        </div>

        <br/>
        <Divider />
        <br/>

        <div class="flex justify-content-end gap-2">
            <Button type="button" icon="pi pi-user" label="login"  class="w-full p-3 text-xl" @click="auth"></Button>
        </div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from "vue";
// import { useToast } from 'primevue/usetoast';
// const toast = useToast();

const dialogRef:any = inject("dialogRef");
const username=ref(null);
const password=ref(null);


const auth = () => {
    var url="./auth/v1/token?username=" + username.value + "&password=" + password.value;
    fetch(url, {
        method: 'POST',
        redirect: 'error',
        headers: {
            'Content-Type': 'application/json',
        }
    }).then((rsp)=>{
        // console.log(rsp);
        if(!rsp.ok){
            let status = rsp.status;
            let statusText = rsp.statusText;
            if(!statusText || statusText.length===0){
                if(status === 401){
                    statusText = "Unknown username or password, please try again.";
                }else{
                    statusText = "Unknown error."
                }
            }
            throw new Error(status + " : " + statusText);
        }
        return rsp.text();
    })
    .then((tokenValue)=>{
        // console.log("token:" + token);
        dialogRef.value.close(tokenValue);
    }).catch((err)=>{
        // console.log(err);
        // toast.add({ severity: 'error', summary: 'Error Message', detail: err.message, life: 3000 });
        alert(err.message);
    });
};
</script>

<style scoped>
    .p-inputtext{
        width: 300px;
    }
    
    .p-button{
        width: 300px;
    }
</style>