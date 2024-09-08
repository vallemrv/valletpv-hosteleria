from django.urls import path
from api_android.views.api_impresion import (abrircajon, imprimir_factura, imprimir_ticket, reenviarlinea,
                                             preimprimir, reenviarpedido)
from api_android.views.api_arqueos import imprimir_desglose


urlpatterns  =[
      path("preimprimir", preimprimir, name="impresion_preimprimir"),
      path("reenviarlinea", reenviarlinea, name="impresion_reenviarlinea"),
      path("reenviarpedido", reenviarpedido, name="reenviarpedido"),
      path("imprimir_ticket", imprimir_ticket, name="impresion_imprimir_ticket"),
      path("imprimir_factura", imprimir_factura, name="impresion_imprimir_factura"),
      path("imprimir_desglose", imprimir_desglose, name="impresion_imprimir_desglose"),
      path("abrircajon", abrircajon, name="impresion_abrircajon"),
]