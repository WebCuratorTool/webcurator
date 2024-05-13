<template>
    <div class="nav-bar">
        <div class="nav-bar-inner w-full">
            <img class="logo" src="@/assets/wct_logo.png" />
            <div class="flex w-full justify-content-between">
                <div class="nav-bar-links">
                    <router-link class="nav-bar-link" to="/wct/dashboard">Dashboard</router-link>
                    <router-link class="nav-bar-link" to="/wct/targets">Targets</router-link>
                </div>
                <div class="nav-bar-links pr-4">
                    <Button label="Logout" @click="logout" />
                    <InputSwitch class="my-auto ml-4" :modelValue="layoutConfig.darkTheme.value" @update:modelValue="onDarkModeChange" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import {useUserProfileStore} from '@/stores/users';
import { useLayout } from '@/layout/composables/layout';
import { usePrimeVue } from 'primevue/config';

const { setScale, layoutConfig } = useLayout();
const $primevue = usePrimeVue();

const token = useUserProfileStore();
const logout = ()=>{
    token.clear();
}

const onDarkModeChange = (value: any) => {
    const newThemeName = value ? layoutConfig.theme.value.replace('light', 'dark') : layoutConfig.theme.value.replace('dark', 'light');

    layoutConfig.darkTheme.value = value;
    onChangeTheme(newThemeName, value);
};

const onChangeTheme = (theme: string, mode: any) => {
    $primevue.changeTheme(layoutConfig.theme.value, theme, 'theme-css', () => {
        layoutConfig.theme.value = theme;
        layoutConfig.darkTheme.value = mode;
    });
};

</script>
<style scoped>
.nav-bar{
    position: sticky;
    top: 0;
    height: 80px;
    width: 100%;
    background-color: #1F5384;
    z-index: 100;
}

.nav-bar-inner{
    height: 65%;
    margin: 0;
    position: absolute;
    top: 50%;
    -ms-transform: translateY(-50%);
    transform: translateY(-50%);
    display: flex;
}

.logo {
    height: 100%;
    padding-left: 4em;
    padding-right: 4em;
}

.nav-bar-links {
    display: flex;
    margin-top: auto;
    margin-bottom: auto;
}

.nav-bar-link {
    margin-right: 1em;
    font-size: large;
    text-decoration: none;
    color: #E4F0F5;
}
</style>