<template>
    <MainToolBar />
    <v-main>
        <v-container >
            <v-card>
                <v-card-title class="pa-0">
                    <v-card-title class="pa-0">
                        <v-toolbar dark>
                            <v-toolbar-title dark>Secciones</v-toolbar-title>
                            <v-spacer></v-spacer>
                            <v-btn icon v-if="storeSecciones.items.length < 6" @click="addSeccion">
                                <v-icon>mdi-plus</v-icon>
                            </v-btn>
                            <v-btn icon v-if="seccionSel" @click="upSeccion">
                                <v-icon>mdi-pencil</v-icon>
                            </v-btn>

                        </v-toolbar>
                    </v-card-title>

                </v-card-title>
                <v-card-text>
                    <v-item-group selected-class="bg-primary" v-model="seccionSelected">
                        <v-row class="mt-2">
                            <v-col cols="4" v-for="(item, index) in   storeSecciones.items  " :key="index">
                                <v-item :value="item.id" v-slot="{ isSelected, selectedClass, toggle }">
                                    <v-card :class="['d-flex align-center ', selectedClass]"
                                        :style="{ backgroundColor: isSelected ? '' : item.color }" dark height="90"
                                        style="width: 100%;" @click="(e) => { toggle(e); sel_seccion(item, isSelected) }">

                                        <!-- Si item.icono está presente, muestra la imagen -->
                                        <div class="text-center flex-grow-1">
                                            <img v-if="item.icono && item.icono.length > 0" width="40" height="40"
                                                :src="item.icono_url" class="mx-3" :alt="item.icono">
                                        </div>

                                        <div class="text-h5 flex-grow-1 text-center d-none d-lg-flex">
                                            {{ item.nombre }}
                                        </div>
                                    </v-card>
                                </v-item>
                            </v-col>
                        </v-row>

                    </v-item-group>
                </v-card-text>
            </v-card>

            <v-card elevation="4" class="mt-2">
                <v-card-title class="pa-0">
                    <v-toolbar dark>
                        <v-toolbar-title v-if="nivel == 0" dark>Productos</v-toolbar-title>
                        <v-toolbar-title v-else=dark>Suproductos de: {{ teclaSel ? teclaSel.nombre : "" }}</v-toolbar-title>
                        <v-spacer></v-spacer>
                        <div v-if="seccionSel">
                            <v-btn icon v-if="storeTeclas.items.length < 18" @click="addTecla">
                                <v-icon>mdi-plus</v-icon>
                            </v-btn>
                            <v-btn icon v-if="teclaSel" @click="rmTecla">
                                <v-icon>mdi-minus</v-icon>
                            </v-btn>
                            <v-btn icon v-if="teclaSel" @click="upTecla">
                                <v-icon>mdi-pencil</v-icon>
                            </v-btn>
                            <v-btn v-if="nivel > 0" icon @click="upLevel">
                                <v-icon>mdi-stairs-up</v-icon>
                            </v-btn>
                        </div>
                    </v-toolbar>

                </v-card-title>


                <v-card flat class="pa-2">
                    <v-row>
                        <v-col cols="4" v-for="(item, index) in storeTeclas.items " :key="index">
                            <v-card class="pa-2 d-flex align-center "
                                :style="{ backgroundColor: getBackgroud(item), width: '100%' }" height="90"
                                @click="sel_tecla(item)">

                                <div class="text-h5 text-sm-subtitle-1 flex-grow-1 text-center">
                                    {{ item.nombre }}
                                    <p class="text-h6" v-if="item.p1">{{ item.p1 }} €</p>

                                </div>

                                <v-badge v-if="showDown(item)" color="red" overlap bordered class="pa-2">
                                    <template v-slot:badge>
                                        {{ item.child }}
                                    </template>
                                    <v-btn icon @click.stop="downLevel">
                                        <v-icon>mdi-file-multiple</v-icon>
                                    </v-btn>
                                </v-badge>
                                <v-badge v-else color="red" overlap bordered class="pa-2">
                                    <template v-slot:badge>
                                        {{ item.child }}
                                    </template>
                                    <v-icon>mdi-folder-multiple-outline</v-icon>
                                </v-badge>
                            </v-card>
                        </v-col>
                    </v-row>
                </v-card>
            </v-card>
        </v-container>
        <DialogFormDinamico ref="editDialog" @save="guardarItem" />
        <DialogConfirm ref="confirmDialog" @result="confirm" />
        <v-snackbar v-model="snackbar" color="error" timeout="3000" top>
            {{ snackbarText }}
        </v-snackbar>
    </v-main>
</template>

<script>
import MainToolBar from "@/components/tools/MainToolBar.vue";
import { EmpresaStore } from '@/stores/empresaStore';
import { ConfigStore } from '@/stores/configStore';
import { SeccionesStore } from '@/stores/teclados/secciones';
import { TeclasStore } from '@/stores/teclados/teclas';
import DialogFormDinamico from '@/components/dialogs/DialogFormDinamico.vue';
import DialogConfirm from '@/components/dialogs/DialogConfirm.vue';
import { ref, watch } from 'vue';

export default {
    props: {
        seccion_id: {
            type: String,
            default: null,
        },
        tecla_id: {
            type: String,
            default: null,
        },
        nivel: {
            type: [Number, String],
            default: 0,

        },

    },
    components: {
        DialogFormDinamico,
        DialogConfirm,
        MainToolBar,
    },
    setup(props) {
        const empresasStore = EmpresaStore();
        const configStore = ConfigStore();
        const storeSecciones = SeccionesStore();
        const storeTeclas = TeclasStore();
        const seccionSelected = ref(null);
        const seccionSel = ref(null);
        const teclaSel = ref(null);

        const load_init = async () => {
            await storeSecciones.load(empresasStore);
            storeTeclas.loadFamilias(empresasStore);
            if (props.seccion_id) {
                seccionSelected.value = Number(props.seccion_id);
                seccionSel.value = storeSecciones.items.find((item) => item.id == props.seccion_id);
                if (props.nivel > 0) {
                    let tecla = await storeTeclas.getItemByID(props.tecla_id);
                    await storeTeclas.setParent(tecla);
                    teclaSel.value = tecla
                } else {
                    await storeTeclas.setSeccion(props.seccion_id);
                    if (props.tecla_id) {
                        teclaSel.value = storeTeclas.items.find((item) => item.id == props.tecla_id);
                    }
                }
            }
        }

        watch(() => empresasStore.empresa, async (empresa) => {
            if (empresa) {
                load_init()
            }
        });


        setTimeout(() => {
            if (empresasStore.empresa && storeSecciones.items.length == 0) load_init()
        }, 500);

        return {
            empresasStore,
            configStore,
            storeSecciones,
            storeTeclas,
            seccionSelected,
            seccionSel,
            teclaSel,
            nivel: parseInt(props.nivel),
        };
    },
    data() {
        return {
            store: null,
            tecladoSel: null,
            isNew: false,
            teclaSelChild: null,
            snackbar: false,
            snackbarText: "",
        }
    },
    methods: {
        getBackgroud(item) {
            let itemSel = this.nivel > 0 ? this.teclaSelChild : this.teclaSel;
            return itemSel && itemSel.id == item.id ? 'blue' : item.familia.color
        },
        showDown(item) {
            //Si en nivel cero comparamas el item con teclaSel si no con teclaSelChild
            let itemSel = this.nivel == 0 ? this.teclaSel : this.teclaSelChild;
            return itemSel && itemSel.id == item.id;
        },
        async downLevel() {
            this.nivel += 1;
            if (this.nivel > 1)
                this.teclaSel = this.teclaSelChild;
            await this.storeTeclas.setParent(this.teclaSel);
            this.teclaSelChild = null;
            this.$router.push("/teclados/" + this.seccionSel.id + "/" + this.teclaSel.id + "/" + this.nivel);
        },
        async upLevel() {
            this.nivel -= 1;
            if (this.nivel == 0) {
                this.teclaSelChild = null;
                await this.storeTeclas.setSeccion(this.seccionSel.id);
            } else {
                let tecla = await this.storeTeclas.getItemByID(this.teclaSel.parent);
                this.teclaSelChild = this.teclaSel;
                this.teclaSel = tecla;
                await this.storeTeclas.setParent(this.teclaSel);
            }
            this.$router.push("/teclados/" + this.seccionSel.id + "/" + this.teclaSel.id + "/" + this.nivel);
        },
        sel_tecla(item) {
            if (this.seccionSel == null) {
                this.goTo("/teclados/");
                this.storeTeclas.items = [];
                return;
            } else if (this.nivel > 0) {
                this.teclaSelChild = item;
                return;
            }
            this.teclaSel = item;
            this.goTo("/teclados/" + this.seccionSel.id + "/" + item.id);
        },
        sel_seccion(item, isSelected) {
            this.teclaSel = null;
            this.teclaSelChild = null;
            this.nivel = 0;
            if (!isSelected) {
                this.seccionSel = item;
                this.storeTeclas.setSeccion(item.id);
                this.goTo("/teclados/" + item.id);
            } else {
                this.seccionSel = null;
                this.goTo("/teclados");
                this.storeTeclas.setSeccion(null);
            }
        },
        goTo(route) {
            this.$router.replace(route);
        },
        rmTecla() {
            let itemSel = this.nivel == 0 ? this.teclaSel : this.teclaSelChild;
            if (!itemSel) return;
            this.$refs.confirmDialog.openDialog("¿Desea eliminar la tecla " + itemSel.nombre + "?",
                itemSel);
            if (this.nivel == 0)
                this.goTo("/teclados/" + this.seccionSel.id);

        },
        upTecla() {
            this.isNew = false;
            let itemSel = this.nivel == 0 ? this.teclaSel : this.teclaSelChild;
            this.store = this.storeTeclas;
            this.$refs.editDialog.openDialog(itemSel,
                "Modificar una Tecla",
                this.storeTeclas.fields);
        },
        async confirm(result, item) {
            if (result) {
                const error = await this.storeTeclas.delete(item);
                if (error) {
                    this.snackbar = true;
                    this.snackbarText = error;
                }
                if (this.nivel == 0) this.teclaSel = null;
                else this.teclaSelChild = null;
            }
        },
        addTecla() {
            this.isNew = true;
            this.store = this.storeTeclas;
            this.$refs.editDialog.openDialog(this.storeTeclas.newItem,
                "Crear una Tecla",
                this.storeTeclas.fields);
        },
        addSeccion() {
            this.isNew = true;
            this.store = this.storeSecciones;
            this.$refs.editDialog.openDialog(this.storeSecciones.newItem,
                "Crear una Seccion",
                this.storeSecciones.fields);
        },
        upSeccion() {
            this.isNew = false;
            this.store = this.storeSecciones;
            this.$refs.editDialog.openDialog(this.seccionSel,
                "Ecitar Seccion",
                this.storeSecciones.fields,
                storage,
                "/resources/logos/");
        },
        async guardarItem(item) {
            let error = null;
            if (this.store) {
                if (this.store == this.storeTeclas) {
                    if (this.nivel > 0) {
                        this.teclaSelChild = item;
                    } else {
                        this.teclaSel = item;
                    }
                } else {
                    this.seccionSel = item;
                }
                if (this.isNew) {
                    error = await this.store.add(item);

                } else {
                    error = await this.store.update(item);
                }
                if (error) {
                    this.snackbar = true;
                    this.snackbarText = error;
                }
            }
        },
    },
};
</script>

<style scoped>
.v-col {
    display: flex;
    justify-content: center;
    align-items: center;
}

.overflow-x-auto {
    overflow-x: auto;
}
</style>
