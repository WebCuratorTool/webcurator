<script setup lang="ts">
import WctPrimaryButton from '@/components/WctPrimaryButton.vue';
import { useLoginStore } from '@/utils/rest.api';

const loginStore = useLoginStore();

// const checked = ref(false);

const login = () => {
  loginStore.authenticate();
};
</script>

<template>
  <div class="surface-ground flex flex-col items-center justify-center min-h-screen min-w-screen overflow-hidden">
    <div class="flex flex-col items-center justify-center gap-4" style="width: 30rem; overflow: hidden">
      <div class="text-center mb-5">
        <img src="@/assets/new_logo_WCT.png" alt="Image" height="150" class="mb-3" />
        <div class="text-600 font-medium mb-3">Sign in to continue</div>
      </div>

      <Message v-if="!loginStore.feedback.ok" severity="error" icon="pi pi-exclamation-triangle" :life="5000">
        {{ loginStore.feedback.title + ':' + loginStore.feedback.detail }}
      </Message>

      <InputText v-model="loginStore.username" placeholder="username" class="w-full" autocomplete="off" type="text" />

      <InputText v-model="loginStore.password" placeholder="password" id="password" class="w-full" autocomplete="off" type="password" />

      <WctPrimaryButton label="Sign In" class="mt-4 w-full" @click="login" fluid />

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
