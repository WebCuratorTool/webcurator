<script lang="ts">
    import SelectorFlagVue from '@/components/SelectorFlag.vue';

    export default{
        components:{
            "selectorflag": SelectorFlagVue
        },
        emits: ["btnSearch"],
        data() {
            return {
                instanceId: "",
                targetName: "",
                flag: {},
                multiSelectAction:[],
            }
        },
        methods: {
            search(){
                let flagOptionProxy = this.$refs.selectorFlag.getValue();
                let searchCondition={
                    'targetInstanceId':this.instanceId,
                    'targetName':this.targetName,
                    'flagId':flagOptionProxy.f_oid,
                }
                
                this.$emit('btnSearch',searchCondition);

                console.log("Triggered emit from search component");
            },
            filter(){
                console.log("Filtering");
            },
            customLabel ({ title, desc }) {
                console.log(title);
                console.log(desc);
               return `${title}/${desc}`
            }
        },
        mounted() {
            // this.$refs.flagSelector.focus();
        }
    }
</script>

<template>
    <div class="row">
        <div class="col-2">
            <label for="instanceId" class="form-label">Instance ID</label>
        </div>
        <div class="col-2">
            <label for="targetName" class="form-label">Target name</label>
        </div>
        <div class="col-2">
            <label for="actions" class="form-label">Flag</label>
        </div>
        <div class="col-4">
            <label for="actions" class="form-label">Multi-select action</label>
        </div>
        <div class="col-2"></div>
    </div>
    <div class="row">
        <div class="col-2">                
            <input v-model="instanceId" type="text" class="form-control form-control-sm" id="instanceId" placeholder="Instance ID">
        </div>
        <div class="col-2">
            <input v-model="targetName" type="text" class="form-control form-control-sm" id="targetName" placeholder="Target name">
        </div>
        <div class="col-2">
            <selectorflag ref="selectorFlag"/>
        </div>
        <div class="col-3">                
            <select class="form-select form-select-sm" id="actions">
                <option selected>Open this select menu</option>
                <option value="1">dg</option>
                <option value="2"><i class="fa-solid fa-flag"></i>Two</option>
                <option value="3">&#xf024;v-html="<span style='color: black'>Primary</span>" </option>
            </select>
        </div>
        <div class="col-1">
            <button type="button" class="btn btn-outline-primary btn-sm" id="btn-achive">Archive</button>
        </div>
        <div class="col-2">
            <div class="d-flex justify-content-end">
                <button type="button" class="btn btn-primary btn-sm" id="btn-action" @click="search">Search &ensp;<i class="bi bi-search"></i></button>
            </div>
        </div>
    </div>
</template>

<style scoped>
    .row{
        /* background: #985fac; */
        margin: 0;
        padding: 0;
    }

    input,select{
        border: 1px solid;
    }
    input:focus,
    select:focus {
        border-color: inherit;
        -webkit-box-shadow: none;
        box-shadow: none;
    }
    /* .btn{
        width: 100%;
    } */

</style>
  