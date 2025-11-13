export interface TeclaParams {
  ID: number;
  nombre: string;
  IDFamilia?: number | null;
  IDSeccion?: number | null;
  IDSeccionCom?: number | null;
  Precio?: number | null ;
  RGB?: string | null;
  p1: number;
  p2: number;
  tag?: string | null;
  orden?: number | null;
  tipo?: string | null;
  descripcion_t: string;
  descripcion_r: string;
  IDParentTecla?:  number | null;
  hay_existencias: 0 | 1;
}

class Tecla {
  ID: number;
  nombre: string;
  IDFamilia?: number | null = null;
  IDSeccion?: number | null = null;
  IDSeccionCom?: number | null = null;
  Precio?: number | null = null;
  RGB?: string | null = null;
  p1: number;
  p2: number;
  tag?: string | null = null;
  orden?: number | null = null;
  tipo?: string | null = null;
  descripcion_t: string;
  descripcion_r: string;
  IDParentTecla?:  number | null = null;
  hay_existencias: 0 | 1 = 1;
  static schema: string | null = '++ID, nombre, p1, p2, IDFamilia, IDParentTecla, IDSeccion, IDSeccionCom, Precio, RGB, tag, orden, tipo, descripcion_t, descripcion_r, hay_existencias';

  constructor({ ID, nombre, p1, p2, descripcion_t, descripcion_r,  tag = null, orden = null, tipo = null,   hay_existencias = 1, IDFamilia = 0, IDParentTecla = null, IDSeccion = 0, IDSeccionCom = 0, Precio = 0, RGB = '' }: TeclaParams) {
    this.ID = ID || Date.now();
    this.nombre = nombre;
    this.p1 = p1;
    this.p2 = p2;
    this.IDFamilia = IDFamilia;
    this.IDSeccion = IDSeccion;
    this.IDSeccionCom = IDSeccionCom;
    this.Precio = Precio;
    this.RGB = RGB;
    this.tag = tag;
    this.orden = orden;
    this.tipo = tipo;
    this.descripcion_t = descripcion_t;
    this.descripcion_r = descripcion_r;
    this.IDParentTecla = IDParentTecla;
    this.hay_existencias = hay_existencias;
  }

}

export default Tecla;
