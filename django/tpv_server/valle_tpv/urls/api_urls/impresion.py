from django.urls import path
from valle_tpv import api as api_views

urlpatterns  =[
      path("preimprimir", api_views.preimprimir, name="impresion_preimprimir"),
      path("reenviarlinea", api_views.reenviarlinea, name="impresion_reenviarlinea"),
      path("reenviarpedido", api_views.reenviarpedido, name="reenviarpedido"),
      path("imprimir_ticket", api_views.imprimir_ticket, name="impresion_imprimir_ticket"),
      path("imprimir_factura", api_views.imprimir_factura, name="impresion_imprimir_factura"),
      path("imprimir_desglose", api_views.imprimir_desglose, name="impresion_imprimir_desglose"),
      path("abrircajon", api_views.abrircajon, name="impresion_abrircajon"),
]