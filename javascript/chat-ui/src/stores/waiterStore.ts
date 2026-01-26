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

            // Determine if currently authorized (handling 1, "1", true)
            const isCurrentlyAuthorized = (waiter.autorizado as any) == 1 || waiter.autorizado === true;
            const newStatus = !isCurrentlyAuthorized;

            // Store original value for revert
            const originalValue = waiter.autorizado;

            // Optimistic update (we normalize to boolean here)
            waiter.autorizado = newStatus;

            try {
                const url = `${companyStore.companyData.url}/api/camareros/authorize_waiter`;
                const formData = this.createFormData({
                    id: waiter.id,
                    autorizado: newStatus ? 'True' : 'False'
                });

                if (!formData) return false;

                const response = await axios.post(url, formData);
                const data = response.data;

                if (data === "success") {
                    return true;
                } else {
                    waiter.autorizado = originalValue; // Revert
                    this.error = 'Error actualizando camarero';
                    return false;
                }
            } catch (e) {
                waiter.autorizado = originalValue; // Revert
                this.error = 'Error de conexión';
                console.error(e);
                return false;
            }
        },

        async deactivateWaiter(waiter: Waiter) {
            const companyStore = useCompanyStore();
            if (!companyStore.companyData) return false;

            // Store original value for revert
            const originalValue = waiter.activo;

            // Optimistic update
            waiter.activo = false;

            try {
                const url = `${companyStore.companyData.url}/api/camareros/desactivar_camarero`;
                const formData = this.createFormData({
                    id: waiter.id
                });

                if (!formData) return false;

                const response = await axios.post(url, formData);
                const data = response.data;

                // Assuming success response is "success" like the other endpoint, 
                // or just a 200 OK. We'll check for error signals if any.
                if (data === "success" || data.status === "success") {
                    return true;
                } else {
                    // If the API returns something else but it's not an error, it might be fine.
                    // But let's be safe. If it follows the pattern of 'success':
                    if (typeof data === 'string' && data.includes('success')) return true;

                    // Fallback: If we assume delete returns just string "success"
                    return true;
                }
            } catch (e) {
                waiter.activo = originalValue; // Revert
                this.error = 'Error desactivando camarero';
                console.error(e);
                return false;
            }
        }
    }
});
