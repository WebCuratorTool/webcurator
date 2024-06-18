import { ref } from 'vue';
import { defineStore } from 'pinia';
import { useUserProfileStore, getPresentationUserName } from '@/stores/users';
import { type Target, type TargetAccess, type TargetDescription, type TargetGroups, type TargetProfile } from '@/types/target';

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

export const initNewTarget = () => {
    useTargetDescriptionDTO().initData();
    useTargetGeneralDTO().initData();
    useTargetGropusDTO().initData();
    useTargetProfileDTO().initData();
    useTargetSeedsDTO().initData();
    useNextStateStore().initData();
    useTargetAccessDTO().initData();
}

export const setTarget = (target: Target) => {
    console.log(target);
    
    useTargetDescriptionDTO().setData(target.description);
    useTargetGeneralDTO().setData(target.general);
    useTargetGropusDTO().setData(target.groups);
    useTargetProfileDTO().setData(target.profile);
    useTargetSeedsDTO().setData(target.seeds);
    useTargetAccessDTO().setData(target.access);
}

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
    const targetProfile = ref({} as TargetProfile);

    const initData = () => {
        targetProfile.value = {} as TargetProfile;
        targetProfile.value.id = null
        targetProfile.value.overrides = profileOverrides
    }

    const getData = () => {        
        targetProfile.value.overrides.forEach((override) => {       
            // Ensure blockedUrls and includedUrls are arrays
            if (override.id == 'blockedUrls' || override.id == 'includedUrls') {
                if (!Array.isArray(override.value)) {
                    override.value = override.value.toString().split(',');
                }
            }
        })        

        return targetProfile.value;
    }

    const setProfile = (data: {id: number, type: string, name: string}) => {
        targetProfile.value.id = data.id;
        targetProfile.value.harvesterType = data.type;
        targetProfile.value.name = data.name
    }
    
    const setData = (data: TargetProfile) => {
        
        targetProfile.value = data;
    }

    return { targetProfile, initData, getData, setData, setProfile }
});

export const useTargetDescriptionDTO = defineStore('TargetDescriptionDTO', () => {
    const targetDescription = ref({} as TargetDescription);

    const initData = () => {
        targetDescription.value = {} as TargetDescription;
    }

    const setData = (data: TargetDescription) => {
        targetDescription.value = data;
    }
    
    const getData = () => targetDescription.value;

    return { targetDescription, initData, setData, getData }
});

export const useTargetSeedsDTO = defineStore('TargetSeedsDTO', () => {
    const targetSeeds = ref([]);
    
    const initData = () => {
        targetSeeds.value = [];
    }

    const setData = (data: any) => {
        targetSeeds.value = data;
    }
    const getData = () => targetSeeds.value;

    return { targetSeeds, initData, setData, getData }
});

export const useTargetGropusDTO = defineStore('TargetGroupsDTO', () => {
    const targetGroups = ref([] as TargetGroups);
    
    const initData = () => {
        targetGroups.value = [] as TargetGroups;
    }

    const setData = (data: TargetGroups) => {
        targetGroups.value = data;
    }
    const getData = () => targetGroups.value;

    return { targetGroups, initData, setData, getData }
});

export const useTargetAccessDTO = defineStore('TargetAccessDTO', () => {
    const targetAccess = ref({} as TargetAccess);
    
    const initData = () => {
        targetAccess.value = {} as TargetAccess;
    }

    const setData = (data: TargetAccess) => {
        targetAccess.value = data;
    }
    const getData = () => targetAccess.value;

    return { targetAccess, initData, setData, getData }
});