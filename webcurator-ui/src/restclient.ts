
import { useDialog } from 'primevue/usedialog';
import LoginDialog from './components/LoginDialog.vue';
import { reactive } from 'vue';

const openLoginDialog = (dialog:any) => {
    const dialogRef = dialog.open(LoginDialog, {
        props: {
            header: 'Product List',
            style: {
                width: '50vw',
            },
            breakpoints:{
                '960px': '75vw',
                '640px': '90vw'
            },
            modal: true
        },
        
        onClose: (options) => {
            console.log(options);
        }
    });
}

export default {
    openLoginDialog
}



// let dialog:any=null;

// export default {
//     setup(){
//         dialog=useDialog();
//     },

//     openLoginDialog(){
//         dialog=useDialog();

//         const dialogRef = dialog.open(LoginDialog, {
//             props: {
//                 header: 'Product List',
//                 style: {
//                     width: '50vw',
//                 },
//                 breakpoints:{
//                     '960px': '75vw',
//                     '640px': '90vw'
//                 },
//                 modal: true
//             },
            
//             onClose: (options:any) => {
//                 console.log(options);
//             }
//         });
//     }
// }


// export declare function useDialog(): {
//         const dialog = useDialog();

//         const dialogRef = dialog.open(LoginDialog, {
//             props: {
//                 header: 'Product List',
//                 style: {
//                     width: '50vw',
//                 },
//                 breakpoints:{
//                     '960px': '75vw',
//                     '640px': '90vw'
//                 },
//                 modal: true
//             },
            
//             onClose: (options:any) => {
//                 console.log(options);
//             }
//         });
// }

