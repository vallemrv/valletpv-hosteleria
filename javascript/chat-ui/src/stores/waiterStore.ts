import { defineStore } from 'pinia';
import { useCompanyStore } from '@/stores/companyStore';
import axios from 'axios';

export interface Waiter {
    id: number;
    nombre: string;
    apellidos: string;
    autorizado: boolean;
    activo: boolean;
}

export const useWaiterStore = defineStore('waiter', {
    state: () => ({
        waiters: [] as Waiter[],
        isLoading: false,
        error: null as string | null,
    }),

    actions: {
        createFormData(obj: any = {}) {
            const companyStore = useCompanyStore();
            if (!companyStore.companyData) return null;

            const formData = new FormData();
            formData.append('user', companyStore.companyData.user);
            formData.append('token', companyStore.companyData.token);

            for (const key in obj) {
                if (obj.hasOwnProperty(key) && obj[key] !== undefined && obj[key] !== null) {
                    formData.append(key, obj[key].toString());
                }
            }

            return formData;
        },

        async loadWaiters() {
            const companyStore = useCompanyStore();
            if (!companyStore.companyData) {
                this.error = 'No hay empresa seleccionada';
                return false;
            }

            this.isLoading = true;
            this.error = null;

            try {
                const url = `${companyStore.companyData.url}/api/camareros/listado`;

                // Enviamos credenciales
                const formData = this.createFormData();

                if (!formData) {
                    this.error = 'Error creando FormData';
                    return false;
                }

                const response = await axios.post(url, formData);
                const data = response.data;
                console.log('Listado camareros response:', data);

                if (Array.isArray(data)) {
                    this.waiters = data;
                    return true;
                } else {
                    console.error('Formato inválido de camareros:', data);
                    this.error = 'Error carga camareros (formato inválido)';
                    return false;
                }
            } catch (error: any) {
                this.error = 'Error de conexión';
                console.error('Error loading waiters:', error);
                return false;
            } finally {
                this.isLoading = false;
            }
        },

        async toggleAuthorization(waiter: Waiter) {
            const companyStore = useCompanyStore();
            if (!companyStore.companyData) return false;

            // v-switch ya ha cambiado 'waiter.autorizado' al nuevo valor.
            const newStatus = waiter.autorizado;
            const originalStatus = !newStatus; // para revertir

            try {
                const url = `${companyStore.companyData.url}/api/camareros/authorize_waiter`;
                const formData = this.createFormData({
                    id: waiter.id,
                    autorizado: newStatus ? 'True' : 'False' // Enviamos como string Python-friendly
                });

                if (!formData) return false;

                const response = await axios.post(url, formData);
                const data = response.data;

                if (data === "success") {
                    return true;
                } else {
                    waiter.autorizado = originalStatus; // Revert
                    this.error = 'Error actualizando camarero';
                    return false;
                }
            } catch (e) {
                waiter.autorizado = originalStatus; // Revert
                this.error = 'Error de conexión';
                console.error(e);
                return false;
            }
        }
    }
});
