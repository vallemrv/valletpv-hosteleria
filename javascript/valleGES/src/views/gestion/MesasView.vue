<template>
    <MainToolBar  titulo="Mesas"/>
    <v-main>
        <v-container>
            <v-card>
                <v-card-title class="pa-0">
                    <v-toolbar dark>
                        <v-toolbar-title dark>Zonas</v-toolbar-title>
                        <v-spacer></v-spacer>
                        <v-btn icon @click="addZona">
                            <v-icon>mdi-plus</v-icon>
                        </v-btn>
                        <v-btn icon v-if="zonaSel" @click="rmZona">
                            <v-icon>mdi-minus</v-icon>
                        </v-btn>
                        <v-btn icon v-if="zonaSel" @click="upZona">
                            <v-icon>mdi-pencil</v-icon>
                        </v-btn>
                        
                    </v-toolbar>
                </v-card-title>
                <v-card-text>
                    <v-item-group selected-class="bg-primary" v-model="zonaSelected">
                        <v-row class="mt-2">
                            <v-col cols="4" v-for="(item, index) in storeZonas.items" :key="index">
                                <v-item :value="item.id" v-slot="{ isSelected, selectedClass, toggle }">
                                    <v-card :class="['d-flex align-center ']"
                                        :style="{ backgroundColor: item.color }" dark height="90"
                                        style="width: 100%;" @click="(e) => { toggle(e); sel_zona(item, isSelected) }">
                                        <div class="text-h5 flex-grow-1 text-center ">
                                            {{ item.nombre }}
                                            <v-icon v-if="isSelected" color="green" >mdi-check</v-icon>
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
                        <v-toolbar-title>Mesas</v-toolbar-title>
                        <v-spacer></v-spacer>
                        <div v-if="zonaSel">
                            <v-btn icon @click="addMesa">
                                <v-icon>mdi-plus</v-icon>
                            </v-btn>
                            <v-btn icon v-if="mesaSel" @click="rmMesa">
                                <v-icon>mdi-minus</v-icon>
                            </v-btn>
                            <v-btn icon v-if="mesaSel" @click="upMesa">
                                <v-icon>mdi-pencil</v-icon>
                            </v-btn>
                        </div>
                    </v-toolbar>
                </v-card-title>

                <v-card flat class="pa-2">
                    <v-row>
                        <v-col cols="4" v-for="(item, index) in storeMesas.items" :key="index">
                            <v-card class="pa-2 d-flex align-center "
                                :style="{ backgroundColor: zonaSel.color, width: '100%' }" height="90"
                                @click="sel_mesa(item)">
                                <div class="text-h5 text-sm-subtitle-1 flex-grow-1 text-center">
                                    {{ item.nombre }}
                                    <v-icon color="green" v-if="mesaSel && mesaSel.id == item.id ">mdi-check</v-icon>
                                </div>
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
import { ZonasStore } from '@/stores/mesas/zonas';
import { MesasStore } from '@/stores/mesas/mesas';
import DialogFormDinamico from '@/components/dialogs/DialogFormDinamico.vue';
import DialogConfirm from '@/components/dialogs/DialogConfirm.vue';
import { ref, watch } from 'vue';

export default {
    props: {
        zona_id: {
            type: String,
            default: null,
        },
        mesa_id: {
            type: String,
            default: null,
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
        const storeZonas = ZonasStore();
        const storeMesas = MesasStore();
        const zonaSelected = ref(null);
        const zonaSel = ref(null);
        const mesaSel = ref(null);

        const load_init = async () => {
            await storeZonas.load(empresasStore);
            storeMesas.empresaStore = empresasStore;
            if (props.zona_id) {
                zonaSelected.value = Number(props.zona_id);
                zonaSel.value = storeZonas.items.find((item) => item.id == props.zona_id);
                await storeMesas.setZona(props.zona_id);
                if (props.mesa_id) {
                    mesaSel.value = storeMesas.items.find((item) => item.id == props.mesa_id);
                }
            }
        }

        setTimeout(() => {
            if (empresasStore.empresa && storeZonas.items.length == 0) load_init();
        }, 500);

        watch(() => empresasStore.empresa, async (empresa) => {
            if (empresa) {
                load_init();
            }
        });

        return {
            empresasStore,
            configStore,
            storeZonas,
            storeMesas,
            zonaSelected,
            zonaSel,
            mesaSel,
        };
    },
    data() {
        return {
            store: null,
            isNew: false,
            snackbar: false,
            snackbarText: "",
        }
    },
    methods: {
        sel_mesa(item) {
            if (this.zonaSel == null) {
                this.goTo("/mesas/");
                this.storeMesas.items = [];
                return;
            }
            this.mesaSel = item;
            this.goTo("/mesas/" + this.zonaSel.id + "/" + item.id);
        },
        sel_zona(item, isSelected) {
            this.mesaSel = null;
            if (!isSelected) {
                this.zonaSel = item;
                this.storeMesas.setZona(item.id);
                this.goTo("/mesas/" + item.id);
            } else {
                this.zonaSel = null;
                this.goTo("/mesas");
                this.storeMesas.setZona(null);
            }
        },
        goTo(route) {
            this.$router.replace(route);
        },
        rmMesa() {
            if (!this.mesaSel) return;
            this.store = this.storeMesas;
            this.$refs.confirmDialog.openDialog("¿Desea eliminar la mesa " + this.mesaSel.nombre + "?", this.mesaSel);
        },
        rmZona() {
            if (!this.zonaSel) return;
            this.store = this.storeZonas;
            this.$refs.confirmDialog.openDialog("¿Desea eliminar la zona " + this.zonaSel.nombre + "?", this.zonaSel);
        },
        upMesa() {
            this.isNew = false;
            this.store = this.storeMesas;
            this.$refs.editDialog.openDialog(this.mesaSel, "Modificar una Mesa", this.storeMesas.fields);
        },
        async confirm(result, item) {
            if (result) {
                const error = await this.store.delete(item);
                if (error) {
                    this.snackbar = true;
                    this.snackbarText = error;
                }
                if (this.store == this.storeZonas) {
                    this.zonaSel = null;
                    this.storeMesas.setZona(null);
                    this.$router.replace("/mesas");
                }else{
                    this.mesaSel = null;
                    this.$router.replace("/mesas/" + this.zonaSel.id);
                }
                
             
            }
        },
        addMesa() {
            this.isNew = true;
            this.store = this.storeMesas;
            this.$refs.editDialog.openDialog(this.storeMesas.newItem, "Crear una Mesa", this.storeMesas.fields);
        },
        addZona() {
            this.isNew = true;
            this.store = this.storeZonas;
            this.$refs.editDialog.openDialog(this.storeZonas.newItem, "Crear una Zona", this.storeZonas.fields);
        },
        upZona() {
            this.isNew = false;
            this.store = this.storeZonas;
            this.$refs.editDialog.openDialog(this.zonaSel, "Editar Zona", this.storeZonas.fields);
        },
        async guardarItem(item) {
            let error = null;
            if (this.store) {
                if (this.store == this.storeMesas) {
                    this.mesaSel = item;
                } else {
                    this.zonaSel = item;
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


}


</script>
