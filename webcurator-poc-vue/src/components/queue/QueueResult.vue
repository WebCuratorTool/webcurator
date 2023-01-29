<script lang="ts">
    import {TabulatorFull as Tabulator} from 'tabulator-tables'; //import Tabulator library
    function flagFormatter(cell, formatterParams, onRendered){
        var colorValue = cell.getValue();
        console.log(colorValue);
        return '<i class="bi bi-flag-fill" style="color: ' + colorValue + ';"></i>';
    }
    function targetFormatter(cell, formatterParams, onRendered){
        var colorValue = cell.getValue();
        console.log(colorValue);
        return '<i class="bi bi-flag-fill" style="color: ' + colorValue + ';"></i>';
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
                tableData: [
                    {id:1, name:"Oli Bob", age:"12", flag:"red", dob:""},
                    {id:2, name:"Mary May", age:"1", flag:"blue", dob:"14/05/1982"},
                    {id:3, name:"Christine Lobowski", age:"42", flag:"green", dob:"22/05/1982"},
                    {id:4, name:"Brendon Philips", age:"125", flag:"orange", dob:"01/08/1980"},
                    {id:5, name:"Margret Marmajuke", age:"16", flag:"yellow", dob:"31/01/1999"},
                ],
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
                    {title:"ID", field:"id", sorter:"number", hozAlign:"left", width:50},
                    {title:"Target <br/> (Owner)", field:"target", sorter:"number", hozAlign:"left", width:150},
                    {title:"Start time <br/> (Runtime)", field:"name", sorter:"string", width:200, editor:true},
                    {title:"nr URL's <br/> (Crawlers)", field:"age", sorter:"number", hozAlign:"right", formatter:"progress"},
                    {title:"Data loaded <br/> (Files failed)", field:"dob", sorter:"date", hozAlign:"center"},
                    {title:"State <br/> (QA recom)", field:"dob", sorter:"date", hozAlign:"center"},
                    {title:"Action", field:"dob", sorter:"date", hozAlign:"center"},
                ], //define table columns
            });
        },
        methods: {
            setData(tableData: never[]){
                this.tableData=tableData;
            },
            async getData(searchCondition:Object) {
                var reqData={
                    method: 'POST', // or 'PUT'
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(searchCondition),
                }
                const res = await fetch("https://jsonplaceholder.typicode.com/posts",reqData);
                const finalRes = await res.json();
                this.setData(finalRes);
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
  