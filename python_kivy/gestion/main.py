from kivy.lang import Builder
from kivy.factory import Factory
from kivy.properties import StringProperty, ObjectProperty
from kivy.uix.screenmanager import ScreenManager, Screen
from kivy.network.urlrequest import UrlRequest
from kivy.uix.anchorlayout import AnchorLayout
from kivy.clock import Clock
from kivy.metrics import dp

from kivymd.app import MDApp
from kivymd.uix.boxlayout import MDBoxLayout
from kivymd.uix.card import MDCard
from kivymd.uix.boxlayout import MDBoxLayout
from kivymd.uix.behaviors import TouchBehavior
from kivymd.uix.taptargetview import MDTapTargetView
from kivymd.uix.list import ThreeLineListItem

import os
import urllib

from kivy.utils import platform

if platform != "android":
    from kivy.core.window import Window
    Window.size = (400, 700)

url_network = "http://tpvtr.valleapp.com"

KV = ('''

<LineaPedido>:
    MDBoxLayout:
        size_hint: .9, .9
        MDLabel:
            text: root.can
            haling: "right"
            size_hint_x: .1
            theme_text_color: "Custom"
            text_color: root.color
        MDLabel:
            text: root.nombre
            haling: "left"
            theme_text_color: "Custom"
            text_color: root.color
        MDLabel:
            text: root.precio + "€"
            size_hint_x: .2
            haling: "right"
            theme_text_color: "Custom"
            text_color: root.color

<Pedido>:
    orientation: "vertical"
    content: _content_
    adaptive_height: True
    TwoLineListItem:
        text: root.camarero
        secondary_text: root.hora
    GridLayout:
        cols: 1
        id: _content_
        size_hint_y: None
        heigth: dp(50) * len(self.children)


<InformacionMesa>:
    inf_button: _button_
    MDBoxLayout:
        orientation: "vertical"
        MDToolbar:
            title: root.title
            elevation: 5
            pos_hint: {'top': 1}
            left_action_items: [["chevron-left", lambda x: app.callback()]]
           

        ScrollView:

            MDGridLayout:
                id: _ticket_
                cols: 1
                adaptive_height: True

    MDFloatingActionButton:
        id: _button_
        icon: "information-outline"
        pos: 10, 10
        on_release: root.tap_target_start()        

<LineaNula>:
    text: "Mesa: "+ root.tag["mesa"] + "  Fecha: " + root.tag["fecha"]
    secondary_text: root.tag["camarero"]
    tertiary_text: 'Articulos: ' + str(root.tag["lineas"]) + " Total: " + str(root.tag["nulos"])+ " €"

               
           
                
<ListaLineasNulas>:
    BoxLayout:
        orientation: 'vertical'

        MDToolbar:
            id: toolbar
            title: "Historial Nulos"
            elevation: 5
            pos_hint: {'top': 1}


        ScrollView:    
            MDList:
                id: container


    MDSpinner:
        id: _spinner_
        size_hint: None, None
        size: dp(46), dp(46)
        pos_hint: {'center_x': .5, 'center_y': .5}
        active: True  

                

ScreenManager:
    id: _screen_manager_
    ListaLineasNulas:
        name:'listado_nulos'
        id: _listado_nulos_
    InformacionMesa:
        name:'infmesa'
        id:_infmesa_

  
''')

class LineaPedido(AnchorLayout):
    can = StringProperty()
    nombre = StringProperty()
    precio = StringProperty()
    color = ObjectProperty()


class Pedido(MDBoxLayout):
    tag = ObjectProperty(None)
    camarero = StringProperty("")
    hora = StringProperty("")
    content = ObjectProperty()

    def on_content(self, w, v):
        for l in self.tag["lineas"]:
            color = (0,0.5,1,1)
            if l["estado"] == "P":
               color = (0,0.5,0.5,1)
            elif l["estado"] == "A":
               color = (1,0,0,1)
            else:
               color = (0,0,1,1)

            self.content.add_widget(
                LineaPedido(can=str(l["can"]), precio=str(l["precio"]),
                nombre=str(l["nombre"]), color=color)
            )
        self.content.height = dp(40) * len(self.content.children)

    

    def on_tag(self, w, v):
        self.camarero = self.tag["camarero"]
        self.hora = self.tag["hora"]
        


class InformacionMesa(Screen):
    title = StringProperty("")
    inf_button = ObjectProperty(None)
    

    def on_inf_button(self, w, v):
        self.tap_target_view = MDTapTargetView(
            widget=self.inf_button,
            title_text="This is an add button",
            description_text="This is a description of the button",
            widget_position="left_bottom",
        )

    def __init__(self, *args, **kargs):
        super(InformacionMesa, self).__init__(*args, **kargs)
        

    def set_infmesa(self, inf):
        self.title = f'Mesa: {inf["nom_mesa"]}'
        self.tap_target_view.title_text = f'Abierta por: {inf["camarero"]}'
        estado = "Esta abierta aún" if inf["abierta"] else "Ya esta cerrada" 
        self.tap_target_view.description_text = F'Abierta el {inf["apertura"]}\n    {estado}\n    Cobrados:{inf["desglose"]["cobrados"]} €\n    Pendientes:{inf["desglose"]["pendientes"]} €\n    Anulados:{inf["desglose"]["nulos"]} €'
        self.ids._ticket_.clear_widgets()
        for l in inf["pedidos"]:
            self.ids._ticket_.add_widget(
                Pedido(tag=l)
            )


    def tap_target_start(self):
        if self.tap_target_view.state == "close":
            self.tap_target_view.start()
        else:
            self.tap_target_view.stop()

class LineaNula(ThreeLineListItem):
    tag = ObjectProperty(None)
    icon = StringProperty("chevron-right")
    controler = ObjectProperty(None)
    
    def on_release(self):
        self.controler.show_infmesa(self.tag)

    

class ListaLineasNulas(Screen):

    def on_error(self, v, e):
        print(v,e)

    def on_failure(self, v, e):
        print(v, e)

    def on_success(self, v, res):
        app = MDApp.get_running_app()
        app.root.current = "infmesa"
        app.root.ids._infmesa_.set_infmesa(res)
        self.ids._spinner_.active = False
       
    def add_linea(self, item):
        self.ids.container.add_widget(
            LineaNula(tag=item, controler=self))

        self.ids._spinner_.active=False

    def clear_lista(self):
        self.ids._spinner_.active=True
        self.ids.container.clear_widgets()



    def show_infmesa(self, tag):
        self.ids._spinner_.active=True
        url=os.path.join(url_network, 'api', 'get_infmesa')
        params = {'id': tag['id']}
        params = urllib.parse.urlencode(params)
        headers = {'Content-type': 'application/x-www-form-urlencoded', 'Accept': 'text/plain'}
        self.req = UrlRequest(on_success=self.on_success, on_error=self.on_error, on_failure=self.on_failure,
                   url=url, req_body=params,
                   req_headers=headers)




class Test(MDApp):
    
    def on_error(self, v, e):
        print(v,e)

    def on_failure(self, v, e):
        print(v, e)

    def on_success(self, v, res):
        self.root.transition.direction = 'left'
        for e in res:
            self.root.ids._listado_nulos_.add_linea(e)
       
    def build(self):
        self.title = "Gestion"
        return Builder.load_string(KV)

    def on_pause(self):
        return True

    def callback(self):
        self.root.transition.direction = 'right'
        self.root.current = 'listado_nulos'
        self.root.ids._listado_nulos_.clear_lista()
        Clock.schedule_once(lambda dt: self.get_nulos(), 0.5)
        

    def on_start(self):
       Clock.schedule_once(lambda dt: self.get_nulos(), 0.5)


    def get_nulos(self):
        self.req = UrlRequest(os.path.join(url_network,"api", "get_nulos"), on_success=self.on_success, 
                         on_failure=self.on_failure, on_error=self.on_error)

Test().run()

