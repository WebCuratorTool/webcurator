<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router';
import { ref, watch, shallowRef, onBeforeMount } from "vue";
import { getRouteURLByName } from '@/utils/helper';
import { useUsersStore } from '@/stores/users';
import { useAgenciesStore } from '@/stores/agencies';
import TargetList from './TargetList.vue';
import TargetTabView from './TargetTabView.vue';

const current = shallowRef(TargetList);

const props = ref({
    page: 'TargetTabView',
    mode: 'new',
    id: 0,
});

const users = useUsersStore();
const agencies = useAgenciesStore();

const route = useRoute();
const router = useRouter();

watch(() => router.currentRoute.value.path, (to, from) => {
    console.log('to: ' + to);
    if (route.name === 'target-list') {
        current.value = TargetList;
        props.value.page = 'TargetList';
    } else if (route.name === 'target-tabview-new') {
        current.value = TargetTabView;
        props.value.page = 'TargetTabView';
        props.value.mode = 'new';
    } else if (route.name === 'target-tabview-exist') {
        current.value = TargetTabView;
        props.value.page = 'TargetTabView';
        props.value.mode = route.params.mode.toString();
        props.value.id = Number(route.params.id.toString());
    }
}, { immediate: true });

const popPage = (options: any) => {
    let url = '/';
    if (options.page === 'TargetList') {
        current.value = TargetList;
        url = getRouteURLByName('target-list');
    } else {
        props.value = options;
        current.value = TargetTabView;

        if (options.mode === 'new') {
            url = getRouteURLByName('target-tabview-new');
        } else {
            url = getRouteURLByName('target-tabview-exist', options);
        }
    }
    history.pushState({}, '', url);
}

onBeforeMount(() => {
    agencies.initialFetch();
    users.initialFetch();
});
</script>

<template>
    <KeepAlive include="TargetList">
        <component :is="current" :props="props" @popPage="popPage"></component>
    </KeepAlive>
</template>

<style></style>