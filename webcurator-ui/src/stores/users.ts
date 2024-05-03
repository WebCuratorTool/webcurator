import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import { type UseFetchApis, useFetch } from '@/utils/rest.api';


const KEY_USER_PROFILE = 'wct-user-profile';
export const useUserProfileStore = defineStore('userProfile', () => {
  const token = ref('');
  const id = ref(-1);
  const name = ref('');
  const firstName = ref('');
  const lastName = ref('');
  const agency = ref('');
  const isActive = ref(true);
  const roles = ref([]);
  const priviledges = ref([]);

  const currUserName = computed(() => firstName.value + " " + lastName.value + "(" + name.value + ")");

  const load = () => {
    const cachedContent = localStorage.getItem(KEY_USER_PROFILE);
    if (!cachedContent) {
      return;
    }

    const p = JSON.parse(cachedContent);
    token.value = p.token;
    id.value = p.id;
    name.value = p.name;
    firstName.value = p.firstName;
    lastName.value = p.lastName;
    agency.value = p.agency;
    isActive.value = p.isActive;
    roles.value = p.roles;
    priviledges.value = p.priviledges;
  }

  const save = () => {
    const data = {
      token: token.value,
      id: id.value,
      name: name.value,
      firstName: firstName.value,
      lastName: lastName.value,
      agency: agency.value,
      isActive: isActive.value,
      roles: roles.value,
      priviledges: priviledges.value,
    }

    const cachedContent = JSON.stringify(data);
    localStorage.setItem(KEY_USER_PROFILE, cachedContent);
  }

  const clear = () => {
    token.value = '';
    id.value = -1;
    name.value = '';
    firstName.value = '';
    lastName.value = '';
    agency.value = '';
    isActive.value = true;
    roles.value = [];
    priviledges.value = [];
    localStorage.removeItem(KEY_USER_PROFILE);
  }

  const setBasicData = (user: any) => {
    id.value = user.id;
    name.value = user.name;
    firstName.value = user.firstName;
    lastName.value = user.lastName;
    agency.value = user.agency;
    isActive.value = user.isActive;
    roles.value = user.roles;
    save();
  }

  const setToken = (currName: string, currToken: string) => {
    name.value = currName;
    token.value = currToken;
    save();
  }

  load();

  return { token, id, name, firstName, lastName, roles, agency, priviledges, currUserName, setBasicData, setToken, clear }
});


export const useUsersStore = defineStore('users', () => {
  const data = ref([]);
  const initialFetch = () => {
    const userProfile = useUserProfileStore();
    const rest: UseFetchApis = useFetch();
    rest.get("users").then((rsp: any) => {
      data.value = rsp["users"];

      for (var i = 0; i < data.value.length; i++) {
        var user: any = data.value[i];
        if (user.name === userProfile.name) {
          userProfile.setBasicData(user);
        }
      }
    }).catch((err: any) => {
      console.log(err.message);
    });
  }

  const userList = computed(() => {
    const formatedData = [];
    for (var i = 0; i < data.value.length; i++) {
      var user: any = data.value[i];
      formatedData.push({
        "name": user.firstName + " " + user.lastName + " (" + user.name + ")",
        "code": user.name,
      });
    }
    return formatedData;
  });
  return { data, userList, initialFetch }
});


export const getPresentationUserName = (selectedUser: string | any) => {
  if (!selectedUser) {
    console.log('The input selectedUser is ' + selectedUser);
    return "";
  }

  let userName = '';
  if (typeof selectedUser === 'string') {
    userName = selectedUser;
  } else {
    userName = selectedUser.code;
  }

  const users = useUsersStore();
  const data = users.data;
  for (var i = 0; i < data.length; i++) {
    var user: any = data[i];
    if (user.name === userName) {
      return user.firstName + " " + user.lastName + " (" + user.name + ")";
    }
  }
  return "";
}