from django.db import models
from .basemodels import BaseModels
from django.contrib.auth.models import User

class HorarioUsr(BaseModels):
    hora_ini = models.CharField(db_column='Hora_ini', max_length=5)  # Field name made lowercase.
    hora_fin = models.CharField(db_column='Hora_fin', max_length=5)  # Field name made lowercase.
    usurario = models.ForeignKey(User,  on_delete=models.CASCADE, db_column='IDUsr')  # Field name made lowercase.

    class Meta:
        db_table = 'horario_usr'