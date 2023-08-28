<script setup lang="ts">
import { defineComponent, reactive } from 'vue'
import { useAuthStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const { error } = storeToRefs(useAuthStore()) 
const { login } = useAuthStore()

  const form = reactive({
    username: '',
    password: ''
  })

  const onSubmit = () => {
    login(form.username, form.password)
    form.username = ''
    form.password = ''
  }
</script>
<template>
    <table style="width:100vw;">
      <tr>
        <!-- <td id="col-sidebar">
          <div class="sidebar h-100 ms-2">
            <img class="logo" src="../assets/wct_logo.png" height="75"/>
          </div>
        </td> -->
        <td id="col-content">
          <form @submit.prevent="onSubmit" class="w-25 mx-auto">
            <div class="form-group my-2">
              <label>Username</label>
              <input
                v-model="form.username"
                class="form-control"
                placeholder="Username"
                required
              />
            </div>
            <div class="form-group my-2">
              <label>Password</label>
              <input
                v-model="form.password"
                class="form-control"
                type="password"
                placeholder="Password"
                required
              />
            </div>
            <div class="text-danger my-2">{{ error }}</div>
            <button class="btn btn-success btn-block my-2" type="submit">Login</button>
          </form>
        </td>
      </tr>
    </table>
  </template>
