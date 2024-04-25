import { ref, reactive, computed } from 'vue';
import { defineStore } from 'pinia';

export const target=reactive({
    targetList: [],
    selectedTargetId: -1,
    selectedTarget: null,
    openMode:"new",
    readOnly:false,
});


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

export const formatTargetState = (stateCode:number) => {
    if(stateCode>0 && stateCode<=STATE_MAP.length){
        return STATE_MAP[stateCode - 1];
    }else{
        return 'unknown';
    }
};

export const getTargetState=()=>{
    if(target.selectedTarget !== null){
        const stateCode=target.selectedTarget["state"];
        return formatTargetState(stateCode);
    }
    return 'unknown';
};

export const getTargetSubTitle=()=>{
    if(target.selectedTarget !== null){
        return target.selectedTarget["id"] + "-" + target.selectedTarget["creationDate;"] 
    }
    return "New";
};



export const useTargetGeneralDTO = defineStore ('TargetDTO',  () => {
    const id=ref();
    const name=ref("");
    const creationDate=ref(0);
    const description=ref("");
    const referenceNumber=ref("");
    const runOnApproval=ref(false);
    const automatedQA=ref(false);
    const selectedUser=ref();
    const selectedState=ref(1);
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
        selectedUser.value="";
        selectedState.value=1;
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
            owner: selectedUser.value,
            state: selectedState.value,
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
        selectedUser.value=data.owner;
        selectedState.value=data.state;
        autoPrune.value=data.autoPrune;
        referenceCrawl.value=data.referenceCrawl;
        requestToArchivists.value=data.requestToArchivists;
    }

    return {id,name,creationDate,description,referenceNumber,runOnApproval,automatedQA,selectedUser,selectedState,autoPrune,referenceCrawl,requestToArchivists,initData,getData,setData};
});