// Modelo de datos para la colección 'camareros'


export interface CamareroParams {
  id?: number;
  nombre: string;
  apellidos: string;
  activo?: 0 | 1 | boolean;
  pass_field?: string;
  permisos?: string[];
  autorizado?: 0 | 1 | boolean;
  email?: string | null;
}

class Camarero {
  id: number;
  nombre: string;
  apellidos: string;
  activo: 0 | 1 | boolean;
  pass_field: string;
  permisos: string[];
  autorizado: 0 | 1 | boolean;
  email: string | null;
  static schema: string | null = '++id, nombre, apellidos, activo, autorizado, pass_field, permisos, email';

  constructor({ id, nombre, apellidos, activo = 1, pass_field = '', permisos = [], autorizado = 1, email = null }: CamareroParams) {
    this.id = id || Date.now();
    this.nombre = nombre;
    this.apellidos = apellidos;
    this.activo = activo;
    this.pass_field = pass_field;
    this.permisos = permisos;
    this.autorizado = autorizado;
    this.email = email;
  }

  validate(): void {
    if (!this.nombre || !this.apellidos) {
      throw new Error('El nombre y los apellidos son obligatorios.');
    }
  }
}

export default Camarero;
