package com.valleapp.valletpv.tools;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class CashlogySocketManager {
     String cashlogyUrl;
     int cashlogyPort = 8092; // Ajusta este valor según el puerto utilizado por Cashlogy
     Socket socket;
     BufferedReader reader;
     OutputStream writer;
     Handler uiHandler;

     Timer initializationTimer = new Timer();

    public CashlogySocketManager(String url, Handler handler) {
        this.cashlogyUrl = url;
        this.uiHandler = handler;
    }

    public void start() {
        new Thread(this::initializeSocket).start();
    }

    private void initializeSocket() {
        try {
            socket = new Socket(cashlogyUrl, cashlogyPort);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = socket.getOutputStream();

            // Enviar el comando de inicialización no bloqueante
            initializationTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendCommand("#I#"); // Comando de inicialización
                }
            }, 0);

            // Iniciar la recepción de datos en un hilo separado
            new Thread(this::listenForResponses).start();

        } catch (Exception e) {
            Log.e("CASHLOGY", "Error al inicializar Cashlogy: " + e.getMessage());
            e.printStackTrace();
            notifyUI("Error al conectar con Cashlogy");
        }
    }
    // Clase genérica para todas las acciones de Cashlogy
    public abstract class CashlogyAction {
        protected com.valleapp.valletpv.tools.CashlogyManager socketManager;

        public CashlogyAction(com.valleapp.valletpv.tools.CashlogyManager socketManager) {
            this.socketManager = socketManager;
        }

        public abstract void execute();
        public abstract void handleResponse(String response);
    }

    // Clase específica para inicialización
    public class InitializeAction extends CashlogyAction {

        public InitializeAction(com.valleapp.valletpv.tools.CashlogyManager socketManager) {
            super(socketManager);
        }

        @Override
        public void execute() {
            socketManager.sendCommand("#I#");
        }

        @Override
        public void handleResponse(String response) {
            if (response.contains("ACK")) {
                socketManager.notifyUI("Cashlogy inicializado correctamente");
            } else {
                socketManager.notifyUI("Error en la inicialización de Cashlogy");
            }
        }
    }

    // Clase específica para el comando de cobro, etc.
    public class PaymentAction extends CashlogyAction {
        private String amount;

        public PaymentAction(com.valleapp.valletpv.tools.CashlogyManager socketManager, String amount) {
            super(socketManager);
            this.amount = amount;
        }

        @Override
        public void execute() {
            socketManager.sendCommand("#C#" + amount + "#");
        }

        @Override
        public void handleResponse(String response) {
            if (response.contains("ACK")) {
                socketManager.notifyUI("Cobro realizado con éxito");
            } else {
                socketManager.notifyUI("Error en el cobro");
            }
        }
    }

    // En la clase principal o manejador
    public class CashlogyManager {
        private com.valleapp.valletpv.tools.CashlogyManager socketManager;

        public CashlogyManager(com.valleapp.valletpv.tools.CashlogyManager socketManager) {
            this.socketManager = socketManager;
        }

        public void initialize() {
            CashlogyAction action = new InitializeAction(socketManager);
            action.execute();
        }

        public void makePayment(String amount) {
            CashlogyAction action = new PaymentAction(socketManager, amount);
            action.execute();
        }
    }


    private void listenForResponses() {
        try {
            String response;
            while ((response = reader.readLine()) != null) {
                Log.i("CASHLOGY", "Respuesta recibida: " + response);
                // Procesar la respuesta recibida
                processResponse(response);
            }
        } catch (Exception e) {
            Log.e("CASHLOGY", "Error al recibir datos de Cashlogy: " + e.getMessage());
            e.printStackTrace();
            notifyUI("Error en la comunicación con Cashlogy");
        }
    }

    public void sendCommand(String command) {
        try {
            writer.write((command + "\r\n").getBytes());
            writer.flush();
            Log.i("CASHLOGY", "Comando enviado: " + command);
        } catch (Exception e) {
            Log.e("CASHLOGY", "Error al enviar comando: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processResponse(String response) {
        // Aquí puedes procesar la respuesta de Cashlogy y enviar mensajes a la UI si es necesario
        // Por ejemplo, si la respuesta es una confirmación de inicialización exitosa:
        if (response.contains("ACK")) {
            notifyUI("Cashlogy inicializado correctamente");
        }
        // Otros comandos pueden ser manejados aquí
    }

    private void notifyUI(String message) {
        if (uiHandler != null) {
            Message msg = uiHandler.obtainMessage();
            msg.obj = message;
            uiHandler.sendMessage(msg);
        }
    }

    public void stop() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (initializationTimer != null) {
                initializationTimer.cancel();
            }
        } catch (Exception e) {
            Log.e("CASHLOGY", "Error al cerrar CashlogySocketManager: " + e.getMessage());
        }
    }
}
