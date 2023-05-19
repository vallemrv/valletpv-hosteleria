<template>
    <v-table fixed-header> 
      <thead>
        <tr>
          <th
            v-for="(columna, index) in tabla.columnas"
            :key="index"
            class="text-center"
          >
            {{ columna }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(fila, i) in tabla.datos" :key="i">
          <td class="text-center" v-for="(item, j) in fila" :key="j">{{ item }}</td>
        </tr>
      </tbody>
    </v-table>
  </template>
  
  <script>
  export default {
    props: {
      tabla: {
        type: Object,
        required: true,
      },
    },
  }
  </script>
  