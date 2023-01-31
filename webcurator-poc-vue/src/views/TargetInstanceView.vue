<script lang="ts">
export default{
        data() {
            return {
                tiOid: -1,
                ti: {},
            }
        },
        mounted() {
          var tiOid = this.$route.query.tiOid;
          this.getData(tiOid);
        },
        methods: {            
            async getData(tiOid) {
                const res = await fetch("http://localhost:8080/wct/api/1.0/target_instance?tiOid=" + tiOid);
                const finalRes = await res.json();
                this.ti=finalRes;
            },           
        },
        computed: {

        }
    }
</script>

<template>
  <ul class="nav nav-tabs">
  <li class="nav-item">
    <a class="nav-link active" aria-current="page" href="#">General</a>
  </li>
  <li class="nav-item dropdown">
    <a class="nav-link" href="#">Used profile</a>
  </li>
  <li class="nav-item">
    <a class="nav-link" href="#">Logs</a>
  </li>
  <li class="nav-item">
    <a class="nav-link">Results</a>
  </li>
  <li class="nav-item">
    <a class="nav-link">Access</a>
  </li>
</ul>
</template>
