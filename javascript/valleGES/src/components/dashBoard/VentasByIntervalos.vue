<template>
    <v-card>
      <v-card-title>Ventas por intervalos de tiempo</v-card-title>
      <v-card-text v-if="!error">
        <v-dialog v-model="datePickerDialog" max-width="400px">
        <template v-slot:activator="{ props }">
          <v-text-field
            v-model="selectedDate"
            label="Selecciona una fecha"
            prepend-icon="mdi-calendar"
            readonly
            v-bind="props"
          ></v-text-field>
        </template>
        <VueDatePicker v-model="formattedSelectedDate" inline  auto-apply
            @update:model-value="updateData"
            :enable-time-picker="false" 
            ></VueDatePicker>
        
         </v-dialog>

         <v-switch
            class="pa-0 ma-0"
            v-model="switchEstado"
            :label="`Mostrar: ${estado === 'C' ? 'T. Nulo' : 'T. Cobrado'}`"
            @change="updateData"
          ></v-switch>

         <v-table>
          <template v-slot:default>
            <thead>
              <tr>
                <th>Inicio</th>
                <th>Fin</th>
                <th>Ventas</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(interval, index) in intervalos" :key="index">
                <td>{{ interval.inicio }}</td>
                <td>{{ interval.fin }}</td>
                <td>{{ parseFloat(interval.ventas).toFixed(2) }} €</td>
              </tr>
            </tbody>
          </template>
        </v-table>
      </v-card-text>
      <v-card-text v-else>
        <v-alert dense outlined type="error">
          {{ error }}
        </v-alert>
      </v-card-text>
    </v-card>
  </template>
  
  <script>
  import VueDatePicker from '@vuepic/vue-datepicker';
  import '@vuepic/vue-datepicker/dist/main.css'
  import { DashBoard } from "@/stores/dashBoard";

  
  export default{
    components:{ VueDatePicker },
    props: ["empresa"],
    data() {
      return {
        error: null,
        intervalos: [],
        selectedDate: new Date().toISOString().split("T")[0],
        datePickerDialog: false,
        switchEstado: false
      };
    },
    computed:{
      estado() {
         return this.switchEstado ? "A" : "C";
      },
      formattedSelectedDate: {
          get() {
              return this.formatDate(this.selectedDate);
          },
          set(date) {
            this.selectedDate = this.formatDate(date);
          },
      },
    },
    methods:{
        formatDate(date) {
            var d = new Date(date),
            month = '' + (d.getMonth() + 1),
            day = '' + d.getDate(),
            year = d.getFullYear();

            if (month.length < 2) 
                month = '0' + month;
            if (day.length < 2) 
                day = '0' + day;

            return [year, month, day].join('/');
        },     
        async updateData(){
            try {
                const store = DashBoard();
                this.datePickerDialog = false;
                const {data, error} = await store.getVentasByIntervalos(this.formattedSelectedDate, this.estado);
                if (error) {
                    this.error = error;
                    return;
                }
                this.intervalos = data;
            } catch (error) {
                console.error(error);
            }
        }
    },
    async mounted() {
      await this.updateData();
    },
  };
  </script>
  