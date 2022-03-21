from kivy.app import App
from kivy.uix.label import Label


class AppM(App):
    def build(self):
        return Label(text="helleo makello")



AppM().run()