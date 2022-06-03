<template>
   <v-toolbar>
            <v-btn
                icon
                class="hidden-xs-only" 
                @click="$router.back()"
            >
                <v-icon>mdi-arrow-left</v-icon>
            </v-btn>
        <v-title>{{title}}</v-title>
   </v-toolbar>
</template>


<script>
export default {
    props:["title"]
}
</script>