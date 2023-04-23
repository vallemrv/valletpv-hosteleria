<template>
    <div>
      <v-list two-line>
        <v-list-item v-for="(item, index) in items" :key="index">
         
            <v-card
              elevation="3"
              class="ma-2 pa-2"
              :class="{ 'text-primary text-left' : item.type === 'question', 'text-success text-right': item.type === 'answer' }"
            >
              <v-card-text class="text-h6">
                {{ item.text }}
                <v-container fluid v-if="item.table">
                  <v-table>
                    <template v-slot:default>
                      <thead>
                        <tr>
                          <th class="text-center" v-for="(header, index) in item.table.headers" :key="index">
                            {{ header }}
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(row, rowIndex) in item.table.data" :key="rowIndex">
                          <td class="text-center" v-for="(cell, cellIndex) in row" :key="cellIndex">
                            {{ cell }}
                          </td>
                        </tr>
                      </tbody>
                    </template>
                  </v-table>
                </v-container>
                <v-container class="text-center" fluid v-if="item.image">
                  <v-img :src="item.image" max-width="300px" max-height="200px" contain></v-img>
                </v-container>
              </v-card-text>
            </v-card>

        </v-list-item>
      </v-list>
    </div>
  </template>
  
  <script>
  import { useChatStore } from "@/stores/chatStore";

  export default {
    setup() {
    const chatStore = useChatStore();
     return {
        items: chatStore.items,
        };
    },
  };
  </script>
  
  <style scoped>
  .text-primary {
    color: #1976d2 !important; /* Personaliza el color de las preguntas aquí */
  }
  
  .text-success {
    color: #4caf50 !important; /* Personaliza el color de las respuestas aquí */
  }
  </style>
  