<template>
  <div class="dashboard">
    <PageHeader title="Dashboard" />
    <Button label="Login" @click="showProducts" />
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue';
import { useDialog } from 'primevue/usedialog';
// import LoginDialog from '@/components/LoginDialog.vue';
// const dialog = useDialog();

// const showProducts = () => {
//     const dialogRef = dialog.open(LoginDialog, {
//         props: {
//             header: 'Product List',
//             style: {
//                 width: '50vw',
//             },
//             breakpoints:{
//                 '960px': '75vw',
//                 '640px': '90vw'
//             },
//             modal: true
//         },
        
//         onClose: (options) => {
//             console.log(options);
//         }
//     });
// }

import api from '../restclient';

api.dynamicDialog.dialog=useDialog();

const showProducts = () => {
  console.log(api.openLoginDialog);
  api.openLoginDialog();
}
</script>

<style>

</style>
../restclient.ts