# @Author: Manuel Rodriguez <valle>
# @Date:   11-Jun-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-02T18:53:16+01:00
# @License: Apache license vesion 2.0


from distutils.command.config import config
from re import M
from textwrap import indent
from docprint.impresora import DocPrint
import websocket
import json
import time
import threading
import os

file_config = "config.json"

class receptor_manager():

    def __init__(self, datos_empresa, datos_impresora):
        
        self.doc = DocPrint(datos_empresa, datos_impresora)

    def on_message(self, ws, msg):
        message = json.loads(msg)
        message = json.loads(message["message"])
        op = message["op"]
        
        if op == "open":
            ws.doc.abrir_cajon()
        elif op == "desglose":
            ws.doc.printDesglose(message["fecha"], message["lineas"])
        elif op == "ticket":
            if str(message["abrircajon"]).strip() == "True":
                ws.doc.abrir_cajon()
            if str(message["receptor_activo"]).strip() == "True":
                cambio = float(message['efectivo']) - float(message['total'])
                ws.doc.imprimirTicket(message['num'], message['camarero'], message['fecha'], message["mesa"],
                                   message['total'], message['efectivo'], cambio, message['lineas'])
        elif op == "pedido" and str(message["receptor_activo"]).strip() == "True":
            ws.doc.imprimirPedido(message["camarero"], message["mesa"], message["hora"], message["lineas"])
        elif op == "urgente" and str(message["receptor_activo"]).strip() == "True":
            ws.doc.imprimirUrgente(message["camarero"], message["mesa"], message["hora"], message["lineas"])
        elif op == "preticket":
            ws.doc.imprimirPreTicket(message["camarero"], message['numcopias'], message['fecha'],
                                  message["mesa"], message['lineas'], message['total'])


    def on_error(self, ws, error):
        print(error)

    def on_close(self, ws, a, b):
        print("### closed ###")

    def on_open(self, ws):
        print("### open ###")


def run_websocket(url, args):

    websocket.enableTrace(False)
        
    manager = receptor_manager(**args)

    
    ws = websocket.WebSocketApp(url,
                                on_message = manager.on_message,
                                on_error = manager.on_error,
                                on_close = manager.on_close)
    
    ws.doc = manager.doc
    ws.on_open = manager.on_open
    
    while True:
        ws.run_forever()
        time.sleep(2)
    
if __name__ == "__main__":

    #print_caja = {'usb':(0x1504,0x002b,0,0x81,0x02)}
    #print_cocina = {"usb":(0x20d1,0x7007,0,0x81,0x02)}
    #threading.Thread(target=run_websoker, args=("ws://localhost:8000/ws/impresion/caja/", print_caja)).start()
    #threading.Thread(target=run_websoker, args=("ws://localhost:8000/ws/impresion/cocina/", print_cocina)).start()
  
    base_path = os.path.dirname(__file__)
    f = open(os.path.join(base_path, file_config), "r")
    datos = json.loads(f.read())
    for imp in datos["impresoras"]:
        arg = {
            "datos_empresa": datos["datos_empresa"],
            "datos_impresora": imp
        }
        url_socket = "ws://"+datos["url_server"]+"/ws/impresion/"+imp["ws"]+"/"
        threading.Thread(target=run_websocket, 
                         args=(url_socket,
                         arg)).start()