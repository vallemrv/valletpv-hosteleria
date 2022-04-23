<template>
    <v-menu v-model="showMod" 
            anchor="bottom end" origin="auto"
    >
      <template v-slot:activator="{ props }">
          <v-btn stacked v-bind="props">
            <v-badge
              color="info"
              :content="num_inst"
            >
              <v-icon icon="mdi-inbox-arrow-down"></v-icon>
            </v-badge>
          </v-btn>
      </template>
    
    </v-menu>
</template>


<script>
export default{
    props: ["num_inst"]
}
</script>