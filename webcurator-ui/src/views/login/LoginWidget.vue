<script setup lang="ts">
import { useApiAuthStore, usePageAuthStore, type LoginResponse } from '@/utils/rest.api';
import { ref } from 'vue';

const props = defineProps<{
  mode: string;
}>();

const authApi = useApiAuthStore();
const authPage = usePageAuthStore();

const username = ref();
const password = ref();
const feedback = ref<LoginResponse>({
  ok: true,
  title: '',
  detail: '',
  token: ''
});

const msgKey = ref(0);
const login = async () => {
  if (props.mode === 'page') {
    feedback.value = await authPage.login(username.value, password.value);
  } else {
    feedback.value = await authApi.authenticate(username.value, password.value);
  }
  msgKey.value += 1;
};
</script>

<template>
  <div class="surface-ground flex flex-col items-center justify-center min-h-screen min-w-screen overflow-hidden">
    <div class="flex flex-col items-center justify-center gap-4" style="width: 30rem; overflow: hidden">
      <div class="text-center mb-5">
        <img src="@/assets/new_logo_WCT.png" alt="Image" height="150" class="mb-3" />
        <div class="text-600 font-medium mb-3">Sign in to continue</div>
      </div>

      <Message v-show="!feedback.ok" :key="msgKey" severity="error" icon="pi pi-exclamation-triangle" :life="5000">
        {{ feedback.title + ':' + feedback.detail }}
      </Message>

      <form class="flex flex-col items-center justify-center gap-4 w-full">
        <InputText v-model="username" placeholder="username" class="w-full" autocomplete="off" type="text" />
        <InputText v-model="password" placeholder="password" id="password" class="w-full" autocomplete="off" type="password" />
      </form>

      <Button label="Sign In" class="wct-primary-button mt-4 w-full" @click="login" fluid />

      <Divider />
      <span>version: 3.2.1</span>
    </div>
  </div>
</template>

<style scoped>
.pi-eye {
  transform: scale(1.6);
  margin-right: 1rem;
}

.pi-eye-slash {
  transform: scale(1.6);
  margin-right: 1rem;
}

#password .p-inputtext {
  width: 10rem;
}
</style>
