<template>
  <ion-header>
      <ion-toolbar color="pink">
        <ion-title>
            <span v-if="empresa"> {{empresa.nombre}} </span>
            <span v-else>ValleRCP</span>
           </ion-title>
        <ion-buttons slot="end">
            <ion-button @click="on_click_tools('settings')">
                <ion-icon slot="icon-only" :icon="listOutline"></ion-icon>
            </ion-button>
            <ion-button  @click="on_click_tools('server')">
                <ion-icon slot="icon-only" :icon="settingsOutline"></ion-icon>
            </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
</template>

<script lang="ts">
import { IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonIcon } from '@ionic/vue'
import { settingsOutline, listOutline } from 'ionicons/icons'
import { mapState } from 'vuex';
import { defineComponent } from 'vue';

export default defineComponent({
    components:{IonHeader, IonToolbar, IonTitle, IonButton, IonButtons, IonIcon},
    data(){
        return {
            settingsOutline,
            listOutline,
        }
    },
    methods:{
        on_click_tools(op){
            this.$emit("clickTools", op);
        }
    },
    computed:{
        ...mapState(["empresa"]),
    },
   
});
</script>