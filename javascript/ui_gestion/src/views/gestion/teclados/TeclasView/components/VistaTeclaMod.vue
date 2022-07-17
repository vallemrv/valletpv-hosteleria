<template>
   <v-col>
      <valle-listados-tb
        :items="listTeclas"
        :columns="tabla.keys"
        :headers="tabla.headers"
        :tools="tools"
        @click_tools="on_click_tools"
        :tb_name="tb_name"
    ></valle-listados-tb>
   </v-col>
   
</template>


<script>
import ValleListadosTb from '@/components/ValleListadosTb.vue'
export default {
    props:["listTeclas"],
    components:{ ValleListadosTb },
    data() {
         const col = ["nombre", "p1", "p2"];
        return {
            tb_name:"teclas",
            tabla: {
                headers: col,
                keys: col,
            },
            tools: [
                { op: "edit", text: "Editar", icon: "mdi-account-edit" },
                { op: "rm", text: "Borrar", icon: "mdi-delete" },
                { op: "sec", text: "Modificar secciones", icon: "mdi-pencil" },
                { op: "add_sub", text: "Agregar subtecla", icon: "mdi-plus" },
            ],
        }
    },
    methods:{
        on_click_tools(v, op) {
           this.$emit("click_tools", v, op);
        },
    }
}
</script>