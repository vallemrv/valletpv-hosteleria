from kivymd.app import MDApp
from kivymd.uix.screen import MDScreen
from kivy.properties import ObjectProperty, ListProperty, BooleanProperty, StringProperty 
from kivy.lang.builder import Builder
from kivy.uix.screenmanager import ScreenManager
from kivy.uix.anchorlayout import AnchorLayout


Builder.load_string('''
<LineaComanda>
    anchor_x: 'center'
    anchor_y: 'center'
    size_hint: 1, 1
    canvas.before:
        Color:
            rgba: root.color
        Rectangle:
            size: self.size
            pos: self.pos
    BoxLayout:
        orientation:'horizontal'
        size_hint: .9, .9
        MDLabel:
            text: root.descripcion
            valign: 'center'
            halign: 'left'
            
        MDIconButton:
            icon: 'check-bold'
            pos_hint: {"center_x": .5, "center_y": .5}
            on_release: root.servido()
    
<Comandas>
    anchor_x: 'center'
    anchor_y: 'center'
    size_hint: 1, 1
    BoxLayout:
        orientation: 'vertical'
        size_hint:.95, .95
        canvas.before:
            Color:
                rgba: 0.50,.5,.5,1
            Rectangle:
                size: self.size
                pos: self.pos
        MDToolbar:
            title: root.title
            right_action_items: [["trash-can-outline", lambda x: root.delete()]]

        ScrollView:
            size_hint: 1, .85
            MDGridLayout:
                cols:1
                size_hint_y: None
                height: len(self.children) * dp(70)
                spacing: dp(5)
                
<ConfigPage>
    name: "config_page"
    MDToolbar:
        elevation: 10
        pos_hint: {'top': 1}
        title: "Configuracion"
        left_action_items: [["chevron-left", lambda x: app.set_page("main_page")]] 
        

    AnchorLayout:
        size_hint: 1,1
        anchor_x: 'center'
        anchor_y: 'center'
        BoxLayout:
            size_hint: .95,.95
            orientation: 'vertical'
            MDTextField:
                hint_text: "Url servidor"
            MDLabel:
                text: "Esperando configuracion"
            

<MainPage>
    name: "main_page"
    content: _content
    AnchorLayout:
        size_hint: 1,1
        anchor_x: 'center'
        anchor_y: 'center'
        ScrollView:
            
            MDGridLayout:
                id: _content
                size_hint: None, 1
                rows:1
                width: len(self.children) * dp(270)
                


    MDFloatingActionButtonSpeedDial:
        data: root.buttons
        root_button_anim: True
        callback: root.on_press_action


''')

class LineaComanda(AnchorLayout):
    esta_servido = BooleanProperty(False)
    color = ObjectProperty((0.5, 1,.7,1))
    descripcion = StringProperty("Descripcion")
    def servido(self):
        if self.esta_servido:
            self.color = (0.5, 1, .7, 1)
            
        else:
            self.color = (0.7, .7, .7, 1)
            
            
        self.esta_servido = not self.esta_servido


class Comandas(AnchorLayout):
    title = StringProperty("Comanda")
    def delete(self):
        self.parent.remove_widget(self)


class ConfigPage(MDScreen):
    sm = ObjectProperty()

class MainPage(MDScreen):
    sm = ObjectProperty()
    buttons = ObjectProperty({
        'exit-to-app': 'Salir',
        'database-settings': 'Configurar',
        'reload': 'Ver Ultimas'
    })

    content = ObjectProperty()

    def on_press_action(self, w):
        if (w.icon == 'exit-to-app'):
            MDApp.get_running_app().stop()
        elif (w.icon == 'database-settings'):
            self.sm.current="config_page"
        


class AppMain(MDApp):

    def build(self):
        self.title = "Receptor ValleTpv"
        self.sm = ScreenManager()
        self.sm.add_widget(MainPage(sm=self.sm))
        self.sm.add_widget(ConfigPage(sm=self.sm))
        return self.sm


    def on_stop(self):
        return True

    def on_pause(self):
        return True

    def set_page(self, pg):
        self.sm.current = pg


if __name__ == "__main__":
    AppMain().run()
