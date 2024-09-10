from django.db import models
from .sync import Sync
from django.forms.models import model_to_dict

class BaseModels(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    

    def serialize(self):
        return model_to_dict(self)   
    

    def save(self, *args, **kwargs):
        sync = Sync.objects.filter(nombre=self._meta.db_table).first()
        if not sync:
            sync = Sync()
        sync.nombre = self._meta.db_table
        sync.save()
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        sync = Sync.objects.filter(nombre=self._meta.db_table).first()
        if not sync:
            sync = Sync()
        sync.nombre = self._meta.db_table
        sync.save()
        return super().delete( *args, **kwargs)
    

    @classmethod
    def get_normalization_rules(cls):
        """
        Devuelve las reglas de normalización que debe seguir cada campo.
        Si no se define una regla, por defecto se trata como 'str'.
        """
        return {
        }

    @classmethod
    def normalize_record(cls, record):
        """
        Normaliza un registro usando las reglas de normalización definidas por cada clase hija.
        Si no se proporciona una regla, el campo se convierte a 'str'.
        """
        normalized = {k.lower(): v for k, v in record.items()}
        rules = cls.get_normalization_rules()

        for key, value in normalized.items():
            # Aplicar reglas específicas si existen
            if key in rules:
                rule = rules[key]
                if rule['type'] == 'int':
                    normalized[key] = int(value)
                elif rule['type'] == 'float':

                    normalized[key] = float(value)
                elif rule['type'] == 'str':
                    if rule.get('remove_quotes', False):
                        normalized[key] = str(value).replace("'", '').replace('"', '')
                    if rule.get('null_to_empty', False) and (value is None or value == 'null'):
                        # Si la regla define null_to_empty, reemplaza None o 'null' por una cadena vacía
                        normalized[key] = ''
            
            else:
                # Por defecto, convertir a 'str'
                normalized[key] = str(value)
         

        return normalized

    @classmethod
    def compare_value_by_key(cls, val1, val2):
        return str(val1).strip() == str(val2).strip()

    @classmethod
    def normalize_and_compare(cls, reg1, reg2):
    
        """
        Normaliza dos registros y los compara clave por clave.
        """
        reg1_normalized = cls.normalize_record(reg1)
        reg2_normalized = cls.normalize_record(reg2)

        for key in reg1_normalized:
            if not cls.compare_value_by_key(reg1_normalized[key], reg2_normalized.get(key)):
                print(key, reg1_normalized[key])
                print(key, reg2_normalized[key])
                return False

        return True

    @classmethod
    def compare_regs(cls, regs):
        
        """
        Compara los datos del cliente con los datos del servidor buscando por ID directamente en la base de datos.
        """
        result = []
        ids_procesados = []

        # Recorrer los registros recibidos del cliente
        for r in regs:
            key_id = next((k for k in ['id', 'ID'] if k in r), None)
            client_id = r.get(key_id)
          
            if client_id:
                # Buscar el registro en la base de datos por ID
                try:
                    server_record = cls.objects.get(id=client_id).serialize()
                except cls.DoesNotExist:
                    server_record = None
                except Exception as e:
                    # Manejar otros posibles errores de la base de datos
                    print(f"Error al consultar la base de datos: {e}")
                    continue
             
        
                if server_record:
                    # Si el registro existe, normalizamos y comparamos clave por clave
                    if not cls.normalize_and_compare(r, server_record):
                        result.append({
                            'tb': cls.__name__.lower(),
                            'op': 'md',  # Operación de modificación
                            'obj': server_record
                        })
                        
                else:
                    # Si no existe en la base de datos, el registro debe ser eliminado
                    result.append({
                        'tb': cls.__name__.lower(),
                        'op': 'rm',  # Operación de eliminación
                        'obj': {'id': client_id}
                    })

                # Agregar el ID a la lista de procesados
                ids_procesados.append(client_id)

        # Buscar registros en la base de datos que no están en los registros del cliente
        server_records = cls.objects.exclude(id__in=ids_procesados)

        for server_record in server_records:
            result.append({
                'tb': cls.__name__.lower(),
                'op': 'insert',  # Operación de inserción
                'obj': server_record.serialize()
            })

       

        return result
         

    
    class Meta:
        abstract = True  # Indica que este modelo es abstracto