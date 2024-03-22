
<template>
	<div>
        <div class="flex align-items-center gap-3 mb-3">
            <label for="username" class="font-semibold w-6rem">Username</label>
            <InputText id="username" class="flex-auto" autocomplete="off" v-model="username" />
        </div>
        <div class="flex align-items-center gap-3 mb-5">
            <label for="password" class="font-semibold w-6rem">Password</label>
            <Password id="password" class="flex-auto" autocomplete="off"  v-model="password" toggleMask/>
        </div>
        <div class="flex justify-content-end gap-2">
            <Button type="button" label="Cancel" severity="secondary" @click="visible = false"></Button>
            <Button type="button" label="Save" @click="visible = false"></Button>
        </div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from "vue";

const dialogRef:any = inject("dialogRef");
const username=ref("leefr");
const password=ref("password")

onMounted(() => {
    
});

const auth = async () => {
    var url="./auth/v1/token?username=lql&password=1qaz@WSX";
    const response = await fetch(url, {
        method: 'POST',
        redirect: 'follow',
        headers: {
            'Content-Type': 'application/json',
        }
    });
    return response.text();
};


const selectProduct = (data:any) => {
    dialogRef.value.close(data);
};

const showInfo = () => {
    
};
</script>
