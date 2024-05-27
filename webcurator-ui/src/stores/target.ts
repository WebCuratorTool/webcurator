import { ref, reactive, computed } from 'vue';
import { defineStore } from 'pinia';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';
import { useUserProfileStore, useUsersStore, getPresentationUserName } from '@/stores/users';
import { type TargetDescription } from '@/types/target';

const TARGET_STATE_PENDING = { name: "Pending", code: 1 }
const TARGET_STATE_REINSTATED = { name: "Reinstated", code: 2 }
const TARGET_STATE_NOMINATED = { name: "Nominated", code: 3 }
const TARGET_STATE_REJECTED = { name: "Rejected", code: 4 }
const TARGET_STATE_APPROVED = { name: "Approved", code: 5 }
const TARGET_STATE_CANCELLED = { name: "Cancelled", code: 6 }
const TARGET_STATE_COMPLETED = { name: "Completed", code: 7 }

export const stateList = [
    TARGET_STATE_PENDING,
    TARGET_STATE_REINSTATED,
    TARGET_STATE_NOMINATED,
    TARGET_STATE_REJECTED,
    TARGET_STATE_APPROVED,
    TARGET_STATE_CANCELLED,
    TARGET_STATE_COMPLETED,
]

export const formatTargetState = (state: number | any) => {
    //console.log(state);
    const placeHolder = 'Select a state';

    if (typeof state === 'undefined') {
        return placeHolder;
    }

    if (typeof state === 'number') {
        if (state > 0 && state <= stateList.length) {
            return stateList[state - 1].name;
        } else {
            return placeHolder;
        }
    } else {
        return state.name;
    }
};

export const useNextStateStore = defineStore('TargetNextStateList', () => {
    const nextStateList = ref()

    const initData = () => {
        nextStateList.value = [
            {
                label: 'Original',
                code: 'original',
                items: [TARGET_STATE_PENDING]
            },
            {
                label: 'Next States',
                code: 'next',
                items: [TARGET_STATE_NOMINATED, TARGET_STATE_APPROVED, TARGET_STATE_CANCELLED]
            }
        ]
    }

    const setData = (originalState: any, data: any) => {
        const states = [];
        for (let i = 0; i < data.length; i++) {
            states.push(stateList[data[i] - 1]);
        }
        nextStateList.value = [
            {
                label: 'Original',
                code: 'original',
                items: [originalState]
            },
            {
                label: 'Next States',
                code: 'next',
                items: states
            }
        ]
    }

    return { nextStateList, initData, setData };
});

export const showTargetAction = (target: any, actionName: string) => {
    if (!target || !actionName) {
        return false;
    }

    //TODO: privilege applied

    if (actionName === 'view') {
        return true;
    }

    if (actionName === 'edit') {
        return true;
    }

    if (actionName === 'new' || actionName === 'copy') {
        return true;
    }

    if (actionName === 'delete') {
        return (target.state === TARGET_STATE_REJECTED.code || target.state === TARGET_STATE_CANCELLED.code);
    }
};

export const useTargetGeneralDTO = defineStore('TargetDTOGeneral', () => {
    const id = ref();
    const name = ref("");
    const creationDate = ref(0);
    const description = ref("");
    const referenceNumber = ref("");
    const runOnApproval = ref(false);
    const automatedQA = ref(false);
    const selectedUser = ref();
    const selectedState = ref(TARGET_STATE_PENDING);
    const autoPrune = ref(false);
    const referenceCrawl = ref(false);
    const requestToArchivists = ref("");
    const nextStates = ref([]);

    const userProfile = useUserProfileStore();

    const initData = () => {
        id.value = undefined;
        name.value = "";
        creationDate.value = 0;
        description.value = "";
        referenceNumber.value = "";
        runOnApproval.value = false;
        automatedQA.value = false;
        selectedUser.value = {
            name: userProfile.currUserName,
            code: userProfile.name,
        };
        selectedState.value = TARGET_STATE_PENDING;
        autoPrune.value = false;
        referenceCrawl.value = false;
        requestToArchivists.value = "";
        nextStates.value = [];
    }

    const getData = () => {
        return {
            id: id.value,
            name: name.value,
            creationDate: Date.now(),
            description: description.value,
            referenceNumber: referenceNumber.value,
            runOnApproval: runOnApproval.value,
            automatedQA: automatedQA.value,
            owner: selectedUser.value.code,
            state: selectedState.value.code,
            autoPrune: autoPrune.value,
            referenceCrawl: referenceCrawl.value,
            requestToArchivists: requestToArchivists.value,
        }
    }

    const setData = (data: any) => {        
        id.value = data.id;
        name.value = data.name;
        creationDate.value = data.creationDate;
        description.value = data.description;
        referenceNumber.value = data.referenceNumber;
        runOnApproval.value = data.runOnApproval;
        automatedQA.value = data.automatedQA;
        selectedUser.value = {
            name: getPresentationUserName(data.owner),
            code: data.owner,
        };
        selectedState.value = {
            code: data.state,
            name: formatTargetState(data.state),
        };
        autoPrune.value = data.autoPrune;
        referenceCrawl.value = data.referenceCrawl;
        requestToArchivists.value = data.requestToArchivists;

        // const rest: UseFetchApis=useFetch();
        // rest.get('targets/nextStates/'+data.id).then((rsp:any)=>{
        //     console.log(rsp);
        // }).catch((err:any)=>{

        // });
    }

    return { id, name, creationDate, description, referenceNumber, runOnApproval, automatedQA, selectedUser, selectedState, autoPrune, referenceCrawl, requestToArchivists, initData, getData, setData };
});

const profileOverrides = [
    {
        id:	"documentLimit",
        value:	0,
        enabled: false
    },
    {
        id: "dataLimit",
        value: 0.0,
        enabled: false,
        unit: "B"
    },
    {
        id: "timeLimit",
        value: 0.0,
        enabled: false,
        unit: "SECOND"	
    },
    {
        id: "maxPathDepth",
        value: 0,
        enabled: false,
    },
    {
        id: "maxHops",
        value: 0,
        enabled: false
    },
    {   
        id: "maxTransitiveHops",
        value: 0,
        enabled: false
    },
    {
        id: "ignoreRobots",
        value: false,
        enabled: false
    },
    {
        id: "extractJs",
        value: false,
        enabled: false
    },
    {
        id: "ignoreCookies",
        value: false,
        enabled: false
    },
    {
        id: "blockedUrls",
        value: [],
        enabled: false
    },
    {
        id: "includedUrls",
        value: [],
        enabled: false
    }
];

export const useTargetProfileDTO = defineStore('TargetProfileDTO', () => {
    const harvesterType	= ref('');
    const id = ref();
    const imported = ref(false);
    const name = ref('');
    const overrides = ref(profileOverrides);

    const initData = () => {
        harvesterType.value = '',
        id.value = null,
        imported.value = false,
        name.value = '',
        overrides.value = profileOverrides
    }

    const getData = () => {        
        overrides.value.forEach((override) => {       
            // Ensure blockedUrls and includedUrls are arrays
            if (override.id == 'blockedUrls' || override.id == 'includedUrls') {
                if (!Array.isArray(override.value)) {
                    override.value = override.value.split(',');
                }
            }
        })

        return {
            harvesterType: harvesterType.value,
            id: id.value,
            imported: imported.value,
            name: name.value,
            overrides: overrides.value
        }
    }
    
    const setData = (data: any) => {
        harvesterType.value = data.harvesterType,
        id.value = data.id,
        imported.value = data.imported,
        name.value = data.name
        overrides.value = data.overrides
    }

    return { harvesterType, id, imported, name, overrides, initData, getData, setData }
});

export const useTargetDescriptionDTO = defineStore('TargetDescriptionDTO', () => {
    const targetDescription = ref({} as TargetDescription);

    const initData = () => {
        targetDescription.value = {} as TargetDescription;
    }

    const setData = (data: any) => {
        targetDescription.value.identifier = data.identifier;
        targetDescription.value.description = data.description; 
        targetDescription.value.subject = data.subject;
        targetDescription.value.creator = data.creator;
        targetDescription.value.publisher = data.publisher; 
        targetDescription.value.type = data.type;
        targetDescription.value.format = data.format; 
        targetDescription.value.language = data.language; 
        targetDescription.value.source = data.source; 
        targetDescription.value.relation = data.relation; 
        targetDescription.value.contributor = data.contributor; 
        targetDescription.value.coverage = data.coverage; 
        targetDescription.value.issn = data.issn; 
        targetDescription.value.isbn = data.isbn; 
    }
    
    const getData = () => targetDescription.value;

    return { targetDescription, initData, setData, getData }
});