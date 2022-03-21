#!/bin/bash


if [  "$#" -lt "2" ]
then
    echo "Uso loaddata.sh <fichero_manage.py> <fichero_entrada.json>"
    exit
else
    python $1 loaddata $2
fi

