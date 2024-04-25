import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useUsersStore = defineStore('users', () => {
  const userList = ref([]);
  const currUser=ref({
    oid: -1,
    name: '',
    firstName: '',
    lastName: '',
  });

  const currUserName=computed(()=>currUser.value.firstName + " " + currUser.value.lastName + "(" + currUser.value.name + ")");
  return { userList, currUser, currUserName }
});
