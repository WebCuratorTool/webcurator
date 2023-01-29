<script lang="ts">
    export default{
        data() {
            return {
                filterBtnNextTwoMonths: true,
                filterBtnAllActive: true,
                filterBtnOwnedByMe: true,
            }
        },
        methods: {
            reset(){
                this.filterBtnNextTwoMonths=true;
                this.filterBtnAllActive=true;
                this.filterBtnOwnedByMe=true;
            },
            filter(){
                console.log("Filtering");
            }
        }
    }
</script>

<template>
    <div class="row queue-filter">
        <div class="col-12">
            <span class="queue-filter-btn-group">
                <span v-show="filterBtnNextTwoMonths"> In the next two months &ensp;<a @click="filterBtnNextTwoMonths=!filterBtnNextTwoMonths" href="#"><i class="fa-solid fa-xmark"></i></a> </span>
                <span v-show="filterBtnAllActive"> All Actions &ensp;<a @click="filterBtnAllActive=!filterBtnAllActive" href="#"><i class="fa-solid fa-xmark"></i></a> </span>
                <span v-show="filterBtnOwnedByMe"> Owned by me &ensp;<a @click="filterBtnOwnedByMe=!filterBtnOwnedByMe" href="#"><i class="fa-solid fa-xmark"></i></a> </span>
            </span>

            <span class="queue-filter-operation">
                <a @click="reset()" href="#"><i class="bi bi-x-lg icon"></i>&nbsp; Reset filter</a>
                <a @click="filter()" href="#"><i class="bi bi-filter icon"></i>&nbsp;Filter</a>
            </span>
        </div>
    </div>
   
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
  