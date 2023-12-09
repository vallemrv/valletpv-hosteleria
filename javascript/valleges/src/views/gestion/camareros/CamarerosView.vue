<template>
  <ValleHeader :title="title" anchor="bottom center" :btns="btns"/>
  <v-container>
    <Camareros v-if="vista == 'edicion'" />
    <CamarerosPase v-else-if="vista == 'pase'"/>
    <ValleDialogoForm   ref="dialogForm" :form="form" 
                        tb_name="camareros"
                        :item="item"
                        tipo="add" title="Agregar camarero"/>

   </v-container>
</template>


<script>
import { mapGetters, mapState } from "vuex";
import ValleHeader from "@/components/ValleHeader.vue";
import ValleDialogoForm from "@/components/ValleDialogoForm.vue";
import Camareros from "@/views/gestion/camareros/components/Camareros.vue";
import CamarerosPase from "@/views/gestion/camareros/components/CamarerosPase.vue";

export default {
    components:{ ValleHeader, ValleDialogoForm, Camareros, CamarerosPase },
    data() {
        return {
            title: "Camareros",
            vista: "edicion",
            showDialogo: false,
            item: {},
            btns:[
                {icon: "mdi-pencil", op: "show-edit", callback: this.op_cam},
                {icon: "mdi-key", op: "show-pase", callback: this.op_cam},
                {icon: "mdi-plus", op: "add-cam", callback: this.op_cam}
            ]
        }
    },
    computed:{
    ...mapGetters(["getListValues"]),
    ...mapState(["permisoschoices"]),
    form() {
      return [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "apellidos", label: "1º Apellido", tp: "text" },
        {
          col: "permisos",
          label: "Permisos",
          choices: this.getListValues("permisoschoices", "choices"),
          tp: "multiple",
        },
      ];
    },
    },
    methods:{
        on_close_add_cam(){
            this.showDialogo = false
        },
        op_cam(op){
          switch(op){
              case "add-cam":
                  this.item = {}
                  this.$ref.dialogForm.show_dialogo(true)
                  break;
              case "show-pase":
                  this.vista = "pase";
                  break;
               case "show-edit":
                  this.vista = "edicion";
                  break;
          }
        }
    }
}
</script>