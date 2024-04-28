<script setup lang="ts">
import { ref, inject, computed, onMounted, onBeforeMount } from "vue";
import {useUsersStore} from '@/stores/users';
import FormField from '@/components/WctFormField.vue';
import {useTargetGeneralDTO, stateList, formatTargetState} from '../../stores/target';


const fields=useTargetGeneralDTO();
const users=useUsersStore();
</script>

<template>

</template>

<style>

</style>