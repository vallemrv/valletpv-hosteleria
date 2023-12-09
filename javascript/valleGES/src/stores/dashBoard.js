import { defineStore } from "pinia";
import { buildUrl } from "@/api";
import { VENTAS_SIN_CIERRE, VENTAS_BY_ARTICULO, 
         VENTAS_BY_INTERVALOS,
         VENTAS_BY_CAMARERO } from "@/endpoints";
import axios from "axios";

export const DashBoard = defineStore({
    id: "dashBoard",
    state: () => ({
        url: null,
        storeEmpresa: null,
        error: null,
    }),
    actions: {
        setStoreEmpresa(storeEmpresa) {
            this.storeEmpresa = storeEmpresa;
        },
        async getVentasByArticulo() {
            try {
                const url = buildUrl(this.storeEmpresa.empresa.url, VENTAS_BY_ARTICULO);
                const params = this.storeEmpresa.createFormData();
                const response = await axios.post(url, params);
                const data = response.data;
                if (!data.success) {
                    return { data: null, error: data.errors };
                }
                return { data: data, error: null };
            } catch (error) {
                return { data: null, error: error };
            }
        },
        async getVentasByCam() {
            try {
                const url = buildUrl(this.storeEmpresa.empresa.url, VENTAS_BY_CAMARERO);
                const params = this.storeEmpresa.createFormData();
                const response = await axios.post(url, params);
                const data = response.data;
                if (!data.success) {
                    return { data: null, error: data.errors};
                }
                return { data: data, error: null };
            } catch (error) {
                return { data: null, error: error };
            }
        },
        async getVentasByIntervalos(date, estado) {
            try {
                const url = buildUrl(this.storeEmpresa.empresa.url, VENTAS_BY_INTERVALOS);
                const params = this.storeEmpresa.createFormData({ "date": date, "estado": estado });
                const response = await axios.post(url, params);
                const data = response.data;
                return { data: data, error: null };
            } catch (error) {
                return { data: null, error: error };
            }
        },
        async getVentasSinCierre() {
            try {
                const url = buildUrl(this.storeEmpresa.empresa.url, VENTAS_SIN_CIERRE);
                const params = this.storeEmpresa.createFormData();
                const response = await axios.post(url, params);
                const data = response.data;
                if (!data.success) {
                    return { data: null, error: data.errors };
                }
                return { data: data, error: null };
            } catch (error) {
                return { data: null, error: error };
            }
        }

    }
});




