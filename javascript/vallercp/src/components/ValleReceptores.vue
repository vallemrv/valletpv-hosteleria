<template>
  <ion-popover :is-open="show">
       <ion-content>
        <ion-toolbar color="dark">
        <ion-title>Servidor</ion-title>
        </ion-toolbar>
        <ion-item v-for="(item, i) in items" :key="i">
             <ion-label>{{item.Nombre}}</ion-label>
            <ion-toggle
                slot="end"
                @update:modelValue="item.isChecked = $event"
                :modelValue="item.isChecked"
                @click="value_change(item)">
            </ion-toggle>
        </ion-item>
        <ion-toolbar>
            <ion-buttons slot="end">
            <ion-button @click="on_aceptar">Aceptar</ion-button>
        </ion-buttons>
        </ion-toolbar>
        
    </ion-content>
  </ion-popover>
</template>


<script>
import { mapState } from 'vuex'
import { IonContent, IonPopover,
        IonButton, IonTitle, IonToggle,
        IonLabel, IonButtons, 
        IonItem, IonToolbar,
        } from '@ionic/vue';
export default {
    props:["show", "items"],
    components: {
        IonPopover,
        IonContent,
        IonButton,
        IonTitle,
        IonLabel,
        IonButtons,
        IonItem,
        IonToolbar,
        IonToggle,
    },
    data() {
        return {
            receptores: [],
        }
    },
    computed: {
        ...mapState(["receptores_sel"])
    },
     methods: {
        on_aceptar(){
            this.$store.state.receptores_sel = Object.values(this.receptores);
            this.$emit("close", false);
        },
        value_change(item){
            if (item.isChecked){
                this.receptores = Object.values(this.receptores).filter(e => e.ID != item.ID)
            }else{
                this.receptores.push(item.ID);
            }
        }
    },
    watch:{ 
        receptores_sel(v){
           if (!v) this.receptores = v
        }
    }
}
</script>