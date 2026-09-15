import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import '@fontsource/noto-sans-sc/400.css'
import '@fontsource/noto-sans-sc/500.css'
import '@fontsource/noto-sans-sc/700.css'
import '@fontsource/noto-serif-sc/600.css'
import '@fontsource/noto-serif-sc/700.css'
import '@fontsource/noto-serif-sc/800.css'
import '@/styles/tokens.css'
import '@/styles/list.css'
import App from '@/App.vue'
import router from '@/router'

document.documentElement.classList.add('dark')

const app = createApp(App)
app.use(createPinia().use(piniaPluginPersistedstate))
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
