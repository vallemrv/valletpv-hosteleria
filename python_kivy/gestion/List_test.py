from kivy.lang import Builder

from kivymd.app import MDApp

KV = '''
ScrollView:

    MDList:

        OneLineAvatarIconListItem:
            on_release: print("Click!")

            IconLeftWidget:
                icon: "github"

        OneLineAvatarIconListItem:
            on_release: print("Click 2!")

            IconLeftWidget:
                icon: "gitlab"

        OneLineAvatarIconListItem:
            text: "One-line item with avatar"

            IconLeftWidget:
                icon: "plus"

            IconRightWidget:
                icon: "minus"
'''


class MainApp(MDApp):
    def build(self):
        return Builder.load_string(KV)


MainApp().run()