# Generated by Django 4.1 on 2023-05-09 01:02

from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='InfModelos',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('nombre', models.CharField(max_length=100)),
                ('sinonimos', models.TextField(blank=True)),
                ('columnas', models.TextField(blank=True)),
                ('relaciones', models.TextField(blank=True)),
            ],
        ),
    ]
