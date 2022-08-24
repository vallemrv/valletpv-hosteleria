<template>
   <v-toolbar>
            <v-btn
                icon
                class="hidden-xs-only" 
                @click="$router.back()"
            >
                <v-icon>mdi-arrow-left</v-icon>
            </v-btn>
        <v-toolbar-title>{{title}}</v-toolbar-title>
         <div class="text-center" v-if="ocupado" >
            <v-progress-circular indeterminate color="primary"></v-progress-circular>
         </div>
   </v-toolbar>
</template>


<script>
import { mapState } from 'vuex';
export default {
    props:["title"],
    computed: {
        ...mapState(["ocupado"])
    }
}
</script>