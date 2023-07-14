<template>
    <MainToolbar titulo="Modificar teclas">
        <v-icon class="pl-2" @click="$router.push('/modificar/precios')">mdi-cash</v-icon>
        <v-icon class="pl-2" @click="$router.push('/modificar/texto')">mdi-text-box-multiple-outline</v-icon>
        <v-icon class="pl-2" @click="$router.push('/modificar/tag')">mdi-magnify</v-icon>
    </MainToolbar>
    <v-main>
        <v-container>
            <BuscadorTeclas :titulo="datos ? datos.titulo: ''" />
            <v-expansion-panels class="mt-4">
                <v-expansion-panel v-for="item in store.items" :key="item.id" @click="fetchChildren(item)">
                    <v-expansion-panel-title>
                        <template v-slot:actions="{ expanded }">
                            <v-icon v-if="item.child > 0" :color="!expanded ? 'teal' : ''"
                                :icon="expanded ? 'mdi-pencil' : 'mdi-check'"></v-icon>
                        </template>
                        <v-row>
                            <v-col cols="12">
                                <v-toolbar :color="item.color">
                                    <v-toolbar-title>
                                        {{ item.nombre }}
                                    </v-toolbar-title>
                                </v-toolbar>
                            </v-col>
                            <v-col v-for="(field, ns) in datos.fields" :key="ns" cols="4">
                                <EditFloatVue :value="item" :field="field" :titulo="datos.labels[ns]"
                                    :type="datos.types[ns]" @change="change" />
                            </v-col>

                        </v-row>
                    </v-expansion-panel-title>
                    <v-expansion-panel-text v-if="item.child > 0">
                        <v-card v-for="(child, index) in children" :key="index">
                            <v-card-text>
                                <v-row>
                                    <v-col cols="12">
                                        <v-toolbar :color="child.color">
                                            <v-toolbar-title>
                                                {{ child.nombre }}
                                            </v-toolbar-title>
                                        </v-toolbar>
                                    </v-col>
                                    <v-col v-for="(field, ns) in datos.fields" :key="ns" cols="4">
                                        <EditFloatVue :value="child" :field="field" :titulo="datos.labels[ns]"
                                            :type="datos.types[ns]" @change="change" />
                                    </v-col>
                                </v-row>
                            </v-card-text>
                        </v-card>
                    </v-expansion-panel-text>
                </v-expansion-panel>
            </v-expansion-panels>
        </v-container>
    </v-main>
</template>

<script>
import EditFloatVue from "@/components/tools/EditFloat.vue";
import MainToolbar from "@/components/tools/MainToolbar.vue";
import { TeclasStore } from "@/stores/teclados/teclas.js";
import { EmpresaStore } from "@/stores/empresaStore.js";
import BuscadorTeclas from "../../components/tools/BuscadorTeclas.vue";
export default {
    props: {
        tipo: {
            type: String,
            default: "precios",
        },
    },
    setup() {
        const store = TeclasStore();
        const empresaStore = EmpresaStore();
        store.loadFamilias(empresaStore);
        return { store }
    },
    data() {
        return {
            children: [],
            precios: {
                titulo: 'Precios',
                fields: ['p1', 'p2', 'incremento'],
                labels: ['Precio 1ª tarifa', 'Precio 2ª tarifa', 'Incremento'],
                types: ['number', 'number', 'number']
            },

            texto: {
                titulo: 'Texto',
                fields: ['nombre', 'descripcion_t', 'descripcion_r'],
                labels: ['Nombre', 'Descripción ticket', 'Descripción receptor'],
                types: ['text', 'text', 'text']
            },
            tag: {
                titulo: 'Tag',
                fields: ['tag'],
                labels: ['Tag para buscar'],
                types: ['text']
            }
        }
    },
    components: {
        EditFloatVue,
        MainToolbar,
        BuscadorTeclas
    },
    computed: {
        datos() {
            const datos = this[this.tipo];
            if (this.store.empresaStore) {
                this.store.load({ parent_id: null });
            }
            return datos
        },
    },
    methods: {
        change(item) {
            this.store.update(item);
        },
        async fetchChildren(item) {
            if (item.child > 0) {
                this.children = await this.store.getChilds(item.id);
            }
        }
    },
    unmounted() {
        this.store.items = [];
    }
}
</script>