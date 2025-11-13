from django.db import models
from django.forms.models import model_to_dict
from decimal import Decimal


class BaseModels(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)

    def serialize(self):
        data = model_to_dict(self)
        # Convertir todos los Decimal a float
        for key, value in data.items():
            if isinstance(value, Decimal):
                data[key] = float(value)
        return data

    @classmethod
    def normalize_value(cls, v):
        if v in [None, 'None', "null"]:
            return ''
        if isinstance(v, str):
            v = v.strip()
            if v.isdigit():
                return int(v)
            try:
                return float(v)
            except ValueError:
                return v.lower()
        return v

    @classmethod
    def compare_regs(cls, regs):
        result = []
        ids_procesados = []

        for reg in regs:
            # Buscar ID o id en el reg del cliente
            client_id = int(reg.get('ID') or reg.get('id'))
            if not client_id:
                continue

            try:
                server_record = cls.objects.get(id=client_id).serialize()
                
            except cls.DoesNotExist:
                # Si no existe en servidor, eliminar del cliente
                result.append({
                    'tb': cls.__name__.lower(),
                    'op': 'rm',
                    'obj': {'id': client_id}
                })
                continue

            
            # Convertir reg del cliente a minúsculas keys y normalizar values
            client_normalized = {k.lower(): cls.normalize_value(v) for k, v in reg.items()}
            # Convertir registro del servidor a minúsculas keys y normalizar values
            server_normalized = {k.lower(): cls.normalize_value(v) for k, v in server_record.items()}
            
             # Comparar usando keys del cliente como referencia
            has_difference = False
            for key, value in client_normalized.items():
                if key in server_normalized and server_normalized[key] != value:
                    has_difference = True
                    break

            if has_difference:
                result.append({
                    'tb': cls.__name__.lower(),
                    'op': 'md',
                    'obj': server_record
                })

            ids_procesados.append(client_id)

        # Agregar inserts para registros del servidor no procesados
        server_records = cls.objects.exclude(id__in=ids_procesados)
        for server_record in server_records:
            result.append({
                'tb': cls.__name__.lower(),
                'op': 'insert',
                'obj': server_record.serialize()
            })

        return result

    class Meta:
        abstract = True
