<template>
    <MainToolbar title="Precios" />
    <EditFloatVue />
</template>

<script>
import EditFloatVue from "@/components/tools/EditFloat.vue";
import MainToolbar from "@/components/tools/MainToolbar.vue";
export default{
    components: {
        EditFloatVue
    }, 
}
</script>