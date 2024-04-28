import { ref, reactive, computed } from 'vue';
import { defineStore } from 'pinia';
import {useUserProfileStore, useUsersStore, getPresentationUserName} from '@/stores/users';

const STATE_MAP=["Pending","Reinstated","Nominated","Rejected","Approved", "Cancelled", "Completed"];
export const stateList=computed(()=>{
    const ary=[];
    for(let i=0; i<STATE_MAP.length; i++){
        ary.push({
            "name": STATE_MAP[i],
            "code": i+1,
        })
    }
    return ary;
});

export const formatTargetState = (state:number|any) => {
    //console.log(state);
    const placeHolder='Select a state';
    
    if(typeof state === 'undefined'){
        return placeHolder;
    }

    if(typeof state === 'number'){
        if(state>0 && state<=STATE_MAP.length){
            return STATE_MAP[state - 1];
        }else{
            return placeHolder;
        }
    }else{
        return state.name;
    }
};

const userProfile=useUserProfileStore();

export const useTargetGeneralDTO = defineStore ('TargetDTO',  () => {
    const id=ref();
    const name=ref("");
    const creationDate=ref(0);
    const description=ref("");
    const referenceNumber=ref("");
    const runOnApproval=ref(false);
    const automatedQA=ref(false);
    const selectedUser=ref();
    const selectedState=ref();
    const autoPrune=ref(false);
    const referenceCrawl=ref(false);
    const requestToArchivists=ref("");

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
        selectedState.value={
            name: 'Approved',
            code: 5,
        };
        autoPrune.value=false;
        referenceCrawl.value=false;
        requestToArchivists.value="";
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
    }

    return {id,name,creationDate,description,referenceNumber,runOnApproval,automatedQA,selectedUser,selectedState,autoPrune,referenceCrawl,requestToArchivists,initData,getData,setData};
});