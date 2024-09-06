package com.valleapp.valletpvlib.comunicacion.CashlogyManager;


import java.util.HashMap;
import java.util.Map;

public class ArqueoAction extends CashlogyAction {

    Map<Integer, Integer> denominacionesDisponibles = new HashMap<>();

    double totalRecicladores = 0;
    double totalAlmacenes = 0;
    double cambio = 0;

    public ArqueoAction(CashlogySocketManager socketManager) {
        super(socketManager);
    }

    @Override
    public void execute() {
        // Envía el comando para obtener todas las denominaciones en Cashlogy
        socketManager.sendCommand("#Y#");
    }


    @Override
    public void handleResponse(String comando, String response) {
        if (comando.startsWith("#Y")) {
            manejarRespuestaDenominaciones(response);
        }else if (comando.startsWith("#U")){
            socketManager.notifyUI("CASHLOGY_CASH", "");
        }
    }

    private void procesarRespuestaCambios() {
        // Comparar el total de recicladores con el cambio esperado
        if (totalRecicladores > cambio) {
            double excessAmount = totalRecicladores - cambio;

            // Inicializar el StringBuilder para formar el comando #U#
            StringBuilder uCommandBuilder = new StringBuilder("#U#;");

            // Variable para verificar si se han añadido denominaciones al comando
            boolean hasDenominations = false;

            // Procesar cada denominación comenzando por la más alta
            if (excessAmount >= 20) {
                int num20s = (int) (excessAmount / 20);
                if (num20s > 0) {
                    uCommandBuilder.append("2000:").append(num20s).append(",");
                    totalAlmacenes += num20s * 20;
                    totalRecicladores -= num20s * 20;
                    excessAmount -= num20s * 20;
                    hasDenominations = true;
                }
            }
            if (excessAmount >= 10) {
                int num10s = (int) (excessAmount / 10);
                if (num10s > 0) {
                    uCommandBuilder.append("1000:").append(num10s).append(",");
                    totalAlmacenes += num10s * 10;
                    totalRecicladores -= num10s * 10;
                    excessAmount -= num10s * 10;
                    hasDenominations = true;
                }
            }
            if (excessAmount >= 5) {
                int num5s = (int) (excessAmount / 5);
                if (num5s > 0) {
                    uCommandBuilder.append("500:").append(num5s).append(",");
                    totalAlmacenes += num5s * 5;
                    totalRecicladores -= num5s * 5;
                    hasDenominations = true;
                }
            }

            // Si se añadieron denominaciones al comando, enviarlo
            if (hasDenominations) {
                // Eliminar la última coma en la cadena si es necesario
                if (uCommandBuilder.charAt(uCommandBuilder.length() - 1) == ',') {
                    uCommandBuilder.deleteCharAt(uCommandBuilder.length() - 1);
                }

                // Completar el comando con el formato correcto
                uCommandBuilder.append("#1#0#0#");


                // Enviar el command para mover el exceso al stacker
                socketManager.sendCommand(uCommandBuilder.toString());
            } else {
                socketManager.notifyUI("CASHLOGY_CASH", "");
            }
        } else {
            socketManager.notifyUI("CASHLOGY_CASH", "");
        }
    }

    private void manejarRespuestaDenominaciones(String response) {
        try {
            String[] parts = response.split("#");

            if (parts.length < 3) {
                socketManager.notifyUI("CASHLOGY_ERR", "Respuesta de denominaciones no válida.");
                return;
            }

            // Parte b de la respuesta: Monedas/Billetes en recicladores
            String recicladoresParte = parts[2];
            // Parte c de la respuesta: Billetes en almacenes no devolvibles (stacker + caja fuerte)
            String almacenesParte = parts[3];

            // Limpiar el mapa antes de rellenarlo
            denominacionesDisponibles.clear();
            totalAlmacenes = 0;
            totalRecicladores = 0;

            // Procesar recicladores (Parte b)
            String[] seccionesRecicladores = recicladoresParte.split(";");
            String[] recicladoresMonedas = seccionesRecicladores[0].split(",");
            String[] recicladoresBilletes = seccionesRecicladores[1].split(",");

            // Procesar monedas en recicladores
            for (String reciclador : recicladoresMonedas) {
                String[] denomination = reciclador.split(":");
                if (denomination.length == 2) {
                    int valorEnCentimos = Integer.parseInt(denomination[0]);
                    int cantidad = Integer.parseInt(denomination[1]);

                    // Almacenar en el mapa de denominaciones disponibles
                    denominacionesDisponibles.put(valorEnCentimos, cantidad);

                    // Sumar al total de recicladores
                    totalRecicladores += (valorEnCentimos * cantidad) / 100.0;
                }
            }

            // Procesar billetes en recicladores
            for (String reciclador : recicladoresBilletes) {
                String[] denomination = reciclador.split(":");
                if (denomination.length == 2) {
                    int valorEnCentimos = Integer.parseInt(denomination[0]);
                    int cantidad = Integer.parseInt(denomination[1]);

                    // Almacenar en el mapa de denominaciones disponibles
                    denominacionesDisponibles.put(valorEnCentimos, cantidad);

                    // Sumar al total de recicladores
                    totalRecicladores += (valorEnCentimos * cantidad) / 100.0;
                    if (valorEnCentimos > 2000) break;

                 }
            }

            // Procesar almacenes no devolvibles (Parte c)
            String[] seccionesAlmacenes = almacenesParte.split(";");
            String[] almacenesBilletes = seccionesAlmacenes[1].split(",");



            // Procesar billetes en almacenes
            for (String almacen : almacenesBilletes) {
                String[] denomination = almacen.split(":");
                if (denomination.length == 2) {
                    int valorEnCentimos = Integer.parseInt(denomination[0]);
                    int cantidad = Integer.parseInt(denomination[1]);

                    // Calcular la suma total del stacker
                    totalAlmacenes += valorEnCentimos * cantidad / 100.0;

                 }
            }

            // Notificar a la UI que las denominaciones están listas
            socketManager.notifyUI("CASHLOGY_DENOMINACIONES_LISTAS", "Las denominaciones están listas para su uso.");

        } catch (Exception e) {
            socketManager.notifyUI("CASHLOGY_ERR", "Error al manejar la respuesta de denominaciones.");
        }
    }


    public Map<Integer, Integer> getDenominaciones() {
        return denominacionesDisponibles;
    }

    public  void cerrarCashlogy(){
        procesarRespuestaCambios();
    }

    // Manejo de acceso a cambio y stacker
    public synchronized void setCambioStacker(double cambio) {
        this.cambio = cambio;
    }

    public double getTotalRecicladores() {
        return totalRecicladores;
    }

    public double getTotalAlmacenes() {
        return totalAlmacenes;
    }

    public void cashLogyCerrado() {
        socketManager.notifyUI("CASHLOGY_CIERRE_COMPLETADO", "");
    }
}
