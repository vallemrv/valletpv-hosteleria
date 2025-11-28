import { defineStore } from 'pinia';
import Cuenta from '../../models/cuenta';
import type { CuentaItem, InfoCobro } from '../../models/cuenta';
import db from '../../db/indexedDB';
import { Mesa } from '../../models/mesaZona';
import { useInstruccionesStore } from '../instruccionesStore';
import { useCamarerosStore } from './camarerosStore';
import { useMesasStore } from './mesasStore';
import { useEmpresasStore } from './empresasStore';


export const useCuentaStore = defineStore('lineaspedido', {
  state: () => ({
    items: [] as Cuenta[],
    isLoadDB: false,
    infoCobro: null as InfoCobro | null,
    isAparcandoMesa: false, // Flag para evitar ejecución concurrente de aparcarMesa
    isCobrandoMesa: false, // Flag para evitar ejecución concurrente de cobrarMesa
    isCambiandoMesa: false, // Flag para evitar ejecución concurrente de cambiarMesa
    isJuntandoMesas: false, // Flag para evitar ejecución concurrente de juntarMesas
    isBorrandoLineas: false, // Flag para evitar ejecución concurrente de rmConMotivo
  }),
  getters: {
    // Getter complejo para obtener cuenta agrupada de una mesa
    cuentaDetalladaPorMesa: (state) => {
      return (idMesa: number) => {
        // Filtrar cuentas de la mesa específica para estados P y N
        const cuentasMesa = state.items.filter(cuenta =>
          cuenta.IDMesa === idMesa && (cuenta.Estado === 'P' ||
            cuenta.Estado === 'M' || cuenta.Estado === 'N')
        );

        // Agrupar por descripcion_t, precio y estado para contar cantidades
        const agrupado = new Map<string, CuentaItem>();

        cuentasMesa.forEach(cuenta => {
          const clave = `${cuenta.descripcion_t}_${cuenta.Precio}_${cuenta.Estado}`;
          const cuentaIdNum = Number(cuenta.ID);

          if (agrupado.has(clave)) {
            const item = agrupado.get(clave)!;
            item.cantidad += 1;
            item.total = item.cantidad * item.precio;
            // Mantener el ID más reciente (comparar como number)
            if (cuentaIdNum > item.ultimoPedido!) {
              item.ultimoPedido = cuentaIdNum;
            }
          } else {
            agrupado.set(clave, {
              cantidad: 1,
              descripcion: cuenta.descripcion_t,
              precio: cuenta.Precio,
              total: cuenta.Precio,
              estado: cuenta.Estado,
              ultimoPedido: cuentaIdNum
            });
          }
        });

        // Convertir Map a Array y ordenar por último pedido (descendente)
        return Array.from(agrupado.values()).sort((a, b) => (b.ultimoPedido || 0) - (a.ultimoPedido || 0));
      };
    },

    // Getter para buscar cuentas por mesa y estado
    lineasNuevas: (state) => {
      return (idMesa: number): Cuenta[] => {
        return state.items.filter(cuenta =>
          cuenta.IDMesa === idMesa && cuenta.Estado === 'N'
        );
      };
    },

    // Getter para obtener todas las líneas de una mesa sin agrupar
    lineasPorMesa: (state) => {
      return (idMesa: number) => {
        return state.items.filter(cuenta =>
          cuenta.IDMesa === idMesa
        );
      };
    },

    // Getter para agrupar por Descripcion (no descripcion_t) - devuelve CuentaItem[]
    cuentaAgrupadaPorPedido: (state) => {
      return (idMesa: number): CuentaItem[] => {
        // Filtrar cuentas de la mesa específica Y por estado
        const cuentasMesa = state.items.filter(cuenta =>
          cuenta.IDMesa === idMesa && (cuenta.Estado === 'P' ||
            cuenta.Estado === 'M' || cuenta.Estado === 'N')
        );

        // Agrupar por Descripcion y Precio para contar cantidades
        const agrupado = new Map<string, CuentaItem>();

        cuentasMesa.forEach(cuenta => {
          const clave = `${cuenta.Descripcion}_${cuenta.Precio}_${cuenta.Estado}`;
          const cuentaIdNum = Number(cuenta.ID);

          if (agrupado.has(clave)) {
            const item = agrupado.get(clave)!;
            item.cantidad += 1;
            item.total = item.cantidad * item.precio;
            // Mantener el ID más reciente (comparar como number)
            if (cuentaIdNum > item.ultimoPedido!) {
              item.ultimoPedido = cuentaIdNum;
            }
          } else {
            agrupado.set(clave, {
              cantidad: 1,
              descripcion: cuenta.Descripcion, // ← Usar Descripcion en lugar de descripcion_t
              precio: cuenta.Precio,
              total: cuenta.Precio,
              estado: cuenta.Estado,
              ultimoPedido: cuentaIdNum
            });
          }
        });

        // Convertir Map a Array y ordenar por último pedido (descendente)
        return Array.from(agrupado.values()).sort((a, b) => (b.ultimoPedido || 0) - (a.ultimoPedido || 0));
      };
    },

    // Getter para obtener líneas individuales por mesa, descripción, precio y cantidad
    desgloseLineas: (state) => {
      return (mesa_id: number, descripcion: string, precio: number, cantidad: number): Cuenta[] => {
        // Filtrar por mesa, descripción y precio
        const lineasFiltradas = state.items.filter(cuenta =>
          cuenta.IDMesa === mesa_id &&
          cuenta.descripcion_t === descripcion &&
          cuenta.Precio === precio
        );

        // Retornar solo la cantidad solicitada
        return lineasFiltradas.slice(0, cantidad);
      };
    },
  },
  actions: {
    limpiarInfoCobro() {
      this.infoCobro = null;
    },
    async imprimirCuenta(mesa: Mesa | null) {
      if (mesa) {
        mesa.num += 1;
        await db.update('mesas', mesa.ID, mesa);
        const instruccionesStore = useInstruccionesStore();
        instruccionesStore.encolarInstruccion(
          'api/impresion/preimprimir',
          { idm: mesa.ID }
        );

      }
    },
    async cobrarMesa(idMesa: number, infoCobro: InfoCobro, itemsCobrados: CuentaItem[]) {
      // 1. Comprobar si ya hay una operación en curso. Si es así, no hacer nada.
      if (this.isCobrandoMesa) {
        console.warn('cobrarMesa ya está en ejecución, se ignora la llamada concurrente.');
        return;
      }

      // 2. Bloquear la función para futuras llamadas
      this.isCobrandoMesa = true;

      try {
        // 3. Si no hay items para cobrar, salir temprano
        if (itemsCobrados.length === 0) {
          return;
        }

        // 4. Recopilar TODAS las cuentas que van a ser cobradas
        const cuentasParaCobrar: Cuenta[] = [];

        itemsCobrados.forEach(itemCobrado => {
          // Intentar buscar primero por descripcion_t (cuenta normal)
          let cuentasFiltradas = this.items.filter(cuenta =>
            cuenta.IDMesa === idMesa &&
            cuenta.descripcion_t === itemCobrado.descripcion &&
            cuenta.Precio === itemCobrado.precio &&
            (cuenta.Estado === 'P' || cuenta.Estado === 'M')
          ).slice(0, itemCobrado.cantidad);

          // Si no se encontraron, buscar por Descripcion (dividir cuenta)
          if (cuentasFiltradas.length === 0) {
            cuentasFiltradas = this.items.filter(cuenta =>
              cuenta.IDMesa === idMesa &&
              cuenta.Descripcion === itemCobrado.descripcion &&
              cuenta.Precio === itemCobrado.precio &&
              (cuenta.Estado === 'P' || cuenta.Estado === 'M')
            ).slice(0, itemCobrado.cantidad);
          }

          if (cuentasFiltradas.length === 0) {
            console.warn('⚠️ No se encontraron líneas para:', itemCobrado.descripcion);
          }

          cuentasParaCobrar.push(...cuentasFiltradas);
        });

        // 5. Si no hay cuentas para cobrar, salir
        if (cuentasParaCobrar.length === 0) {
          console.error('❌ No se encontraron cuentas para cobrar.');
          return;
        }

        // 6. ¡CRÍTICO! Actualizar el estado de TODAS las cuentas a 'C' INMEDIATAMENTE
        const updates = cuentasParaCobrar.map(cuenta =>
          this.update({ ...cuenta, Estado: "C" })
        );
        await Promise.all(updates);

        // 7. Guardar la información de cobro
        this.infoCobro = infoCobro;

        // 8. Encolar UNA SOLA instrucción con TODAS las cuentas cobradas
        const camarerosStore = useCamarerosStore();
        const idCamarero = camarerosStore.camarerosSel?.id || 0;
        const instruccionesStore = useInstruccionesStore();

        await instruccionesStore.encolarInstruccion(
          'api/cuenta/cobrar',
          {
            idm: idMesa,
            idc: idCamarero,
            entrega: infoCobro.totalEntregado,
            recibo: infoCobro.recibo || '',
            idsCobrados: JSON.stringify(cuentasParaCobrar.map(c => c.ID))
          }
        );

      } catch (error) {
        console.error("Error durante cobrarMesa:", error);
        // Aquí podrías añadir lógica para revertir los cambios si algo falla
      } finally {
        // 9. Liberar el bloqueo SIEMPRE, tanto si todo fue bien como si hubo un error
        this.isCobrandoMesa = false;
      }
    },
    async aparcarMesa(idMesa: number) {
      // 1. Comprobar si ya hay una operación en curso. Si es así, no hacer nada.
      if (this.isAparcandoMesa) {
        console.warn('aparcarMesa ya está en ejecución, se ignora la llamada concurrente.');
        return;
      }

      // 2. Bloquear la función para futuras llamadas
      this.isAparcandoMesa = true;

      try {
        // 3. Obtener TODAS las líneas nuevas para esta mesa DE UNA SOLA VEZ.
        const lineasParaAparcar = this.items.filter(c =>
          c.IDMesa === idMesa && c.Estado === 'N'
        );

        // 4. Si no hay líneas, no hay nada que hacer. Salimos temprano.
        if (lineasParaAparcar.length === 0) {
          return; // La función termina aquí.
        }


        // 5. ¡CRÍTICO! Actualizar el estado de TODAS las líneas a 'P' INMEDIATAMENTE.
        // Usamos Promise.all para hacer todas las actualizaciones de estado en paralelo y esperar a que terminen.
        const updates = lineasParaAparcar.map(cuenta =>
          this.update({ ...cuenta, Estado: "P" })
        );
        await Promise.all(updates);

        // Ahora que el estado está actualizado, las siguientes llamadas a `aparcarMesa` no encontrarán nada en el paso 3.

        // 6. Encolar UNA SOLA instrucción con TODAS las líneas que acabamos de actualizar.
        const camarerosStore = useCamarerosStore();
        const idCamarero = camarerosStore.camarerosSel?.id || 0;
        const instruccionesStore = useInstruccionesStore();

        await instruccionesStore.encolarInstruccion(
          'api/cuenta/add',
          {
            idm: idMesa,
            idc: idCamarero,
            uid_device: Date.now(),
            // Mapeamos el array que cogimos al principio
            pedido: JSON.stringify(lineasParaAparcar.map(c => ({
              ID: c.ID,
              descripcion_t: c.descripcion_t,
              IDArt: c.IDArt,
              Descripcion: c.Descripcion,
              Precio: c.Precio,
            }))),
          }
        );

      } catch (error) {
        console.error("Error durante aparcarMesa:", error);
        // Aquí podrías añadir lógica para revertir los cambios si algo falla
      } finally {
        // 7. Liberar el bloqueo SIEMPRE, tanto si todo fue bien como si hubo un error.
        this.isAparcandoMesa = false;
      }
    },
    async rmConMotivo(idm: number, motivo: string, lineas: Cuenta[]) {
      // 1. Comprobar si ya hay una operación en curso. Si es así, no hacer nada.
      if (this.isBorrandoLineas) {
        console.warn('rmConMotivo ya está en ejecución, se ignora la llamada concurrente.');
        return;
      }

      // 2. Bloquear la función para futuras llamadas
      this.isBorrandoLineas = true;

      try {
        // 3. Filtrar líneas válidas (con ID)
        const lineasValidas = lineas.filter(linea =>
          linea.ID !== null && linea.ID !== undefined
        );

        // 4. Si no hay líneas válidas, salir
        if (lineasValidas.length === 0) {
          return;
        }

        // 5. Eliminar todas las líneas
        for (const linea of lineasValidas) {
          await this.rm(linea.ID!);
        }

        // 6. Encolar instrucción para sincronizar el borrado de líneas
        const camarerosStore = useCamarerosStore();
        const idCamarero = camarerosStore.camarerosSel?.id || 0;
        const instruccionesStore = useInstruccionesStore();

        await instruccionesStore.encolarInstruccion(
          'api/cuenta/rmlinea',
          {
            idsABorrar: JSON.stringify(lineasValidas.map(l => l.ID)),
            idc: idCamarero,
            motivo: motivo,
            idm: idm
          }
        );

      } catch (error) {
        console.error("Error durante rmConMotivo:", error);
        // Aquí podrías añadir lógica para revertir los cambios si algo falla
      } finally {
        // 7. Liberar el bloqueo SIEMPRE, tanto si todo fue bien como si hubo un error
        this.isBorrandoLineas = false;
      }
    },
    async cambiarMesa(id_origen: number, id_destino: number) {
      // 1. Comprobar si ya hay una operación en curso. Si es así, no hacer nada.
      if (this.isCambiandoMesa) {
        console.warn('cambiarMesa ya está en ejecución, se ignora la llamada concurrente.');
        return false;
      }

      // 2. Validación básica: no se puede cambiar una mesa consigo misma
      if (id_origen === id_destino) {
        console.warn('No se puede cambiar una mesa consigo misma.');
        return false;
      }

      // 3. Bloquear la función para futuras llamadas
      this.isCambiandoMesa = true;

      try {
        // 4. Obtener líneas de ambas mesas
        const lineasOrigen = this.items.filter(cuenta =>
          cuenta.IDMesa === id_origen
        );
        const lineasDestino = this.items.filter(cuenta =>
          cuenta.IDMesa === id_destino
        );

        // 5. Verificar que la mesa origen tenga productos
        if (lineasOrigen.length === 0) {
          console.warn(`La mesa origen (${id_origen}) no tiene productos. No se puede mover.`);
          return false;
        }

        if (lineasDestino.length === 0) {
          // Mesa destino está cerrada: mover todas las líneas de origen a destino
          for (const linea of lineasOrigen) {
            await this.update({ ...linea, IDMesa: id_destino });
          }

          const mesasStore = useMesasStore();

          // Cerrar mesa origen y abrir mesa destino
          await mesasStore.cerrarMesa(id_origen);
          await mesasStore.abrirMesa(id_destino);

        } else {
          // Mesa destino tiene líneas: intercambiar líneas entre ambas mesas
          // Cambiar líneas de origen a destino
          for (const linea of lineasOrigen) {
            await this.update({ ...linea, IDMesa: id_destino });
          }

          // Cambiar líneas de destino a origen
          for (const linea of lineasDestino) {
            await this.update({ ...linea, IDMesa: id_origen });
          }
        }

        // 6. Encolar instrucción para sincronizar el cambio de mesas
        const instruccionesStore = useInstruccionesStore();

        await instruccionesStore.encolarInstruccion(
          'api/cuenta/cambiarmesas',
          {
            idp: id_origen, // id de mesa origen
            ids: id_destino // id de mesa destino
          }
        );

        return true;

      } catch (error) {
        console.error("Error durante cambiarMesa:", error);
        // Aquí podrías añadir lógica para revertir los cambios si algo falla
        return false;
      } finally {
        // 7. Liberar el bloqueo SIEMPRE, tanto si todo fue bien como si hubo un error
        this.isCambiandoMesa = false;
      }
    },
    async juntarMesas(id_mesa_origen: number, id_mesa_destino: number) {
      // 1. Comprobar si ya hay una operación en curso. Si es así, no hacer nada.
      if (this.isJuntandoMesas) {
        console.warn('juntarMesas ya está en ejecución, se ignora la llamada concurrente.');
        return false;
      }

      // 2. Validación básica: no se puede juntar una mesa consigo misma
      if (id_mesa_origen === id_mesa_destino) {
        console.warn('No se puede juntar una mesa consigo misma.');
        return false;
      }

      // 3. Bloquear la función para futuras llamadas
      this.isJuntandoMesas = true;

      try {
        // 4. VALIDACIONES: Ambas mesas deben estar abiertas y con productos
        const lineasOrigen = this.items.filter(cuenta =>
          cuenta.IDMesa === id_mesa_origen
        );
        const lineasDestino = this.items.filter(cuenta =>
          cuenta.IDMesa === id_mesa_destino
        );

        // Verificar que ambas mesas tengan productos
        if (lineasOrigen.length === 0) {
          console.warn(`La mesa origen (${id_mesa_origen}) no tiene productos. No se puede juntar.`);
          return false;
        }

        if (lineasDestino.length === 0) {
          console.warn(`La mesa destino (${id_mesa_destino}) no tiene productos. No se puede juntar.`);
          return false;
        }

        // 5. Mover todas las líneas de origen a destino
        for (const linea of lineasOrigen) {
          await this.update({ ...linea, IDMesa: id_mesa_destino });
        }

        // 6. Gestionar estados de mesas
        const mesasStore = useMesasStore();

        // Cerrar mesa origen y asegurarse que mesa destino esté abierta
        await mesasStore.cerrarMesa(id_mesa_origen);
        await mesasStore.abrirMesa(id_mesa_destino);

        // 7. Encolar instrucción para sincronizar la unión de mesas
        const instruccionesStore = useInstruccionesStore();

        await instruccionesStore.encolarInstruccion(
          'api/cuenta/juntarmesas',
          {
            idp: id_mesa_destino, // id de la mesa destino
            ids: id_mesa_origen // id de la mesa origen
          }
        );

        return true;

      } catch (error) {
        console.error("Error durante juntarMesas:", error);
        // Aquí podrías añadir lógica para revertir los cambios si algo falla
        return false;
      } finally {
        // 8. Liberar el bloqueo SIEMPRE, tanto si todo fue bien como si hubo un error
        this.isJuntandoMesas = false;
      }
    },
    async update(updated: Cuenta) {
      if (updated.ID) {
        await db.update('lineaspedido', updated.ID, updated);
        const idx = this.items.findIndex((item: Cuenta) => item.ID === updated.ID);
        if (idx !== -1) {
          this.items[idx] = updated;
        }
      }
    },
    async insert(cuenta: Cuenta) {
      await db.add('lineaspedido', cuenta);
      this.items.push(cuenta);
    },
    async rm(id: number) {
      await db.remove('lineaspedido', id);
      this.items = this.items.filter((item: Cuenta) => item.ID !== id);
    },
    async initStore() {
      if (!this.isLoadDB) {
        const cuentas = await db.getAll('lineaspedido');
        this.items = cuentas;
        this.isLoadDB = true;
      }

    },
    async comprobarCuenta(idm: number) {
      const empresasStore = useEmpresasStore();
      const empresaActiva = empresasStore.empresaActiva;

      if (!empresaActiva || !empresaActiva.url_servidor) {
        console.error('No se pudo obtener la URL del servidor desde el store de empresas.');
        return;
      }

      let lineas = this.items.filter(c => c.IDMesa === idm);

      const formData = new FormData();
      formData.append('uid', empresaActiva.uid);
      formData.append('idm', idm.toString());
      formData.append('reg', JSON.stringify(lineas));

      try {
        const response = await fetch(`${empresaActiva.url_servidor}/api/cuenta/get_cuenta`, {
          method: 'POST',
          body: formData,
        });

        if (response.ok) {
          const data = await response.json();

          if (!data.soniguales) {
            // Aplicar inserts
            data.delta.inserts.forEach((item: Cuenta) => {
              let exists = this.items.some(c => c.ID === item.ID);
              if (!exists)
                this.insert(item);
              else
                this.update(item);
            });

            // Aplicar updates
            data.delta.updates.forEach((update: Partial<Cuenta>) => {
              const index = this.items.findIndex(c => c.ID === update.ID);
              if (index !== -1) {
                this.update({ ...this.items[index], ...update });
              }
            });

            // Aplicar deletes
            data.delta.deletes.forEach((del: { ID: number }) => {
              this.rm(del.ID);
            });
          }
        } else {
          console.error('Error al comprobar cuenta:', response.status);
        }
      } catch (error) {
        console.error('Error de red al comprobar cuenta:', error);
      }
    },
  }
});
