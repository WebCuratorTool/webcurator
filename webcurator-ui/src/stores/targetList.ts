import { ref } from 'vue';
import { defineStore } from 'pinia';

export const useTargetListSearchStore = defineStore('TargetListSearchStore', () => {
    // Search conditions
    const targetId = ref(null)
    const targetName = ref('')
    const targetSeed = ref('')
    const targetDescription = ref('')
    const targetMemberOf = ref('')

    const noneDisplayOnly = ref(false)

    return { 
        targetId,
        targetName,
        targetSeed, 
        targetDescription, 
        targetMemberOf, 
        noneDisplayOnly
    }
}) 

export const useTargetListFiltertore = defineStore('TargetListFilterStore', () => {
    // Filter conditions
    const selectedAgency = ref({ name: "", code: "" })
    const selectedUser = ref({ name: "", code: "" })

    const selectedState = ref([])

    return { 
        selectedAgency, 
        selectedUser, 
        selectedState, 
    }
})