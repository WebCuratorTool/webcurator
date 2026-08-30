import "@/assets/styles.scss";

import { createPinia } from "pinia";
import { createApp } from "vue";

import App from "./App.vue";
import router from "./router";

const app = createApp(App);

app.use(createPinia());
app.use(router);

app.directive("tooltip", {
  mounted(el, binding) {
    if (typeof binding.value === "string") {
      el.setAttribute("title", binding.value);
    }
  },
  updated(el, binding) {
    if (typeof binding.value === "string") {
      el.setAttribute("title", binding.value);
    }
  },
});

app.mount("#app");
