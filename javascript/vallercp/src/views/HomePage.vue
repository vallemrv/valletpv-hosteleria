<template>
  <ion-page>
    <valle-header></valle-header>
    <ion-router-outlet></ion-router-outlet>
  </ion-page>
</template>

<script lang="ts">
import  ValleHeader  from '@/components/ValleHeader';
import {  IonPage, IonRouterOutlet } from '@ionic/vue';
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'HomePage',
  components: {
    IonPage,
    IonRouterOutlet,
    ValleHeader,
  }
});
</script>

