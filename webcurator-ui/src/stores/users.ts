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
        var user:any=data.value[i];
        if(user.name === userProfile.name){
          userProfile.setBasicData(user);
        }
      }
    }).catch((err:any)=>{
        console.log(err.message);
    });
  }

  const userList=computed(()=>{
    const formatedData=[];
    for(var i=0; i<data.value.length; i++){
        var user:any=data.value[i];
        formatedData.push({
            "name": user.firstName + " " + user.lastName + " (" + user.name + ")",
            "code": user.name,
        });
    }
    return formatedData;
  });



  return {data, userList, initialFetch}
});


export const getPresentationUserName=(selectedUser:string | any)=>{
  if(!selectedUser){
    console.log('The input selectedUser is ' + selectedUser);
    return "";
  }

  let userName='';
  if(typeof selectedUser === 'string'){
    userName=selectedUser;
  }else{
    userName=selectedUser.code;
  }

  const users=useUsersStore();
  const data=users.data;
  for(var i=0; i<data.length; i++){
    var user:any=data[i];
    if(user.name === userName){
      return user.firstName + " " + user.lastName + " (" + user.name + ")";
    }
  }
  return "";
}