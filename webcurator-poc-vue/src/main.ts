import { createApp, watch } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

import App from './App.vue'
import router from './router'

// import './assets/main.css'
import './assets/wct.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'
import 'vue-multiselect'
// import 'tabulator-tables/dist/js/tabulator.min.js'
// import 'tabulator-tables/dist/css/tabulator.min.css'\

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

const app = createApp(App)
    .use(router)
    .use(pinia)
    .use(createPinia)

app.mount('#app')
