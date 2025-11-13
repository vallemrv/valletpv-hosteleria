
export interface EmpresaParams {
  uid: string;
  id?: number;
  nombre: string;
  descripcion: string;
  url_servidor: string;
  usa_cash_keeper?: 0 | 1;
  url_cash_keeper?: string;
  usa_pinpad?: 0 | 1;
  activa?: 0 | 1;
  url_pinpad?: string;
}

class Empresa {
  uid: string;
  id: number;
  descripcion: string;
  nombre: string;
  url_servidor: string;
  usa_cash_keeper: 0 | 1;
  url_cash_keeper: string;
  usa_pinpad: 0 | 1;
  url_pinpad: string; 
  activa: 0 | 1;
  static schema: string | null = '++id, uid, nombre, activa, descripcion, url_servidor, usa_cash_keeper, url_cash_keeper, usa_pinpad, url_pinpad';

  constructor({ uid, id, nombre, descripcion, url_servidor, activa=1, usa_cash_keeper = 0, url_cash_keeper = '', usa_pinpad = 0, url_pinpad = '' }: EmpresaParams) {
    this.uid = uid;
    this.id = id || Date.now();
    this.nombre = nombre;
    this.activa = activa;
    this.descripcion = descripcion;
    this.url_servidor = url_servidor;
    this.usa_cash_keeper = usa_cash_keeper;
    this.url_cash_keeper = url_cash_keeper;
    this.usa_pinpad = usa_pinpad;
    this.url_pinpad = url_pinpad;
  }

  validate(): void {
    if (!this.uid || !this.descripcion || !this.url_servidor) {
      throw new Error('UID, descripción y URL del servidor son obligatorios.');
    }
  }
}

export default Empresa;
