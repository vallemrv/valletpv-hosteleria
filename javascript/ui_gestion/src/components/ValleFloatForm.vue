<template>

     <v-menu
        transition="scale-transition"
        origin="center center"
        v-model="mostrar"
        >

        <template v-slot:activator="{ props }">
            <v-btn 
            :elevation="elevation"
            class="text-center"
            v-bind="props"
            @click.stop="() => {}">
               <v-row class="pa-0 ma-0">
                    <v-col cols="12" class="current_val"
                    > {{ value }} </v-col> 
                    <v-col cols="12" class="old_val" v-if="val_old"
                    > {{ val_old }} </v-col>  
                </v-row>   
            </v-btn>
        </template>
        <v-card
         width="200px">
            <v-card-header>
                  <v-text-field  
                        v-model="val_modified"
                        @click.stop=""
                        hide-details="auto"
                        :rules="rules"
                        @keypress.enter="on_enter()"
                        ></v-text-field>  
            </v-card-header>
        </v-card>
    </v-menu>    
</template>

<script>

import { mapActions } from "vuex"

export default {
    props:["item", "column", "rules", 
            "app", "tb_name", "value"],
    data: ()=>{
        return {
            elevation: 2,
            mostrar: false,
            val_modified: "",
            val_old: null
        }
    },
    computed:{
       
    },
    methods: {
        ...mapActions(["addInstruccion"]),
        on_enter(){
            this.val_old = this.item[this.column]
            this.item[this.column+"_old"] = this.val_old
            this.item[this.column] = this.val_modified
            this.mostrar = false
            let inst = {
                tb:this.tb_name,
                app:this.app,
                reg:{},
                tipo:"md",
                id:this.item.id
            }
            inst['reg'][this.column] = this.val_modified
            this.addInstruccion({inst:inst})   
           
        },
        
    },
    watch: {
        item(v){
           this.val_modified = this.item[this.column]
           this.val_old = this.item[this.column+"_old"]
        }
        
    },
    mounted() {
        this.val_modified = this.item[this.column]
    },
}
</script>

<style>
   .current_val{
       margin: 0;
       padding: 0;
   }
   .old_val{
       margin: 0;
       padding: 0;
       color: brown;
       font-size: smaller;
   }
</style>