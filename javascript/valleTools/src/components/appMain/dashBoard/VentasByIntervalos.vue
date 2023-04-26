<template>
    <v-card>
      <v-card-title>Ventas por intervalos de tiempo</v-card-title>
      <v-card-text>
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
    </v-card>
  </template>
  
  <script>
  import axios from "axios";
  import VueDatePicker from '@vuepic/vue-datepicker';
  import '@vuepic/vue-datepicker/dist/main.css'

  
  export default {
    components:{ VueDatePicker },
    props: ["empresa"],
    data() {
      return {
        intervalos: [],
        selectedDate: new Date().toISOString().split("T")[0],
        datePickerDialog: false,
      };
    },
    computed:{
        formattedSelectedDate: {
            get() {
                return this.formatDate(this.selectedDate);
            },
            set(date) {
                this.selectedDate = date.toISOString().split("T")[0];
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

                this.datePickerDialog = false;
                const params = new FormData()
                params.append("user", this.empresa.token.user)
                params.append("token", this.empresa.token.token)
                params.append("date", this.formattedSelectedDate)
                const response = await axios.post(this.empresa.url + "/app/dashboard/ventas_por_intervalos", params);
                this.intervalos = response.data;
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
  