# Generated by Django 4.0.3 on 2022-05-24 00:50

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('gestion', '0004_alter_historialmensajes_fecha_and_more'),
    ]

    operations = [
        migrations.AlterField(
            model_name='composicionteclas',
            name='composicion',
            field=models.CharField(max_length=300),
        ),
    ]
