<script lang="ts">
    import { RouterLink, RouterView } from 'vue-router'
    import {TabulatorFull as Tabulator} from 'tabulator-tables'; //import Tabulator library

    function flagFormatter(cell, formatterParams, onRendered){
        var flag = cell.getValue();
        if(!flag){
            return '';
        }

        var colorValue = flag.rgb;
        console.log(colorValue);
        return '<i class="bi bi-flag-fill" style="color: #' + colorValue + ';"></i>';
    }

    function idFormatter(cell, formatterParams, onRendered){
        var oid=cell.getValue();
        // var routerTo={name:'target',params:{oid:oid}};
        // var cellContent="<RouterLink to='" + JSON.stringify(routerTo) + "'>" + oid +"</RouterLink>";
        // var routerTo='{name:"target",params:{oid:oid}}';
        // var cellContent="<RouterLink :to='" + routerTo + "'>" + oid +"</RouterLink>";
        var cellContent="<a href='#/target?oid=19'>xx</a>";
        return cellContent;
    }

    function targetFormatter(cell, formatterParams, onRendered){
        var targetName=cell.getValue();
        var rowData=cell.getData();
        var ownerName=rowData.ownerNiceName;

       return targetName;
    }
    function startTimeFormatter(cell, formatterParams, onRendered){
        var colorValue = cell.getValue();
        console.log(colorValue);
        return '<i class="bi bi-flag-fill" style="color: ' + colorValue + ';"></i>';
    }
    export default{
        data() {
            return {
                tabulator: null, //variable to hold your table
                tableData: [{"oid":19,"flagged":false,"flag":{"oid":3,"name":"Success","rgb":"49e821","complementRgb":"c021e8"},"targetOid":12,"targetName":"RNZ NEWs","sortOrderDate":"2023-01-29T03:03:15.611+00:00","state":"Harvested","ownerOid":1,"ownerNiceName":"F. Lee","statusElapsedTime":320287,"statusDataDownloadedString":"4.12 MB"},{"oid":14,"flagged":false,"flag":{"oid":2,"name":"Danger","rgb":"e61751","complementRgb":"17e6ac"},"targetOid":12,"targetName":"RNZ NEWs","sortOrderDate":"2023-01-28T10:56:18.922+00:00","state":"Aborted","ownerOid":1,"ownerNiceName":"F. Lee","statusElapsedTime":0,"statusDataDownloadedString":"0 bytes"},{"oid":17,"flagged":false,"flag":null,"targetOid":12,"targetName":"RNZ NEWs","sortOrderDate":"2023-01-28T11:01:39.548+00:00","state":"Aborted","ownerOid":1,"ownerNiceName":"F. Lee","statusElapsedTime":330447,"statusDataDownloadedString":"4.3 MB"}],
            }
        },
        mounted() {
            //instantiate Tabulator when element is mounted
            this.tabulator = new Tabulator(this.$refs.table, {
                data: this.tableData, //link data to table
                reactiveData:true, //enable data reactivity
                layout:"fitDataStretch",
                columns: [
                    {formatter:"rowSelection", titleFormatter:"rowSelection", headerSort:false, cellClick:function(e, cell){cell.getRow().toggleSelect();}},
                    {title:'<i class="bi bi-flag-fill"></i>',field:"flag",width:50, sorter:"string", formatter: flagFormatter, formatterParams:{type:"bar"}},
                    {title:"ID", field:"oid", sorter:"number",formatter:idFormatter, width:50},
                    {title:"Target <br/> <span style='color:#C0C0C0;'>(Owner)</span>", field:"targetName", formatter: targetFormatter, formatterParams:{type:"bar"}, hozAlign:"left", width:250},
                    {title:"Start time <br/> <span style='color:#C0C0C0;'>(Runtime)</span>", field:"sortOrderDate", sorter:"string", width:250, editor:true},
                    {title:"nr URL's <br/> <span style='color:#C0C0C0;'>(Crawlers)</span>", field:"age", sorter:"number", hozAlign:"right", formatter:"progress"},
                    {title:"Data loaded <br/> <span style='color:#C0C0C0;'>(Files failed)</span>", field:"statusDataDownloadedString", sorter:"date", hozAlign:"center"},
                    {title:"State <br/> <span style='color:#C0C0C0;'>(QA recom)</span>", field:"state", hozAlign:"center"},
                    {title:"Action", field:"action", hozAlign:"center"},
                ], //define table columns
            });
        },
        methods: {            
            async getData(searchCondition:{}) {
                console.log("get Data");
                var reqData={
                    method: 'POST', // or 'PUT'
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    mode: 'cors',
                    body: JSON.stringify(searchCondition),
                }
                const res = await fetch("http://localhost:8080/wct/api/1.0/queue",reqData);
                const finalRes = await res.json();
                this.setData(finalRes);
            },
            setData(tableData: never[]){
                this.tableData=tableData;
            },
            filterTable(filterCondition){
                console.log(filterCondition);
            },
            openTargetInstance(tiOid){
                this.$router.push({name:'target_instance',params:{oid:tiOid}});
            }


        }
    }
</script>

<template>
     <div class="row justify-content-between">
        <div class="col-6 text-start">Results: {{ tableData.length }} Target instances</div>
        <div class="col-6 text-end">Thumbnails</div>
    </div>
    <div class="row mt-0">

    </div>
    
    <div ref="table"></div>
    <!-- <div>This is the results</div> -->
</template>

<style scoped>
.filter-btn{
    display: inline;
}

.queue-filter-btn-group span{
    display: inline-block;
    margin: 0 0.5em;
    padding: 0.3em 0.5em;
    background: #E2E2E2;
    border: solid 2px;
    border-color: #C6C6C6;
    color: #212121;
    border-radius: 1em;
    font-size: 0.75em;
}
.queue-filter-btn-group :first-child{
    margin: 0 0.5em;
}

.queue-filter-btn-group a{
    text-decoration: none;
    color: #212121;
}

.queue-filter-operation a{
    display: inline-block;
    margin: 0.5em;
    padding: 0.3em 0.5em;
    font-size: 0.8em;
    font-weight: bold;
    text-decoration: none;
    color: #28639A;
}
.queue-filter-operation .icon{
    font-size: 1em;
}

.row{
    margin: 0;
    padding: 0;
}

</style>
  