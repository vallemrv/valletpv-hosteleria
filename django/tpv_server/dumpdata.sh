#!/bin/bash

if [ "$#" -lt "2" ]; then
    echo "Uso dumpdata.sh <fichero_manage.py> <fichero_salida.json>"
    exit
else
    python $1 dumpdata gestion.camareros gestion.zonas gestion.mesas gestion.mesaszona gestion.receptores gestion.familias gestion.secciones gestion.teclas gestion.subteclas gestion.sugerencias gestion.seccionescom gestion.teclascom gestion.teclaseccion  > $2
fi
