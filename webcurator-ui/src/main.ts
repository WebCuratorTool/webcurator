import "@/assets/styles.scss";

import Aura from "@primeuix/themes/aura";
import { createPinia } from "pinia";
import PrimeVue from "primevue/config";
import ConfirmationService from "primevue/confirmationservice";
import DialogService from "primevue/dialogservice";
import ToastService from "primevue/toastservice";
import { createApp } from "vue";

import App from "./App.vue";
import router from "./router";

const app = createApp(App);

app.use(createPinia());
app.use(router);

// app.use(PrimeVue, { unstyled: false });
app.use(PrimeVue, {
  // ripple: true,
  theme: { preset: Aura, options: { darkModeSelector: ".app-dark" } },
});
app.use(ConfirmationService);
app.use(ToastService);
app.use(DialogService);

app.mount("#app");
