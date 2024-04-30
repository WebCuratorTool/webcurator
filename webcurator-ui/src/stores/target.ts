import { ref, reactive, computed } from 'vue';
import { defineStore } from 'pinia';
import {type UseFetchApis, useFetch} from '@/utils/rest.api';
import {useUserProfileStore, useUsersStore, getPresentationUserName} from '@/stores/users';

const TARGET_STATE_PENDING={name:"Pending",  code:1}
const TARGET_STATE_REINSTATED={name:"Reinstated",  code:2}
const TARGET_STATE_NOMINATED={name:"Nominated",  code:3}
const TARGET_STATE_REJECTED={name:"Rejected",  code:4}
const TARGET_STATE_APPROVED={name:"Approved",  code:5}
const TARGET_STATE_CANCELLED={name:"Cancelled",  code:6}
const TARGET_STATE_COMPLETED={name:"Completed",  code:7}

export const stateList=[
    TARGET_STATE_PENDING,
    TARGET_STATE_REINSTATED,
    TARGET_STATE_NOMINATED,
    TARGET_STATE_REJECTED,
    TARGET_STATE_APPROVED,
    TARGET_STATE_CANCELLED,
    TARGET_STATE_COMPLETED,
]

export const formatTargetState = (state:number|any) => {
    //console.log(state);
    const placeHolder='Select a state';
    
    if(typeof state === 'undefined'){
        return placeHolder;
    }

    if(typeof state === 'number'){
        if(state>0 && state<=stateList.length){
            return stateList[state - 1].name;
        }else{
            return placeHolder;
        }
    }else{
        return state.name;
    }
};


export const isTargetAction=(target:any, actionName:string)=>{
    if(!target || !actionName){
        return false;
    }

    //TODO: privilege applied

    if(actionName === 'view'){
        return true;
    }

    if(actionName === 'edit' ){
        return true;
    }

    if(actionName === 'new' || actionName === 'copy' ){
        return true;
    }

    if(actionName === 'delete'){
        return (target.state === TARGET_STATE_REJECTED.code || target.state === TARGET_STATE_CANCELLED.code);
    }
};


export const useTargetGeneralDTO = defineStore ('TargetDTOGeneral',  () => {
    const id=ref();
    const name=ref("");
    const creationDate=ref(0);
    const description=ref("");
    const referenceNumber=ref("");
    const runOnApproval=ref(false);
    const automatedQA=ref(false);
    const selectedUser=ref();
    const selectedState=ref(TARGET_STATE_APPROVED);
    const autoPrune=ref(false);
    const referenceCrawl=ref(false);
    const requestToArchivists=ref("");
    const nextStates=ref([]);

    const userProfile=useUserProfileStore();

    const initData=()=>{
        id.value=undefined;
        name.value="";
        creationDate.value=0;
        description.value="";
        referenceNumber.value="";
        runOnApproval.value=false;
        automatedQA.value=false;
        selectedUser.value={
            name: userProfile.currUserName,
            code: userProfile.name,
        };
        selectedState.value=TARGET_STATE_APPROVED;
        autoPrune.value=false;
        referenceCrawl.value=false;
        requestToArchivists.value="";
        nextStates.value=[];
    }

    const getData=()=>{
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
            autoPrune:autoPrune.value,
            referenceCrawl:referenceCrawl.value,
            requestToArchivists:requestToArchivists.value,
        }
    }

    const setData=(data:any)=>{
        id.value=data.id;
        name.value=data.name;
        creationDate.value=data.creationDate;
        description.value=data.description;
        referenceNumber.value=data.referenceNumber;
        runOnApproval.value=data.runOnApproval;
        automatedQA.value=data.automatedQA;
        selectedUser.value={
            name:getPresentationUserName(data.owner),
            code:data.owner,
        };
        selectedState.value={
            code:data.state,
            name:formatTargetState(data.state),
        };
        autoPrune.value=data.autoPrune;
        referenceCrawl.value=data.referenceCrawl;
        requestToArchivists.value=data.requestToArchivists;

        // const rest: UseFetchApis=useFetch();
        // rest.get('targets/nextStates/'+data.id).then((rsp:any)=>{
        //     console.log(rsp);
        // }).catch((err:any)=>{
    
        // });
    }

    return {id,name,creationDate,description,referenceNumber,runOnApproval,automatedQA,selectedUser,selectedState,autoPrune,referenceCrawl,requestToArchivists,initData,getData,setData};
});