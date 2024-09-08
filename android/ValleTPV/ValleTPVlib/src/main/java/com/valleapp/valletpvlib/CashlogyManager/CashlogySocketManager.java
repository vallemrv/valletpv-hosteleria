package com.valleapp.valletpvlib.CashlogyManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;


public class CashlogySocketManager {
    String cashlogyUrl;
    int cashlogyPort = 8092; // Ajusta este valor según el puerto utilizado por Cashlogy
    Socket socket;
    BufferedReader reader;
    OutputStream writer;
    Handler uiHandler;

    CashlogyAction currentAction;

    Timer initializationTimer = new Timer();

    public CashlogySocketManager(String url) {
        this.cashlogyUrl = url;
    }

    public void start() {
        new Thread(this::initializeSocket).start();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private void initializeSocket() {
        try {
            socket = new Socket(cashlogyUrl, cashlogyPort);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = socket.getOutputStream();

            Log.i("CASHLOGY", "Socket conectado exitosamente");  // Verifica que la conexión se haya realizado


        } catch (Exception e) {
            Log.e("CASHLOGY", "Error al inicializar Cashlogy: " + e.getMessage());
            e.printStackTrace();
            notifyUI("CASHLOGY_ERR","Error al conectar con Cashlogy");
        }
    }

    public void sendCommand(final String command) {
        if (!isConnected()) {
            Log.e("CASHLOGY", "El socket no está conectado, esperando...");
            waitForConnectionAndSend(command);
            return;
        }

        new Thread(() -> {
            try {
                writer.write((command + "\r\n").getBytes());
                writer.flush();

                // Leer la respuesta de manera no bloqueante
                char[] buffer = new char[4096];

                int charsRead = reader.read(buffer);

                if (charsRead != -1) {
                    String response = new String(buffer, 0, charsRead);
                    processResponse(command, response); // Pasa tanto el comando como la respuesta
                } else {
                    Log.e("CASHLOGY", "No se pudo leer la respuesta o el socket está cerrado.");
                }


            } catch (Exception e) {
                Log.e("CASHLOGY", "Error al enviar comando: " + e.getMessage(), e);
                e.printStackTrace();
            }

        }).start();
    }


    private void waitForConnectionAndSend(final String command) {
        new Thread(() -> {
            int attempts = 5; // Número de intentos de reintento
            while (attempts > 0 && !isConnected()) {
                try {
                    Thread.sleep(1000); // Espera 1 segundo antes de reintentar
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                attempts--;
            }

            if (isConnected()) {
                sendCommand(command); // Intenta enviar el comando nuevamente
            } else {
                Log.e("CASHLOGY", "No se pudo establecer la conexión después de varios intentos.");
                notifyUI("CASHLOGY_ERR", "Error: No se pudo establecer la conexión con Cashlogy.");
            }
        }).start();
    }


    private void processResponse(final String command, final String response) {
        if (response.startsWith("#0#")) {
            // Respuesta exitosa
            if (currentAction != null) {
                currentAction.handleResponse(command, response); // Pasar el comando y la respuesta a la acción actual
            }
        } else if (response.startsWith("#WR:")) {
            // Advertencia
            if (currentAction != null) {
                currentAction.handleResponse(command, response); // Pasar la advertencia a la acción actual
            }
        } else if (response.startsWith("#ER:")) {
            // Error
            currentAction.handleResponse(command, "CASHLOGY_ERR");
            handleErrors(command, response);
        } else {
            // Respuesta desconocida
            Log.e("CASHLOGY", "Respuesta desconocida: " + response);
        }
    }

    private void handleErrors(String command, String errorResponse) {
        String errorMessage;
        if (errorResponse.contains("GENERIC")) {
            errorMessage = "Error genérico: Problema en la comunicación o devolución.";
        } else if (errorResponse.contains("BAD_DATA")) {
            errorMessage = "Error de datos: Parámetros del comando enviados incorrectamente.";
        } else if (errorResponse.contains("BUSY")) {
            errorMessage = "Error de ocupación: El dispositivo está ocupado con otra operación.";
        } else if (errorResponse.contains("ILLEGAL")) {
            errorMessage = "Error de comando ilegal: El comando no puede realizarse en el estado actual.";
        } else {
            errorMessage = "Error desconocido: " + errorResponse;
        }

        // Notificar a la UI del error crítico
        notifyUI("CASHLOGY_ERR", errorMessage);

    }


    public void notifyUI(String key, String message) {
        if (uiHandler != null) {
            Message msg = uiHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("key", key);
            bundle.putString("value", message); // Poner el mensaje en un Bundle
            msg.setData(bundle);
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

        Log.i("CASHLOGY", "Servidor cerrado correctamente..... " );

    }

    public void setUiHandler(Handler handler) {
        this.uiHandler = handler;
    }

    public void setCurrentAction(CashlogyAction action) {
        this.currentAction = action;
    }
}
