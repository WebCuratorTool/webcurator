import { defineStore } from 'pinia'
import { useAuthStore } from './user'

export const useTargetsStore = defineStore({
   
    id: 'targets',
        state: () => ({
            loading: false,
            targets: [],
            error: ''
    }),

    actions: {
        async fetchTargets(searchTerms?: {}) {
            const { token, logout } = useAuthStore()

            this.loading = true
            this.targets = []

            const headers = { Authorization: 'Bearer ' + token, ContentType: "application/json" }
            const body = JSON.stringify({ filter: searchTerms })
            
            const response = await fetch('/api/targets', { headers })

            if (response.status == 200) {
                const targets = await response.json()
                this.targets = targets.targets
                this.error = ''
                this.loading = false
                return true
            } else if (response.status == 403 || response.status == 401) {
                this.error = await response.text()
                logout()
                return false
            } else if (response == null) {
                this.error = 'Could not get targets.'
                this.loading = false
                return false
            } else {
                this.error = await response.text()
                this.loading = false
                return false
            } 
           
        }
    }
})