<script lang="ts">
export default {
  emits: ["setFlagValue"],
  props: ["optionValue"],
  data () {
    return {
      value:  {f_oid:0, f_name: 'None', f_color: 'color: #000000' },
      options: [
          {f_oid:0, f_name: 'None', f_color: 'color: #000000' },
          {f_oid:1, f_name: 'Primary', f_color: 'color: #2a10e8' },
          {f_oid:2, f_name: 'Danger', f_color: 'color: #fa0d0d' },
          {f_oid:3, f_name: 'Warn', f_color: 'color: #f7ff00' },
          {f_oid:4, f_name: 'Succeed', f_color: 'color: #1df261' }
      ],
    }
  },
  methods: {
    isValued(){
      return this.value!==null;
    },

    getValue () {
      return this.value;
    }
  }
}
</script>
<template>
    <div class="btn-group">
      <div class="btn-group dropdown-label" data-bs-toggle="dropdown" aria-expanded="false" data-bs-reference="parent" contenteditable="true">
        <span v-show="isValued"><i class="bi bi-flag-fill" v-bind:style="value.f_color"></i> &ensp; {{ value.f_name }}</span>
      </div>
      <ul class="dropdown-menu">
        <li v-for="(option, index) in options">
          <a v-if="option.f_oid===0"  class="dropdown-item" href="#" @click="value=option"> <i class="bi bi-slash-circle"  v-bind:style="option.f_color"></i> &ensp; {{ option.f_name }}</a>
          <a v-else class="dropdown-item" href="#" @click="value=option"> <i class="bi bi-flag-fill"  v-bind:style="option.f_color"></i> &ensp; {{ option.f_name }}</a>
        </li>
      </ul>
    </div>
</template>
<style>
  .btn-group{
    width: 100%;
    font-size: small;
  }
  
  .btn-group ul{
    width: 100%;
    /* margin-left: 5px; */
  }

  .dropdown-label{
    border: 0.5px solid;
    padding: 4px 8px;
  }

  .dropdown-label:after{
    display: inline-block;
    position: absolute;
    content: url("../assets/icons/angle-down-solid.svg");
    height: 12px;
    width: 12px;
    right: 5px;
    top: 5px;
  }

</style>