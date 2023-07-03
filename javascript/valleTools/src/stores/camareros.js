import { defineStore } from "pinia";


//definimos el store camareros
export const CamarerosStore = defineStore({
    //id del store
    id: 'camareros',
    //estado del store
    state: () => ({
        pathDoc: null,
        collectionName: "camareros",
        //array de camareros
        items: [],
        //titulo del store
        titulo: 'Camareros',
        //Cabecera de la tabla
        headers: ["Nombre"],
        switchKey: "activo",
        showKeys: ["displayName"],
        displayName: "dipslayName",
        permisos: [],
        //array de objetos con los datos de la tabla
        fields: [
            { key: 'nombre', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"], },
            { key: 'apellidos', label: 'Apellidos', type: 'text', rules: [v => !!v || "Los apellidos son requeridos"], },
            { key: 'permisos', label: 'Permisos', multiple: true, type: 'select', options:[] },
        ],
        extraAcitons: [
            { icon: "mdi-key", action: "clearPassword" },
        ],
        newItem: {
            nombre: "",
            apellidos: "",
            activo: false,
            permisos: [],
        },
    }),
    //acciones del store
    actions: {
        async execAction(item, action) {
            const error = await fb_update(this.pathDoc, item.id, { password: "" });
            if (error) {
                return "Error al cambiar la contraseña: " + error;
            }
            return null;
        },
        async load(pathDoc) {
            this.pathDoc = pathDoc;
            this.items = [];
            const { docs: permisos } = await fb_getAll("permisos_camareros");
         
            this.permisos = permisos;
            this.fields[2].options = permisos;

            const { docs, error } = await fb_getAll(pathDoc);
            
            if (error) {
                console.error("Error al cargar los camareros:", error);
                return;
            }

            this.items = docs;  

        },
        async switchCh(item) {
            await fb_update(this.pathDoc, item.id, { activo: item.activo });
        },
        async add(item) {
            item.password = "";
            item.displayName = item.nombre + " " + item.apellidos;
            const { doc, error } = await fb_create(this.pathDoc, item);
            if (error) {
                return "Error al crear el camarero: " + error;
            }
            this.items.push(doc);
            return null;
        },
        async update(item) {
            item.displayName = item.nombre + " " + item.apellidos;
            const error = await fb_update(this.pathDoc, item.id, item);
            if (error) {
                return "Error al actualizar el camarero: " + error;
            }
            this.items[index] = item;
            return null;
        },
        async delete(item) {
            const error = await fb_delete(item.id);
            if (error) {
                return "Error al eliminar el camarero: " + error;
            }
            const index = this.items.findIndex((i) => i.id === item.id);
            this.items.splice(index, 1);
        }
    }
});




