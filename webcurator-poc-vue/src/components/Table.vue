<template>
  <table class="table table-bordered align-middle">
    <thead>
      <tr>
        <th scope="col" v-for="field in fields" :key="field.value">
          {{ field.display }}
        </th>
      </tr>
    </thead>
    <tbody>
      <tr class="table-row" v-for="item in data" :key="item" @click="onClick(item.id)">
        <td v-for="field in fields" :key="field.value">
          {{ item[field.value] }}
        </td>
      </tr>
    </tbody>
  </table>
</template>
  
<script setup lang="ts">
  import { ref } from 'vue'
  
  interface Field {
    display: string;
    value: string;
  }
  
  interface Props {
    fields: Field[],
    data: any[],
  }
  
  const props = defineProps<Props>()

  const emit = defineEmits(['on-click'])

  const onClick = (value: any) => {
    emit('on-click', value)
  }
  
  const fields = ref(props.fields)
  const data = ref(props.data)
  
</script>
  
<style>
table{
    font-size: small;
}

.table-row {
    cursor: pointer;
}

.table-row:hover {
    background-color: lightgray;
}

table .subtitle{
    color: #8d8888;
    font-size: smaller;
}
</style>