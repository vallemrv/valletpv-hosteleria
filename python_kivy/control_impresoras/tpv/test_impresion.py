# -*- coding: utf-8 -*-
# @Author: Manuel Rodriguez <valle>
# @Date:   10-May-2017
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-27T23:14:53+01:00
# @License: Apache license vesion 2.0

from __future__ import unicode_literals
from datetime import datetime
import sys

ip_caja = '192.168.1.8'

class TestDocPrint():

    def __init__(self, ip_caja=None, usb=None, url=None):
        print(ip_caja,usb, url)
        if ip_caja!=None:
            self.ip_caja = ip_caja
            self.tipo = "Network"
        if usb!=None:
            self.usb = usb
            self.tipo = "Usb"
        if url!=None:
            self.url = url
            self.tipo = "File"

    def abrir_cajon(self,  *args):
        print("abrir_cajon ")

    def printDesglose(self, fecha, lineas):
        print("Desglose ", fecha, lineas)

    def imprimirTicket(self, num, camarero,  fecha, mesa, total, efectivo, cambio, lineas):
        print("imprimirTicket ", num, camarero, fecha, mesa, total, efectivo, cambio, lineas)

    def imprimirPedido(self, camarero, mesa, hora, lineas):
        print("imprimirPedido ", camarero, mesa, hora, lineas)

    def imprimirUrgente(self, camarero, mesa, hora, lineas):
        print("imprimirUrgente ", camarero, mesa, hora, lineas)

    def imprimirPreTicket(self, camarero, numcopias, fecha, mesa, lineas, total):
        print("imprimirPreTicket ", camarero, numcopias, fecha, mesa, lineas, total)

    def test_print(self):
        print("Test print console")
