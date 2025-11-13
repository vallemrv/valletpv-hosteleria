export interface MesaParams {
  ID: number;
  Nombre: string;
  RGB: string;
  abierta: 0 | 1;
  IDZona: number;
  num: number;
  Orden: number;
  Tarifa: number;
}

export interface ZonaParams {
  id: number;
  nombre: string;
  rgb: string;
  tarifa: number;
  icon?: string | null;
}


export interface MesaEstado {
  estado: 'libre' | 'ocupada' | 'impresa';
}

export interface MesasAccion {
  tipo: 'juntar' | 'mover' | 'borrar' | null;
  mesa: Mesa;
}

export class Mesa{
  ID: number;
  Nombre: string;
  RGB: string;
  abierta: 0 | 1;
  IDZona: number;
  num: number;
  Orden: number;
  Tarifa: number;
  static schema: string | null = '++ID, Nombre, RGB, abierta, IDZona, num, Orden, Tarifa';


  constructor({ID, Nombre, RGB, abierta, IDZona, num, Orden, Tarifa}: MesaParams) {
    this.ID = ID;
    this.Nombre = Nombre;
    this.RGB = RGB;
    this.abierta = abierta;
    this.IDZona = IDZona;
    this.num = num;
    this.Orden = Orden;
    this.Tarifa = Tarifa;
  }
} 

export class Zona {
  id: number;
  nombre: string;
  rgb: string;
  tarifa: number;
  icon?: string | null;
  static schema: string | null = '++id, nombre, rgb, tarifa, icon';

  constructor({id, nombre, rgb, tarifa, icon=null}: ZonaParams) {
    this.id = id;
    this.nombre = nombre;
    this.rgb = rgb;
    this.tarifa = tarifa;
    this.icon = icon;
  }
}
    
