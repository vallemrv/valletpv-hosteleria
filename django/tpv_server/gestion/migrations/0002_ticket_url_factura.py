# Generated by Django 4.0.3 on 2022-08-02 22:38

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('gestion', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='ticket',
            name='url_factura',
            field=models.CharField(default='', max_length=140),
        ),
    ]