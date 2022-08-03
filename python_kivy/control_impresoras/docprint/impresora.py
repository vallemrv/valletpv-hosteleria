# -*- coding: utf-8 -*-
# @Author: Manuel Rodriguez <valle>
# @Date:   10-May-2017
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-27T23:14:10+01:00
# @License: Apache license vesion 2.0

from __future__ import unicode_literals
from datetime import datetime

from escpos.printer import Network, Usb, File
from escpos.escpos import EscposIO
from escpos import escpos
import locale

import sys



class DocPrint():

    def __init__(self, datos_empresa, datos_impresora):
        self._datos = datos_empresa
        self.tipo = datos_impresora["tipo"]
        self.params = datos_impresora["params"]
       

    def abrir_cajon(self,  *args):
        try:
            if self.tipo == "Network":
                printer = Network(self.params, timeout=1)
            if self.tipo == "Usb":
                printer = Usb(*self.params)
            if self.tipo == "File":
                printer = File(self.params)

            printer.cashdraw(2)
        except Exception as e:
            print("[ERROR  ] %s" % e)

    def printMensaje(self, camarero, msg):
    
        try:
            if self.tipo == "Network":
                printer = Network(self.params, timeout=1)
            if self.tipo == "Usb":
                printer = Usb(*self.params)
            if self.tipo == "File":
                printer = File(self.params)



            with EscposIO(printer) as p:
                p.printer.codepage = 'cp858'
                
                p.writelines("Mensajito", align='center', width=2, height=2)
                p.writelines(camarero, height=2, width=2, font='a', align='center')
                p.writelines("------------------------------------------",
                              align='center')
                p.writelines("")
                p.writelines(msg, align='center',  height=2)
                p.writelines("")
                p.writelines("")

        except Exception as e:
            print("[ERROR  ] %s" % e)

    def printDesglose(self, fecha, lineas):

        if type(fecha) is datetime:
            fecha = fecha.strftime("%d/%m/%Y %H:%M:%S")
        else:
            fecha = datetime.strptime(fecha, "%Y-%m-%d %H:%M:%S.%f")
            fecha = fecha.strftime("%d/%m/%Y %H:%M:%S")


        try:
            if self.tipo == "Network":
                printer = Network(self.params, timeout=1)
            if self.tipo == "Usb":
                printer = Usb(*self.params)
            if self.tipo == "File":
                printer = File(self.params)



            with EscposIO(printer) as p:
                p.printer.codepage = 'cp858'
                
                p.writelines("Cierre de caja", align='center', width=2, height=2)

                p.writelines(fecha, align='center', width=2, height=2)
                p.writelines("------------------------------------------",
                              align='center')
                p.writelines("")
                for linea in lineas:
                    can = linea["can"]
                    texto_tipo = linea["texto_tipo"]
                    tipo = linea["tipo"]
                    titulo = linea["titulo"]
                    p.writelines("{3} {0: >5} {1: <3} de {2:6.2f} €".format(can, texto_tipo,
                                                                     tipo, titulo ),height=2,align='center')

                p.writelines("")
                p.writelines("")
        except Exception as e:
            print("[ERROR  ] %s" % e)

    def imprimirTicket(self, num, camarero,  fecha, mesa, total, efectivo, cambio, lineas, url_factura=None):

        if type(fecha) is datetime:
            fecha = fecha.strftime("El %a %d-%B a las (%H:%M)")
        else:
            fecha = datetime.strptime(fecha, "%Y/%m/%d %H:%M")
            fecha = fecha.strftime("El %a %d-%B a las (%H:%M)")

        try:

            if self.tipo == "Network":
                printer = Network(self.params, timeout=1)
            if self.tipo == "Usb":
                printer = Usb(*self.params)
            if self.tipo == "File":
                printer = File(self.params)



            with EscposIO(printer) as p:
                p.printer.codepage = 'cp858'
                p.printer._raw(escpos.BEEP)
                p.printer.image(self._datos["url_logo"])
                p.writelines("")
                p.writelines(self._datos["nombre_empresa"], font='A', align='center')
                
                p.writelines(self._datos["direccion"]["calle"], font='a', align='center')
                p.writelines(self._datos["direccion"]["cp"]+ " "+ self._datos["direccion"]["provincia"], font='a', align='center')
                p.writelines('NIF: '+ self._datos["nif"], font='A', align='center')
                p.writelines('------------------------------------------', align='center')
                p.writelines(fecha, height=2, width=1, font='b', align='center')
                p.writelines("Num Ticket: %d" % num,font='a', align='center')
                p.writelines("Camarero: %s" % camarero,  font='a', align='center')
                p.writelines("Mesa: %s" % mesa,  font='a', align='center')
                p.writelines('------------------------------------------', align='center')


                for ln in lineas:
                    p.writelines("{0: >3} {1: <20} {2:5.2f} € {3:6.2f} €".format(ln['can'], ln['descripcion_t'],
                                  float(ln['precio']), float(ln['totallinea'])), density=1, align='center')


                p.writelines("",  text_type='bold',  font='b', align='center')
                p.writelines("Total: {0:0.2f} €".format(float(total)),
                              align='right', height=2)
                if float(efectivo) > 0:
                    p.writelines("Efectivo: {0:0.2f} €".format(float(efectivo)),
                                align='right')
                    p.writelines("Cambio: {0:0.2f} €".format(float(cambio)),
                                align='right', )
                else:
                    p.writelines("")
                    p.writelines("Pago con tarjeta", align='right')

                p.writelines("",  text_type='bold',  font='a', align='center')
                p.writelines("Factura simplificada",  text_type='bold',  font='a', align='center')
                p.writelines("Iva incluido",  text_type='bold', font='a',  align='center')
                p.writelines("Gracias por su visita",font='a',   align='center')
                p.writelines("TLF: "+ self._datos["telefono"], text_type='bold', height=2, font='a',   align='center')

                if url_factura != "":
                    p.writelines("",  text_type='bold',  font='a', align='center')
                    p.writelines("",  text_type='bold',  font='a', align='center')
                    p.writelines("Escanea para la factura",  text_type='bold',  font='a', align='center')
                    p.qr(url_factura)


        except Exception as e:
            print("[ERROR  ] %s" % e)

    def imprimirPedido(self, camarero, mesa, hora, lineas, is_urgente=False):
        
        try:
            if self.tipo == "Network":
                printer = Network(self.params, timeout=1)
            if self.tipo == "Usb":
                printer = Usb(*self.params)
            if self.tipo == "File":
                printer = File(self.params)
            
            with EscposIO(printer) as p:
                p.printer.codepage = 'cp858'
               
                p.printer.set(align='center')
                if (is_urgente):
                    p.writelines('URGENTE!!', height=2, width=2, font='a', align='center')    
                p.writelines("")
                p.writelines('------------------------------------------', align='center')
                p.writelines('HORA: %s' % hora, height=2, width=2, font='b', align='center')
                p.writelines("Mesa: %s" % mesa, height=2, width=2, font='a', align='center')
                p.writelines(camarero, height=2, width=2, font='a', align='center')
                p.writelines('------------------------------------------', align='center')

                p.writelines("")
                for ln in lineas:
                    p.writelines("{0: >3} {1: <25} {2}".format(ln['can'], ln['descripcion'],
                                  ln["estado"]), height=2, align='center' )


                p.writelines("")
                p.writelines("")
        except Exception as e:
            print("[ERROR  ] %s" % e)

    def imprimirPreTicket(self, camarero, numcopias, fecha, mesa, lineas, total):
        
        if type(fecha) is datetime:
            fecha = fecha.strftime("El %a %d-%B a las (%H:%M)")
        else:
            fecha = datetime.strptime(fecha, "%Y-%m-%d %H:%M:%S.%f")
            fecha = fecha.strftime("El %a %d-%B a las (%H:%M)")

        try:
            if self.tipo == "Network":
                printer = Network(self.params, timeout=1)
            if self.tipo == "Usb":
                printer = Usb(*self.params)
            if self.tipo == "File":
                printer = File(self.params)

            with EscposIO(printer) as p:
                p.printer.codepage = 'cp858'
                
                p.printer.set(align='center')
                p.writelines('PRETICKET', font='a', height=2, align='center')
                p.writelines('')
                p.writelines('------------------------------------------', align='center')
                p.writelines('FECHA', height=2, width=2, font='a', align='center')
                p.writelines(fecha, height=2, width=1, font='b', align='center')
                p.writelines("Num copias: %d" % numcopias, font='a', align='center')
                p.writelines("Mesa: %s" % mesa,  font='a', align='center')
                p.writelines('------------------------------------------', align='center')

                for ln in lineas:
                    p.writelines("{0: >3} {1: <20} {2:5.2f} € {3:6.2f} €".format(ln['can'], ln['descripcion_t'],
                                  float(ln['precio']), float(ln['totallinea'])), align='center', font="a")

                p.writelines("")
                p.writelines("Total: {0:0.2f} €".format(float(total)),
                              align='right', height=3)
                p.writelines("")
                p.writelines("No olvide pedir su ticket",  text_type='bold', height=2, align='center')
                p.writelines(camarero, text_type='bold', font='a', align='center')

                p.writelines("")
                p.writelines("")
        except Exception as e:
            print("[ERROR  ] %s" % e)

    def test_print(self):
        print("Test print console")
