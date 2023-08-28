import { defineStore } from 'pinia'
import { useAuthStore } from './user'

import type { Target } from '@/types/target'

export const useTargetStore = defineStore({
    id: 'target',
        state: () => ({
            loading: true,
            target: {} as Target,
            error: ''
    }),

    actions: {
        async fetchTarget(id: string) {
            const { token, logout } = useAuthStore()

            this.loading = true
            this.target = {} as Target

            const headers = { Authorization: 'Bearer ' + token }
            const response = await fetch(`/api/targets/${id}`, { headers })

            if (response.status == 200) {
                const target = await response.json()
                this.target = target
                this.error = ''
                this.loading = false
                return true
            } else if (response.status == 403 || response.status == 401) {
                this.error = await response.text()
                logout()
                return false
            } else if (response == null) {
                this.error = 'Could not get target.'
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