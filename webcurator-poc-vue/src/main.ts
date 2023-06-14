import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// import './assets/main.css'
import './assets/wct.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'
import 'vue-multiselect'
// import 'tabulator-tables/dist/js/tabulator.min.js'
// import 'tabulator-tables/dist/css/tabulator.min.css'\

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
