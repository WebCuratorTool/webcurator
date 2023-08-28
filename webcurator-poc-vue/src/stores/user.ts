import { defineStore } from 'pinia'
import * as Request from '@/requests'
import router  from '@/router'

export const useAuthStore = defineStore({
  id: 'auth',
  state: () => ({
    token: '',
    error: '',
    returnUrl: '',
  }),
  persist: true,

  getters: {
    isLoggedIn: (state) => state.token !== ''
  },

  actions: {
    async login(username: string, password: string) {
      this.token = ''
      this.error = ''

      const request = {
        method: "POST",
        body: new URLSearchParams({ username, password })
      }
      try {
        const response = await fetch('auth', request)
        if (response.status != 200) {
          this.error = 'Authentication failed'
        } else {
          this.token = await response.text()
          router.push(this.returnUrl || '/')
          this.returnUrl = ''
        }
      } catch (e) {
        this.error = 'Error logging in'
        this.returnUrl = ''
      } 
    },
    async logout() {
      this.token = ''
      this.error = ''
      this.returnUrl = ''
      router.push('/login')
    }
  }
})