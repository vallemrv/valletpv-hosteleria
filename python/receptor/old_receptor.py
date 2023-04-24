# @Author: Manuel Rodriguez <valle>
# @Date:   2018-10-12T18:46:37+02:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-10T17:27:35+02:00
# @License: Apache License v2.0

import kivy
from kivy import platform
from kivy.app import App
from kivy.uix.popup import Popup
from kivy.uix.anchorlayout import AnchorLayout
from kivy.properties import (ObjectProperty, BooleanProperty,
                             StringProperty, ListProperty, NumericProperty)
from kivy.lang import Builder
from kivy.clock import Clock
from kivy.config import Config
from kivy.network.urlrequest import  UrlRequest
from components.listview import MenuListView
from components.pagenavigations import  Page
from ws_connect import WSManager
from components.resources import Res as res
import urllib
import os
import json
import threading

Config.set("graphics", "width", 1024)

class Mensajes(AnchorLayout):
    art = StringProperty("")
    def __init__(self, **karg):
        super(Mensajes, self).__init__(**karg)
        if 'art' in karg:
            self.art = karg["art"]
        self.pos_hint = {"top": 10}

    def fallo(self, r, e):
        print(r,e)


    def enviar(self, texto):
        f = open('receptor.dat', "r")
        datos = json.loads(f.read())
        url = datos["url"]
        f.close()
        url="http://"+os.path.join(url,"sync","send_test_mensaje",urllib.parse.quote(texto))
        UrlRequest(on_success=self.recibido,on_error=self.fallo, on_failure=self.fallo,
                   url=url)

    def recibido(self, r, res):
        if (res=='success'):
            self.close()

    def close(self):
        if self.parent:
            self.parent.remove_widget(self)

class LineasWidget(AnchorLayout):
    controller = ObjectProperty()
    esta_servido = BooleanProperty(False)
    bg_color = ObjectProperty('#85C1E9')

    def borrar_linea(self):
        self.controller.lista.rm_linea(self)

    def send_mensaje(self, texto):
        self.controller.show_mensajes(texto)

    def servido(self):
        if not self.esta_servido:
            self.bg_color = '#F1948A'
            self.esta_servido = True
        else:
            self.bg_color = '#85C1E9'
            self.esta_servido = False

class PedidoWidget(AnchorLayout):
    lista = ObjectProperty()
    controller = ObjectProperty()

    def servir(self):
        for l in self.lista.get_lineas():
            l.bg_color = '#F1948A'

    def show_mensajes(self, texto):
        self.controller.show_mensajes(texto)


    def borrar_pedido(self):
        self.controller.rm_pedido()
        self.controller.content.remove_widget(self)

class LineasReceptorWidget(AnchorLayout):
    texto = StringProperty()
    icon = StringProperty()
    tag = StringProperty()

class ListaReceptorPage(Page):
    datos = ObjectProperty()
    lista_receptores = ListProperty()


    def select(self, obj):
        if obj.tag in self.datos['lista']:
            self.datos['lista'].remove(obj.tag)
            obj.icon =  res.FA_EYE_SLASH
        else:
            obj.icon = res.FA_EYE
            self.datos['lista'].append(obj.tag)
        f = open("receptor.dat", 'w')
        f.write(json.dumps(self.datos))
        f.close()
        Clock.schedule_once(self.dibujar_lineas, .5)

    def fallo(self, e, res):
        pass #self.spin.hide()

    def get_lista(self):
        if self.datos is not None:
            #self.spin.show()
            self.url = self.datos["url"]
            UrlRequest(on_success=self.recargar_lista,
                       on_error=self.fallo, on_failure=self.fallo,
                       url=os.path.join('http://'+self.url, 'receptores', 'get_lista'))

    def recargar_lista(self, r, res):
        #self.spin.hide()
        self.lista_receptores = res
        Clock.schedule_once(self.dibujar_lineas, .5)

    def dibujar_lineas(self, dt):
        lista_activos = self.datos["lista"]
        self.content.rm_all_widgets()
        for l in self.lista_receptores:
            if l["Nombre"] != "Ticket":
                lw = LineasReceptorWidget()
                lw.controller = self
                lw.texto = l["Nombre"]
                lw.tag = l["receptor"]
                lw.icon = res.FA_EYE if lw.tag in lista_activos else res.FA_EYE_SLASH
                self.content.add_widget(lw)

class NetworkPage(Page):
    datos = ObjectProperty(None)
    label_info = ObjectProperty(None)

    def on_datos(self, w, v):
        if self.datos is not None:
            self.try_connect()

    def on_label_info(self, w, v):
        if (self.datos == None):
            self.label_info.text = "No hay datos de comunicación...."
        else:
            self.try_connect()

    def fallo(self, r, e):
        self.label_info.text="Tiene datos de conexion pero no alcanzamos el servidor..."


    def know_connect(self, r, res):
        if res == "success":
            self.label_info.text = "El receptor tiene conexion..."
        else:
            self.label_info.text = "Tiene datos de conexion pero no alcanzamos el servidor..."

    def guardar_url(self):
        if (self.datos == None):
            self.datos = { 'url': self.text_input_url.text, 'lista': []}
        else:
            self.datos['url'] = self.text_input_url.text
        f = open("receptor.dat", 'w')
        f.write(json.dumps(self.datos))
        f.close()
        self.try_connect()

    def try_connect(self):
        self.url = self.datos["url"]
        url=os.path.join('http://'+self.url, 'sync', 'know_connect')
        UrlRequest(on_success=self.know_connect, on_error=self.fallo, on_failure=self.fallo, url=url )


    def __init__(self, **kargs):
        super(NetworkPage, self).__init__(**kargs)

class ReceptorPage(Page):
    receptor_controller = ObjectProperty()
    mensage = StringProperty()
    contador = NumericProperty(0)
    app = ObjectProperty(None)


    def __init__(self, **args):
        super(ReceptorPage, self).__init__(**args)

    def exit_app(self):
        if self.app is not None:
            self.app.stop()

    def show_mensajes(self, texto):
        self.add_widget(Mensajes(art=texto))


    def on_show(self, w, v):
        self.receptor_controller.comprobar_cambios()

    def rm_pedido(self):
        if self.contador > 0:
            self.contador = self.contador - 1

        if len(self.content.children) <= 1:
            self.contador = 0


    def recargar(self):
        #self.spin.show()
        self.url = self.datos["url"]
        url=os.path.join('http://'+self.url, 'comandas', 'get_ultimas')
        params = {'r': self.datos['lista'], 'o': self.contador}
        params = urllib.parse.urlencode({"args":json.dumps(params)})
        headers = {'Content-type': 'application/x-www-form-urlencoded',
          'Accept': 'text/plain'}
        UrlRequest(on_success=self.get_ultimas, on_error=self.fallo, on_failure=self.fallo,
                   url=url, req_body=params,
                   req_headers=headers)

    def fallo(self, r, e):
        #self.spin.hide()
        self.label_float.show()
        self.label_float.text = "Error al comunicar con el servidor.. INTERNET"
        print(r,e)


    def get_ultimas(self, r, res):
        #self.spin.hide()
        self.contador = self.contador + 5
        for rs in res:
            for k in rs.keys():
                r = rs[k]
                self.pedido_forzados(r["camarero"], r["mesa"], r["hora"], r["lineas"])

    def pedido_forzados(self, camarero, mesa, hora, lineas):
        pedido = PedidoWidget()
        pedido.controller = self
        pedido.informacion = "{0} \n  {1}  {2}".format(camarero, mesa, hora)
        for linea in lineas:
            lw = LineasWidget()
            lw.texto = "{0: >3}  {1}".format(linea["can"], linea["nombre"])
            lw.controller = pedido
            pedido.lista.add_linea(lw)
        self.content.add_widget(pedido)


    def onMessage(self, v, st):
        self.contador=0
        if v != "":
            message = v["message"]
            op = message["op"]
            if op == "pedido":
                self.pedido(message["camarero"], message["mesa"], message["hora"], message["lineas"])
            elif op == "urgente":
                self.urgente(message["camarero"], message["mesa"], message["hora"], message["lineas"])


    def pedido(self, camarero, mesa, hora, lineas):
        pedido = PedidoWidget()
        pedido.controller = self
        pedido.informacion = "{0} \n  {1}  {2}".format(camarero, mesa, hora)
        for linea in lineas:
            lw = LineasWidget()
            lw.texto = "{0: >3}  {1}".format(linea["can"], linea["nombre"])
            lw.controller = pedido
            pedido.lista.add_linea(lw)
        self.content.add_widget(pedido)
        threading.Thread(target=self.beep).start()

    def urgente(self, camarero, mesa, hora, lineas):
        pedido = PedidoWidget()
        pedido.controller = self
        pedido.informacion = "URGENTE {0} \n  {1}  {2}".format(camarero, mesa, hora)
        for linea in lineas:
            lw = LineasWidget()
            lw.texto = "{0: >3}  {1}".format(linea["can"], linea["nombre"])
            lw.controller = pedido
            pedido.lista.add_linea(lw)
        self.content.add_widget(pedido)
        threading.Thread(target=self.beep).start()


    def beep(self):
        path_sound = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'beep-06-03.wav')
        if platform == "android":
            from kivy.core.audio import SoundLoader
            sound = SoundLoader.load(path_sound)
            if sound:
                sound.play()

        elif platform == "linux":
            os.system("aplay "+ path_sound)

        elif platform == "macosx":
            os.system("afplay "+ path_sound)

class MainPageWidget(AnchorLayout):
    app = ObjectProperty(None)
    datos = ObjectProperty(None)
    receptores = ListProperty()
    receptor = ObjectProperty()
    lista_receptores_activos = ListProperty()

    def __init__(self, **Kargs):
        super(MainPageWidget, self).__init__(**Kargs)
        if 'app' in Kargs:
            self.app = Kargs["app"]

        if os.path.isfile("receptor.dat"):
            f = open('receptor.dat', "r")
            self.datos = json.loads(f.read())
            self.url = self.datos["url"]
            self.lista_receptores_activos = self.datos["lista"]
            f.close()

    def on_lista_receptores_activos(self, w, v):
        self.load_receptores()

    def comprobar_cambios(self):
        if os.path.exists('receptor.dat'):
            f = open('receptor.dat', "r")
            self.datos = json.loads(f.read())
            f.close()
            lista1 =  self.datos["lista"]
            lista2 = self.lista_receptores_activos
            if len(lista1) == len(lista2):
                comparacion = []
                for item in lista2:
                    if item in lista1:
                        comparacion.append(item)

                if len(comparacion) != len(self.lista_receptores_activos):
                    self.label_float.show()

            else:
                self.label_float.show()

    def load_receptores(self):
        self.receptores = []
        for l in self.lista_receptores_activos:
            url=os.path.join('ws://'+self.url, 'ws', "impresion", l+"/")
            self.receptores.append(WSManager(url,self.receptor))


    def stop(self):
        for r in self.receptores:
            r.stop()

class ReceptorApp(App):
    def build(self):
        self.main_page = MainPageWidget(app=self)
        return self.main_page

    def on_pause(self):
        return True

    def on_stop(self):
        self.main_page.stop()


if __name__ == '__main__':
    ReceptorApp().run()
