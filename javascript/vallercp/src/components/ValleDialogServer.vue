<template>
<ion-popover :is-open="show">
    <ion-content>
        <ion-toolbar color="dark">
        <ion-title>Servidor</ion-title>
        </ion-toolbar>
        <div>
            <ion-item>
                <ion-label position="floating">Url servidor</ion-label>
                <ion-input v-model="str_servidor"></ion-input>
            </ion-item>
        </div>
        <ion-toolbar>
            <ion-buttons slot="end">
            <ion-button @click="on_aceptar">aceptar</ion-button>
        </ion-buttons>
        </ion-toolbar>
        
    </ion-content>
</ion-popover>
</template>


<script lang="ts">

import { IonContent, IonPopover,
        IonButton, IonInput, IonTitle, 
        IonLabel, IonButtons, 
        IonItem, IonToolbar,
        } from '@ionic/vue';
export default {
    props: ["show"],
    components: {
        IonPopover,
        IonContent,
        IonButton,
        IonInput,
        IonTitle,
        IonLabel,
        IonButtons,
        IonItem,
        IonToolbar,
    },
    computed:{
        str_servidor:{
            get(){ 
                if (localStorage.server)
                return localStorage.server;
                else return "";
            },
            set(v){ 
               localStorage.server = v
            } 
        }
    },
    methods: {
        on_aceptar(){
            localStorage.receptore = null;
            this.$store.state.receptores_sel = [];    
            if (localStorage.server == ""){
                localStorage.server = null;
            }
            this.$emit("close", false);
        }
    },
   
}
</script>


<style scoped>
ion-content div {
  --padding-bottom: 10px;
  --padding-end: 10px;
  --padding-start: 20px;
  --padding-top: 20px;
}
</style>