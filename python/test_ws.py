import websocket
import json

def on_message(ws, message):
    try:
        respuesta = json.loads(message)
        if "respuesta" in respuesta:
            print(respuesta["respuesta"])
        else:
            print("La clave 'respuesta' no está en el mensaje.")
    except json.JSONDecodeError:
        print("Error al decodificar el mensaje JSON.")

def on_error(ws, error):
    print(f"Error: {error}")

def on_close(ws, close_status_code, close_msg):
    print("Conexión cerrada")

def on_open(ws):
    print("Conexión abierta")
    # Envía un mensaje al WebSocket (si es necesario)
    ws.send(json.dumps({"content": '{"instruccion":"#I#"}'}))

if __name__ == "__main__":
    websocket.enableTrace(False)
    ws = websocket.WebSocketApp("ws://tpv1.valletpv.es/ws/comunicacion/cashlogy",
                                on_open=on_open,
                                on_message=on_message,
                                on_error=on_error,
                                on_close=on_close)

    ws.run_forever()
