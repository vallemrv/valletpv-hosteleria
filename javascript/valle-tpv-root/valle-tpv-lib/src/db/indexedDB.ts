import Dexie from 'dexie';
import Camarero from '../models/camarero';
import Empresa from '../models/empresa';
import Tecla from '../models/tecla';
import Cuenta from '../models/cuenta';
import Seccion from '../models/seccion';
import Sugerencia from '../models/sugerencia';
import { Mesa, Zona }  from '../models/mesaZona';
import Instruction from '../models/instruction';


// Definición de los esquemas de cada tabla usando los modelos
const schemas: Record<string, string> = {
  camareros: Camarero.schema ?? '',
  empresas: Empresa.schema ?? '',
  teclas: Tecla.schema ?? '',
  lineaspedido: Cuenta.schema ?? '',
  seccionescom: Seccion.schema ?? '',
  sugerencias: Sugerencia.schema ?? '',
  mesas: Mesa.schema ?? '',
  zonas: Zona.schema ?? '',
  instructionQueue: Instruction.schema ?? ''
};

class DB {
  private db: Dexie;

  constructor(dbName: string = 'ValleTPVDatabase') {
    this.db = new Dexie(dbName);
    this.db.version(2).stores(schemas);
  }

  async add(table: string, data: any): Promise<void> {
    await this.db.table(table).add(data);
  }

  async getAll(table: string): Promise<any[]> {
    return await this.db.table(table).toArray();
  }

  async getById(table: string, id: number | string): Promise<any> {
    return await this.db.table(table).get(id);
  }

  async update(table: string, id: number | string, updates: any): Promise<void> {
    await this.db.table(table).update(id, updates);
  }

  async remove(table: string, id: number | string): Promise<void> {
    await this.db.table(table).delete(id);
  }

  async syncWithServer(table: string, uid: string, url_server: string): Promise<boolean> {
    const formData = new FormData();
    formData.append('tb', table);
    formData.append('reg', JSON.stringify(await this.getAll(table)));
    formData.append('uid', uid);
    
    try {
      const response = await fetch(`${url_server}/api/sync/sync_devices`, {
        method: 'POST',
        body: formData
      });

      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      
      // Si hay operaciones que ejecutar del servidor
      if (Array.isArray(data) && data.length > 0) {
        await this.executeServerOperations(data);
        return true
      }
      return false;
    } catch (error) {
      console.error(`Error al sincronizar la tabla ${table}:`, error);
      return false;
    }
  }

  async executeServerOperations(operations: any[]): Promise<void> {
    for (const operation of operations) {
      const { tb, op, obj } = operation;
      const id = obj.id || obj.ID;
      try {
        switch (op) {
          case 'insert':
            await this.add(tb, obj);
            break;
          case 'md': // modify/update
            await this.update(tb, id, obj);
            break;
          case 'rm': // remove
            await this.remove(tb, id);
            break;
          default:
            console.warn(`Operación desconocida: ${op}`);
        }
        } catch (error) {
        console.error(`Error ejecutando operación ${op} en tabla ${tb}:`, error);
      }
    }
  }
}

const dbInstance = new DB();
export default dbInstance;
