<template>
     <v-navigation-drawer
        v-model="drawer"
        bottom
        temporary
      >
        <v-list
          :items="items"
        ></v-list>
      </v-navigation-drawer>
</template>

<script>
export default {
    props:["drawer"],
    data(){
        return {
            items: [
        {
          title: 'Foo',
          value: 'foo',
        },
        {
          title: 'Bar',
          value: 'bar',
        },
        {
          title: 'Fizz',
          value: 'fizz',
        },
        {
          title: 'Buzz',
          value: 'buzz',
        },
      ],
        }
    }
}
</script>