<script lang="ts">
    import { RouterLink, RouterView } from 'vue-router'
    // import {TabulatorFull as Tabulator} from 'tabulator-tables'; //import Tabulator library

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
        var cellContent="<a href='/target/19'>xx</a>";
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
                allRowSelected:false,
                tabulator: null, //variable to hold your table
                tableData: [
                    {"rowSelection":false,"oid":19,"flagged":true,"flag":{"oid":3,"name":"Success","rgb":"49e821","complementRgb":"c021e8"},"targetOid":12,"targetName":"RNZ NEWs","sortOrderDate":"2023-01-29T03:03:15.611+00:00","state":"Harvested","ownerOid":1,"ownerNiceName":"F. Lee","statusElapsedTime":320287,"statusDataDownloadedString":"4.12 MB"},
                    {"rowSelection":false,"oid":14,"flagged":true,"flag":{"oid":2,"name":"Danger","rgb":"e61751","complementRgb":"17e6ac"},"targetOid":12,"targetName":"RNZ NEWs","sortOrderDate":"2023-01-28T10:56:18.922+00:00","state":"Aborted","ownerOid":1,"ownerNiceName":"F. Lee","statusElapsedTime":0,"statusDataDownloadedString":"0 bytes"},
                    {"rowSelection":false,"oid":17,"flagged":false,"flag":null,"targetOid":12,"targetName":"RNZ NEWs","sortOrderDate":"2023-01-28T11:01:39.548+00:00","state":"Aborted","ownerOid":1,"ownerNiceName":"F. Lee","statusElapsedTime":330447,"statusDataDownloadedString":"4.3 MB"}
                ],
            }
        },
        mounted() {
            // var searchCondition={};
            // this.getData(searchCondition);
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
                const res = await fetch("http://localhost:8080/wct/api/v1/targets",reqData);
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
                this.$router.push({name:"target_instance",query:{tiOid:tiOid}});
                // this.$router.push({name:"target_instance",params:{oid:tiOid}});
            },
            openTarget(targetOid){
                this.$router.push({name:"target",query:{oid:targetOid}});
            },
            
        },
        computed: {
            cellFlagFormatter(rowData){
                return (rowData) => (rowData.flagged? '<i class="bi bi-flag-fill" style="color:  #' + rowData.flag.rgb + ';"></i>' : '');
            },
            toHref(rowData){
                return (rowData) => ('/target/' + rowData.oid);
            },
        }
    }
</script>

<template>
     <div class="row justify-content-between">
        <div class="col-6 text-start">Results: {{ tableData.length }} Target instances</div>
        <div class="col-6 text-end">Thumbnails</div>
    </div>
    <div class="row">
        <table class="table table-bordered align-middle">
            <thead>
                <tr>
                    <th scope="col">
                        <input class="form-check-input" type="checkbox" v-model="allRowSelected">
                    </th>
                    <th scope="col"><i class="bi bi-flag-fill"></i></th>
                    <th scope="col">ID</th>
                    <th scope="col">Target <br/> <span class='subtitle'>(Owner)</span></th>
                    <th scope="col">Start time <br/> <span class='subtitle'>(Runtime)</span></th>
                    <th scope="col">nr URL's <br/> <span class='subtitle'>(Crawlers)</span></th>
                    <th scope="col">Data loaded <br/> <span class='subtitle'>(Files failed)</span></th>
                    <th scope="col">State <br/> <span class='subtitle'>(QA recom)</span></th>
                    <th scope="col">Action</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="rowData in tableData">
                    <th scope="row">
                        <input class="form-check-input" type="checkbox" v-model="rowData.rowSelection" />
                    </th>
                    <td>
                        <div v-if="rowData.flagged && rowData.flag != null">
                            <i class="bi bi-flag-fill" :style="{color: '#'+rowData.flag.rgb}"></i>
                        </div>
                        <!-- {{ cellFlagFormatter(rowData) }} -->
                    </td>
                    <td>
                        <!-- <a :href='toHref(rowData)'>{{ rowData.oid }}</a> -->
                        <a href="#" @click="openTargetInstance(rowData.oid)">{{ rowData.oid }}</a>
                    </td>
                    <td>
                        <a href="#" @click="openTarget(rowData.targetOid)">{{ rowData.targetName }}</a>
                        <br/>                        
                        <span class='subtitle'>{{ rowData.ownerNiceName }}</span>
                    </td>
                    <td>
                        {{ rowData.sortOrderDate }}
                        <br/>                        
                        <span class='subtitle'>{{ rowData.statusElapsedTime }}</span>
                    </td>
                    <td>
                        
                    </td>
                    <td>
                        {{ rowData.statusDataDownloadedString }}
                    </td>
                    <td>
                        {{ rowData.state }}
                    </td>
                    <td>
                        <i class="bi bi-three-dots-vertical"></i>
                    </td>
                </tr>                
            </tbody>
        </table>
    </div>
    
    <!-- <div ref="table"></div> -->
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

table{
    font-size: small;
}

table .subtitle{
    color: #8d8888;
    font-size: smaller;
}

</style>
  