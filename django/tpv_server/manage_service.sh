#!/bin/bash

# Script para gestionar el servicio valletpv-test.service
# Uso: ./manage_service.sh [start|stop|restart|status]

SERVICE_NAME="valletpv-test.service"

case "$1" in
    start)
        echo "Iniciando servicio $SERVICE_NAME..."
        sudo systemctl start $SERVICE_NAME
        sudo systemctl status $SERVICE_NAME --no-pager
        ;;
    stop)
        echo "Deteniendo servicio $SERVICE_NAME..."
        sudo systemctl stop $SERVICE_NAME
        sudo systemctl status $SERVICE_NAME --no-pager
        ;;
    restart)
        echo "Reiniciando servicio $SERVICE_NAME..."
        sudo systemctl restart $SERVICE_NAME
        sudo systemctl status $SERVICE_NAME --no-pager
        ;;
    status)
        sudo systemctl status $SERVICE_NAME --no-pager
        ;;
    enable)
        echo "Habilitando servicio $SERVICE_NAME para inicio automático..."
        sudo systemctl enable $SERVICE_NAME
        ;;
    disable)
        echo "Deshabilitando servicio $SERVICE_NAME del inicio automático..."
        sudo systemctl disable $SERVICE_NAME
        ;;
    logs)
        echo "Mostrando logs del servicio $SERVICE_NAME..."
        sudo journalctl -u $SERVICE_NAME -n 50 --no-pager
        ;;
    follow)
        echo "Siguiendo logs del servicio $SERVICE_NAME (Ctrl+C para salir)..."
        sudo journalctl -u $SERVICE_NAME -f
        ;;
    *)
        echo "Uso: $0 {start|stop|restart|status|enable|disable|logs|follow}"
        echo ""
        echo "Comandos disponibles:"
        echo "  start   - Iniciar el servicio"
        echo "  stop    - Detener el servicio"
        echo "  restart - Reiniciar el servicio"
        echo "  status  - Ver estado del servicio"
        echo "  enable  - Habilitar inicio automático"
        echo "  disable - Deshabilitar inicio automático"
        echo "  logs    - Ver últimos 50 logs"
        echo "  follow  - Seguir logs en tiempo real"
        exit 1
        ;;
esac

exit 0
