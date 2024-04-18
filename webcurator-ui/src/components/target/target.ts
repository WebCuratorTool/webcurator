import { ref, reactive, computed } from 'vue';

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


export interface TargateGeneralDTO{
    id?: number,
    creationDate?: number,
    name?: string,
    description?: string,
    referenceNumber?: string,
    runOnApproval?: boolean,
    automatedQA?: boolean,
    owner?: string,
    state?: number,
    autoPrune?: boolean,
    referenceCrawl?: boolean,
    requestToArchivists?: string,
}

// export const 
// export const emptyTargetDTO=reactive({
//     general:
// });