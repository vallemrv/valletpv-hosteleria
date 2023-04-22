<template>
    <div>
      <div v-for="(pregunta, index) in preguntas" :key="index">
        <p :class="preguntaColor">{{ pregunta }}</p>
        <div v-if="respuestas[index]">
          <component :is="componenteRespuesta(respuestas[index].tipo)" :respuesta="respuestas[index]"/>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "ChatComponent",
    props: {
      preguntas: {
        type: Array,
        required: true,
      },
      respuestas: {
        type: Array,
        required: true,
      },
    },
    computed: {
      preguntaColor() {
        return "blue"; // elige el color que quieras para las preguntas
      },
    },
    methods: {
      componenteRespuesta(tipo) {
        // define el componente que se utilizará para mostrar la respuesta según el tipo
        switch (tipo) {
          case "texto":
            return "RespuestaTexto";
          case "tabla":
            return "RespuestaTabla";
          case "imagen":
            return "RespuestaImagen";
          default:
            return "RespuestaTexto";
        }
      },
    },
    components: {
      RespuestaTexto: {
        props: {
          respuesta: {
            type: Object,
            required: true,
          },
        },
        template: '<p>{{ respuesta.texto }}</p>',
      },
      RespuestaTabla: {
        props: {
          respuesta: {
            type: Object,
            required: true,
          },
        },
        template: '<table><tr v-for="fila in respuesta.filas"><td v-for="columna in fila">{{ columna }}</td></tr></table>',
      },
      RespuestaImagen: {
        props: {
          respuesta: {
            type: Object,
            required: true,
          },
        },
        template: '<img :src="respuesta.url" :alt="respuesta.descripcion" />',
      },
    },
  };
  </script>
  