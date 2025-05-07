import { createPinia } from 'pinia';
import { createApp } from 'vue';

import Aura from '@primeuix/themes/aura';
import PrimeVue from 'primevue/config';
import ConfirmationService from 'primevue/confirmationservice';
import DialogService from 'primevue/dialogservice';
import ToastService from 'primevue/toastservice';
import App from './App.vue';
import router from './router';

import '@/assets/styles.scss';

const app = createApp(App);

app.use(createPinia());
app.use(router);

// app.use(PrimeVue, { unstyled: false });
app.use(PrimeVue, {
  ripple: true,
  theme: { preset: Aura, options: { darkModeSelector: '.app-dark' } }
});
app.use(ConfirmationService);
app.use(ToastService);
app.use(DialogService);

app.mount('#app');
