<template>
  <ion-header>
      <ion-toolbar color="pink">
        <ion-title>ValleRCP 
            <span v-if="empresa"> {{empresa.nombre}} </span>
           </ion-title>
        <ion-buttons slot="end">
            <ion-button>
                <ion-icon slot="icon-only" :icon="listOutline"></ion-icon>
            </ion-button>
            <ion-button>
                <ion-icon slot="icon-only" :icon="settingsOutline"></ion-icon>
            </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
</template>

<script lang="ts">
import { IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonIcon } from '@ionic/vue'
import { settingsOutline, listOutline } from 'ionicons/icons'
import { mapState, mapActions } from 'vuex';
import { defineComponent } from 'vue';

export default defineComponent({
    name:"ValleHeader",
    components:{IonHeader, IonToolbar, IonTitle, IonButton, IonButtons, IonIcon},
    data(){
        return {
            settingsOutline,
            listOutline,
        }
    },
    computed:{
        ...mapState(["empresa"]),
    },
    methods:{
        ...mapActions(["getEmpresa"])

    },
    mounted(){
        localStorage.server = "192.168.1.168:8000"
        this.getEmpresa()
    }
});
</script>