#!/bin/bash

# Script daemon para monitorear cambios y reiniciar el servicio
# Uso: ./watch_and_reload.sh [start|stop|status]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="$SCRIPT_DIR/.watch_daemon.pid"
LOG_FILE="$SCRIPT_DIR/logs/watch_daemon.log"
SERVICE_NAME="valletpv-test.service"
WATCH_DIR="$SCRIPT_DIR"

# Crear directorio de logs si no existe
mkdir -p "$SCRIPT_DIR/logs"

# Función para escribir en el log
log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Función para verificar si inotify-tools está instalado
check_inotify() {
    if ! command -v inotifywait &> /dev/null; then
        echo "Error: inotify-tools no está instalado."
        echo "Instálalo con: sudo apt-get install inotify-tools"
        exit 1
    fi
}

# Función para iniciar el daemon
start_daemon() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "El daemon ya está ejecutándose (PID: $PID)"
            exit 1
        else
            rm -f "$PID_FILE"
        fi
    fi

    check_inotify

    log_message "Iniciando daemon de monitoreo..."
    log_message "Monitoreando directorio: $WATCH_DIR"
    
    # Iniciar el daemon en background
    nohup bash -c '
        WATCH_DIR="'"$WATCH_DIR"'"
        LOG_FILE="'"$LOG_FILE"'"
        SERVICE_NAME="'"$SERVICE_NAME"'"
        
        log_message() {
            echo "[$(date "+%Y-%m-%d %H:%M:%S")] $1" >> "$LOG_FILE"
        }
        
        log_message "Daemon iniciado correctamente"
        
        # Monitorear cambios en archivos Python, plantillas y configuración
        inotifywait -m -r -e modify,create,delete,move \
            --exclude "\.git|__pycache__|\.pyc$|\.log$|\.pid$|\.sock$|\.env|node_modules|\.sqlite3" \
            --format "%T %e %w%f" --timefmt "%H:%M:%S" \
            "$WATCH_DIR" | while read time event file
        do
            # Filtrar solo archivos relevantes
            if [[ "$file" =~ \.(py|html|json|yaml|yml|txt|sh|js|vue|ts|css)$ ]]; then
                log_message "Cambio detectado: $event en $file"
                log_message "Reiniciando servicio $SERVICE_NAME..."
                
                if sudo systemctl restart "$SERVICE_NAME"; then
                    log_message "Servicio reiniciado correctamente"
                else
                    log_message "ERROR: Fallo al reiniciar el servicio"
                fi
                
                # Esperar 2 segundos antes de procesar más cambios
                sleep 2
            fi
        done
    ' > /dev/null 2>&1 &
    
    echo $! > "$PID_FILE"
    log_message "Daemon iniciado con PID: $(cat $PID_FILE)"
    echo "Daemon iniciado con PID: $(cat $PID_FILE)"
    echo "Logs en: $LOG_FILE"
}

# Función para detener el daemon
stop_daemon() {
    if [ ! -f "$PID_FILE" ]; then
        echo "El daemon no está ejecutándose"
        exit 1
    fi

    PID=$(cat "$PID_FILE")
    
    if ps -p "$PID" > /dev/null 2>&1; then
        log_message "Deteniendo daemon (PID: $PID)..."
        
        # Matar el proceso y todos sus hijos
        pkill -P "$PID"
        kill "$PID" 2>/dev/null
        
        # Esperar a que termine
        sleep 1
        
        if ps -p "$PID" > /dev/null 2>&1; then
            kill -9 "$PID" 2>/dev/null
        fi
        
        rm -f "$PID_FILE"
        log_message "Daemon detenido"
        echo "Daemon detenido"
    else
        echo "El daemon no está ejecutándose (PID obsoleto: $PID)"
        rm -f "$PID_FILE"
    fi
}

# Función para mostrar el estado
status_daemon() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "Daemon ejecutándose (PID: $PID)"
            echo "Log: $LOG_FILE"
            echo ""
            echo "Últimas 10 líneas del log:"
            tail -n 10 "$LOG_FILE"
        else
            echo "Daemon no está ejecutándose (PID obsoleto: $PID)"
            rm -f "$PID_FILE"
        fi
    else
        echo "Daemon no está ejecutándose"
    fi
}

# Procesar comando
case "$1" in
    start)
        start_daemon
        ;;
    stop)
        stop_daemon
        ;;
    restart)
        stop_daemon
        sleep 1
        start_daemon
        ;;
    status)
        status_daemon
        ;;
    logs)
        if [ -f "$LOG_FILE" ]; then
            tail -n 50 "$LOG_FILE"
        else
            echo "No hay logs disponibles"
        fi
        ;;
    follow)
        if [ -f "$LOG_FILE" ]; then
            tail -f "$LOG_FILE"
        else
            echo "No hay logs disponibles"
        fi
        ;;
    *)
        echo "Uso: $0 {start|stop|restart|status|logs|follow}"
        echo ""
        echo "Comandos disponibles:"
        echo "  start   - Iniciar el daemon de monitoreo"
        echo "  stop    - Detener el daemon"
        echo "  restart - Reiniciar el daemon"
        echo "  status  - Ver estado del daemon"
        echo "  logs    - Ver últimos 50 logs del daemon"
        echo "  follow  - Seguir logs del daemon en tiempo real"
        exit 1
        ;;
esac

exit 0
