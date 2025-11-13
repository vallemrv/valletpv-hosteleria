export interface CuentaParams {
  ID?: number | null;
  Estado: string;
  Descripcion: string;
  descripcion_t: string;
  Precio: number;
  IDMesa: number;
  IDArt?: number;
  camarero: number;
  nomMesa: string;
}

export interface InfoCobro{
  id?: number;
  totalEntregado: number;
  tipo: 'efectivo' | 'tarjeta';
  totalCobrado?: number;
  cambio?: number;
  recibo?: string;
}


export interface CuentaItem {
  id?:  number;
  cantidad: number;
  descripcion: string;
  precio: number;
  total: number;
  ultimoPedido?: number;
  estado?: string;
}


class Cuenta {
  ID:  number | null;
  Estado: string;
  Descripcion: string;
  descripcion_t: string;
  Precio: number;
  IDPedido: number;
  IDMesa: number;
  IDArt: number;
  receptor: number;
  camarero: number;
  nomMesa: string;
  IDZona: number;
  servido: 0 | 1;
  UID: string;
  static schema: string | null = '++ID, Estado, Descripcion, Descripcion_t, Precio, IDPedido, IDMesa, IDArt, Receptor, Camarero, NomMesa, IDZona, Servido, UID';

  constructor({ID=null,  Estado="N", Descripcion, descripcion_t, Precio,  IDMesa, IDArt=-1,  camarero, nomMesa }: CuentaParams) {
    this.ID = ID || Date.now();
    this.Estado = Estado;
    this.Descripcion = Descripcion;
    this.descripcion_t = descripcion_t;
    this.Precio = Precio;
    this.IDPedido = -1;
    this.IDMesa = IDMesa;
    this.IDArt = IDArt;
    this.receptor = -1;
    this.camarero = camarero;
    this.nomMesa = nomMesa;
    this.IDZona = -1;
    this.servido = 1;
    this.UID = Date.now().toString();
  }

}

export default Cuenta;
