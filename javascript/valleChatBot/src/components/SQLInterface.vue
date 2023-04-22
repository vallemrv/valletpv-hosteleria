<template>
    <v-app-bar app></v-app-bar>
    <v-main>
       <v-container class="fill-height" fluid>
        <v-row  justify="center">
            <v-col cols="12">
            <v-textarea
                v-model="query"
                auto-grow
                clearable
                label="Escriba su consulta SQL"
                outlined
                rows="10"
            ></v-textarea>
            <v-btn color="primary" @click="submitQuery">Enviar consulta</v-btn>
            
            </v-col>
            <v-col cols="12">
                <v-table>
                <template v-slot:default>
                    <thead>
                    <tr>
                        <th class="text-center" v-for="header in columnHeaders" :key="header">{{ header }}</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="(row, rowIndex) in rows" :key="rowIndex">
                        <td v-for="header in columnHeaders" :key="header">{{  row[header] }}</td>
                    </tr>
                    </tbody>
                </template>
                </v-table>
            </v-col>
        </v-row>
    </v-container>

    </v-main>

   <v-footer app></v-footer>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    data() {
        return {
            query: '',
            result: null,
            columnHeaders: [],
            rows: [],
        };
        },
        methods: {
        async submitQuery() {
            if (!this.query) return;

            // Reemplaza esto con la URL de tu vista de Django que acepta SQL raw
            const url = 'http://localhost:8000/valleIA/sql_query_view/';
            
            
            try {
                var params = new FormData();
                params.append("sql_query", this.query);
                const response = await axios.post(url, params);
                if (response.data.columnas && response.data.valores) {
                    this.columnHeaders = response.data.columnas.map((columnName) => {
                        return columnName;
                    });
                    this.rows = response.data.valores.map((row) => {
                        const rowData = {};
                        response.data.columnas.forEach((columnName, index) => {
                           rowData[columnName] = row[index];
                        });
                        return rowData;
                    });
                    console.log(this.rows, this.columnHeaders)
                    this.query = "";
                    } else {
                    console.error(response.data);
                    }
                } catch (error) {
                    console.error('Error al enviar la consulta:', error);
                }
            },
        },

  };
  </script>
  