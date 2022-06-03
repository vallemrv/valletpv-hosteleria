<template>
     <v-navigation-drawer
        v-model="drawer"
        bottom
        temporary
      >
        <v-list>
          <v-list-item v-for="(item, i) in items" 
                    :key="i" 
                    :value="item" 
                    @click="on_click_item(item.value)">
              <v-list-item-avatar start v-if="item.icon">
                <v-icon :icon="item.icon"></v-icon>
              </v-list-item-avatar>
              <v-list-item-title v-text="item.label"></v-list-item-title>  
          </v-list-item>
        </v-list>
      </v-navigation-drawer>
</template>

<script>
export default {
    props:["drawer", "items"],
    data(){
        return {
            
        }
    },
    methods: {
      on_click_item(v){
       this.$emit("click_item", v);
      }
    },
}
</script>