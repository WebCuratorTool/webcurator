<script lang="ts">
    import QueueFilterVue from '@/components/queue/QueueFilter.vue';
    import QueueSearchVue from '@/components/queue/QueueSearch.vue';
    import QueueResultVue from '@/components/queue/QueueResult.vue';

    export default{
        components:{QueueSearchVue, QueueFilterVue, QueueResultVue},
        emits:["resultTable"],
        data() {
            return {
                
            }
        },
        methods: {
            searchQueue(searchCondition: any){
                console.log(searchCondition);
                this.$refs.resultTable.getData(searchCondition);
            },
            filter(){
                console.log("Filtering");
            },           
            filterResults(filterCondition: any){
                console.log(filterCondition);
                this.$refs.resultTable.filterTable(filterCondition);
            }
        }
    }
    
</script>

<template>
    <div class="page-title">Queue</div>
    <div class="page-container">
        <div class="search-title">Query</div>
        <QueueSearchVue @btnSearch="searchQueue"/>
        <br/>
        <QueueFilterVue @btnFilter="filterResults" />
        <br/>
        <QueueResultVue ref="resultTable"/>
    </div>
</template>

<style scoped>

</style>