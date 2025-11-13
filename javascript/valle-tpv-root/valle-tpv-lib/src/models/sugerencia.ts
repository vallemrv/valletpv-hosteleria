export interface SugerenciaParams {
  id: string;
  tecla: string;
  sugerencia: string;
  incremento?: number;
}

class Sugerencia {
  id: string;
  tecla: string;
  sugerencia: string;
  incremento: number;
  static schema: string | null = '++id, tecla, sugerencia, incremento';

  constructor({ id, tecla, sugerencia, incremento=0.0 }: SugerenciaParams) {
    this.id = id;
    this.tecla = tecla;
    this.sugerencia = sugerencia;
    this.incremento = incremento;
  }

  validate(): void {
    if (!this.id || !this.tecla) {
      throw new Error('ID y Tecla son obligatorios.');
    }
  }
}

export default Sugerencia;
