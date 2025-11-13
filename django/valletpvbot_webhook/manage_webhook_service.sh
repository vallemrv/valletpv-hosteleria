#!/bin/bash
# Script de gestión del servicio Valle TPV Bot Webhook
# Autor: Valle
# Uso: ./manage_webhook_service.sh [start|stop|restart|status|install|logs]

SERVICE_NAME="valletpvbot_webhook"
SERVICE_FILE="$SERVICE_NAME.service"
SYSTEMD_PATH="/etc/systemd/system/$SERVICE_FILE"
PROJECT_DIR="/home/valle/proyectos/valletpv-hosteleria/django/valletpvbot_webhook"
LOG_DIR="$PROJECT_DIR/logs"

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para imprimir mensajes
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Verificar que existe el archivo .service
if [ ! -f "$PROJECT_DIR/$SERVICE_FILE" ]; then
    print_error "Archivo $SERVICE_FILE no encontrado en $PROJECT_DIR"
    exit 1
fi

# Crear directorio de logs si no existe
mkdir -p "$LOG_DIR"

case "$1" in
    install)
        print_info "Instalando servicio $SERVICE_NAME..."
        
        # Copiar archivo de servicio
        sudo cp "$PROJECT_DIR/$SERVICE_FILE" "$SYSTEMD_PATH"
        
        # Recargar systemd
        sudo systemctl daemon-reload
        
        # Habilitar el servicio para que arranque al inicio
        sudo systemctl enable "$SERVICE_NAME"
        
        print_info "Servicio instalado correctamente"
        print_info "Usa: sudo systemctl start $SERVICE_NAME para iniciarlo"
        ;;
    
    start)
        print_info "Iniciando servicio $SERVICE_NAME..."
        sudo systemctl start "$SERVICE_NAME"
        sleep 2
        sudo systemctl status "$SERVICE_NAME" --no-pager
        ;;
    
    stop)
        print_info "Deteniendo servicio $SERVICE_NAME..."
        sudo systemctl stop "$SERVICE_NAME"
        ;;
    
    restart)
        print_info "Reiniciando servicio $SERVICE_NAME..."
        sudo systemctl restart "$SERVICE_NAME"
        sleep 2
        sudo systemctl status "$SERVICE_NAME" --no-pager
        ;;
    
    status)
        sudo systemctl status "$SERVICE_NAME" --no-pager
        ;;
    
    logs)
        print_info "Mostrando logs del servicio (Ctrl+C para salir)..."
        sudo journalctl -u "$SERVICE_NAME" -f
        ;;
    
    logs-error)
        print_info "Mostrando logs de errores..."
        if [ -f "$LOG_DIR/error.log" ]; then
            tail -f "$LOG_DIR/error.log"
        else
            print_error "Archivo de logs de errores no encontrado"
        fi
        ;;
    
    logs-access)
        print_info "Mostrando logs de acceso..."
        if [ -f "$LOG_DIR/access.log" ]; then
            tail -f "$LOG_DIR/access.log"
        else
            print_error "Archivo de logs de acceso no encontrado"
        fi
        ;;
    
    uninstall)
        print_warning "Desinstalando servicio $SERVICE_NAME..."
        sudo systemctl stop "$SERVICE_NAME"
        sudo systemctl disable "$SERVICE_NAME"
        sudo rm -f "$SYSTEMD_PATH"
        sudo systemctl daemon-reload
        print_info "Servicio desinstalado"
        ;;
    
    *)
        echo "Uso: $0 {install|start|stop|restart|status|logs|logs-error|logs-access|uninstall}"
        echo ""
        echo "Comandos disponibles:"
        echo "  install      - Instala el servicio en systemd"
        echo "  start        - Inicia el servicio"
        echo "  stop         - Detiene el servicio"
        echo "  restart      - Reinicia el servicio"
        echo "  status       - Muestra el estado del servicio"
        echo "  logs         - Muestra logs en tiempo real (systemd)"
        echo "  logs-error   - Muestra logs de errores (gunicorn)"
        echo "  logs-access  - Muestra logs de acceso (gunicorn)"
        echo "  uninstall    - Desinstala el servicio"
        exit 1
        ;;
esac

exit 0
