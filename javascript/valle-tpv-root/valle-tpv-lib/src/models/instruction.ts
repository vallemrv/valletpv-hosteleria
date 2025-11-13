class Instruction {
    id?: number;
    url: string;
    endpoint: string;
    data: Record<string, any>;
    timestamp: number; // Para mejor trazabilidad
    attempts: number;  // Para contar reintentos

    static get schema() {
        return '++id'; // Clave primaria autoincremental para garantizar el orden
    }
    constructor(url: string, endpoint: string, data: Record<string, any>) {
        this.url = url;
        this.endpoint = endpoint;
        this.data = data;
        this.timestamp = Date.now();
        this.attempts = 0;
    }
}

export default Instruction;