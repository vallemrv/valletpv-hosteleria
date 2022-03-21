# @Author: Manuel Rodriguez <valle>
# @Date:   11-Jun-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-02T18:53:16+01:00
# @License: Apache license vesion 2.0


import websocket
import json
import time
import threading

class receptor_manager():

    def __init__(self, usb=None, url=None, ip_caja=None):
        from tpv.impresora import DocPrint
        self.doc = DocPrint(usb=usb,url=url,ip_caja=ip_caja)

    def on_message(ws, message):
        message = json.loads(message)["message"]
        op = message["op"]
        if op == "open":
            ws.doc.abrir_cajon()
        elif op == "desglose":
            ws.doc.printDesglose(message["fecha"], message["lineas"])
        elif op == "ticket":
            if str(message["abrircajon"]).strip() == "True":
                ws.doc.abrir_cajon()
            if str(message["receptor_activo"]).strip() == "True"
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


    def on_error(ws, error):
        print(error)

    def on_close(ws):
        print("### closed ###")

    def on_open(ws):
        pass


def run_websoker(url, args):

    websocket.enableTrace(True)
    manager = receptor_manager(**args)

    ws = websocket.WebSocketApp(url,
                                on_message = manager.on_message,
                                on_error = manager.on_error,
                                on_close = manager.on_close)
    ws.doc = manager
    ws.on_open = manager.on_open

    while True:
        ws.run_forever()
        time.sleep(2)

if __name__ == "__main__":
    print_caja = {'usb':(0x1504,0x002b,0,0x81,0x02)}
    print_cocina = {"usb":(0x20d1,0x7007,0,0x81,0x02)}
    threading.Thread(target=run_websoker, args=("ws://tpvtr.valleapp.com/ws/impresion/caja/", print_caja)).start()
    threading.Thread(target=run_websoker, args=("ws://tpvtr.valleapp.com/ws/impresion/cocina/", print_cocina)).start()
