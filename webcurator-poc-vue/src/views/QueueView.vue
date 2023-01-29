<script setup lang="ts">
    import QueueFilterVue from '@/components/queue/QueueFilter.vue';
    import QueueSearchVue from '@/components/queue/QueueSearch.vue';
    import QueueResultVue from '@/components/queue/QueueResult.vue';
    // export default{
    //     data() {
    //         return {
                
    //         }
    //     },
    //     methods: {
    //         searchQueue(searchConditionData){
    //             console.log(searchConditionData);
    //         },
    //         filter(){
    //             console.log("Filtering");
    //         }
    //     }
    // }
    function searchQueue(searchConditionData){
        console.log(searchConditionData);
    }
</script>

<template>
    <div class="page-title">Queue</div>
    <div class="queue-search">
        <div class="search-title">Query</div>
        <QueueSearchVue @btnSearch="searchQueue"/>
        <br/>
        <QueueFilterVue />
        <br/>
        <QueueResultVue />
    </div>

    
</template>

<style scoped>

</style>