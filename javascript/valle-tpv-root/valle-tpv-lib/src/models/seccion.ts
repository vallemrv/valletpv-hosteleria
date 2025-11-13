export interface SeccionParams {
  id?: number;
  nombre: string;
  icono?: string | null;
}

class Seccion {
  id: number;
  nombre: string;
  icono: string | null;
  static schema: string | null = '++id, nombre, icono';

  constructor({ id, nombre, icono = null }: SeccionParams) {
    this.id = id || Date.now();
    this.nombre = nombre;
    this.icono = icono;
  }

  validate(): void {
    if (!this.nombre) {
      throw new Error('El nombre es obligatorio.');
    }
  }
}

export default Seccion;
