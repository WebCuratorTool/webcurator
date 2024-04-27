import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import {type UseFetchApis, useFetch} from '@/utils/rest.api';

export const useUserProfileStore = defineStore ('userProfile', ()=>{
  const id=ref(-1);
  const name=ref('');
  const firstName=ref('');
  const lastName=ref('');
  const agency=ref('');
  const isActive=ref(true);
  const roles=ref([]);
  const priviledges=ref([]);

  const currUserName=computed(()=>firstName.value + " " + lastName.value + "(" + name.value + ")");

  const setBasicData=(user:any)=>{
    id.value=user.id;
    firstName.value=user.firstName;
    lastName.value=user.lastName;
    agency.value=user.agency;
    isActive.value=user.isActive;
    roles.value=user.roles;
  }

  return {id,name,firstName,lastName,roles,agency,priviledges,currUserName,setBasicData}
});
const userProfile=useUserProfileStore();

export const useUsersStore = defineStore('users', () => {
  const data = ref([]);
  const initialFetch=()=>{
    const rest: UseFetchApis=useFetch();
    rest.get("users").then((rsp:any)=>{
      data.value=rsp["users"];
      
      for(var i=0; i<data.value.length; i++){
        var item:any=data.value[i];
        if(item.name === userProfile.name){
          userProfile.setBasicData(item);
        }
      }
    }).catch((err:any)=>{
        console.log(err.message);
    });
  }

  const userList=computed(()=>{
    const formatedData=[];
    for(var i=0; i<data.value.length; i++){
        var item=data.value[i];
        formatedData.push({
            "name": item["firstName"] + " " + item["lastName"] + " (" + item["name"] + ")",
            "code": item["name"],
        });
    }
    return formatedData;
  });

  return {data, userList, initialFetch}
});
