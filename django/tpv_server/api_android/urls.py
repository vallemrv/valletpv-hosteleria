# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T19:42:58+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-10T15:51:11+02:00
# @License: Apache License v2.0

from django.urls import path
from api_android import views as api_views

TEST = [
   path("send_last_cierre", api_views.send_last_cierre, name="send_last_cierre"),
   path("reparar_subteclas", api_views.reparar_subteclas, name="sreparar_subteclas"),
   path("test_ws", api_views.test_websocket, name="test_ws"),
]


ARQUEOS = [
  path("arquear", api_views.arquear, name="arqueos_arquear"),
  path("getcambio", api_views.get_cambio, name="arqueos_get_cambio"),
]


ARITICULOS = [
  path("lstodos", api_views.ls_todos, name="articulos_ls_todos"),
]

COMANDAS = [
    path("lssubteclas", api_views.lssubteclas, name="comandas_lssubteclas"),
    path("lsAll", api_views.lsall, name="comandas_lsall"),
    path("pedir", api_views.pedir, name="comandas_pedir"),
    path("lspedidos", api_views.lspedidos, name="comandas_lspedidos"),
    path("marcar_rojo", api_views.marcar_rojo, name="comandas_marcar_rojo"),
    path("lssecciones", api_views.lssecciones, name="comandas_lssecciones"),
    path("get_ultimas", api_views.get_ultimas, name="get_ultimas"),

]

CAMAREROS = [
    path("listado", api_views.listado, name="camareros_listado"),
    path("es_autorizado", api_views.es_autorizado, name="camareros_es_autorizado"),
    path("crear_password", api_views.crear_password, name="camareros_crear_password"),
    path("listado_activos", api_views.listado_activos, name="camareros_listado_activos"),
    path("sel_camareros", api_views.sel_camareros, name="camareros_sel_camareros"),
    path("listado_autorizados", api_views.listado_autorizados, name="camareros_listado_autorizados"),
]

SYNC = [
    path("getupdate", api_views.getupdate, name="sync_getupdate"),
    path("get_eco", api_views.get_eco, name="get_eco"),
    path("send_test_mensaje/<str:msg>", api_views.send_test_mensaje, name="send_test_mensaje"),
    path("know_connect", api_views.know_connect, name="know_connect"),
    path("lastsync", api_views.lastsync, name="lastsync"),
    path("firstsync", api_views.firstsync, name="firstsync"),
    path("get_update_tables", api_views.get_update_tables, name="get_update_tables")
]

CUENTA = [
    path("ticket", api_views.ticket, name="cuenta_ticket"),
    path("get_cuenta", api_views.get_cuenta, name="get_cuenta"),
    path("juntarmesas", api_views.juntarmesas, name="cuenta_juntarmesas"),
    path("cambiarmesas", api_views.cambiarmesas, name="cuenta_cambiarmesas"),
    path("mvlinea", api_views.mvlinea, name="cuenta_mvlinea"),
    path("lsaparcadas", api_views.ls_aparcadas, name="cuenta_ls_aparcadas"),
    path("aparcar", api_views.cuenta_aparcar, name="cuenta_aparcar"),
    path("cobrar", api_views.cuenta_cobrar, name="cuenta_cobrar"),
    path("rm", api_views.cuenta_rm, name="cuenta_rm"),
    path("rmlinea", api_views.cuenta_rm_linea, name="cuenta_rm_linea"),
    path("lsticket", api_views.cuenta_ls_ticket, name="cuenta_ls_ticket"),
    path("lslineas", api_views.cuenta_ls_linea, name="cuenta_ls_linea"),
]

FAMILIAS = []
GASTOS = []
HISTORIAL = []

IMPRESION = [
      path("preimprimir", api_views.preimprimir, name="impresion_preimprimir"),
      path("reenviarlinea", api_views.reenviarlinea, name="impresion_reenviarlinea"),
      path("imprimir_ticket", api_views.imprimir_ticket, name="impresion_imprimir_ticket"),
      path("imprimir_desglose", api_views.imprimir_desglose, name="impresion_imprimir_desglose"),
      path("abrircajon", api_views.abrircajon, name="impresion_abrircajon"),
]

INFMESAS = []

MESAS = [
    path("lszonas", api_views.ls_zonas, name="mesas_ls_zonas"),
    path("lstodaslasmesas", api_views.lstodaslasmesas, name="mesas_lstodaslasmesas"),
    path("lsmesasabiertas", api_views.lsmesasabiertas, name="mesas_lsmesasabiertas"),
]

PEDIDOS = [
    path("getpendientes", api_views.get_pendientes, name="pedidos_get_pendientes"),
    path("servido", api_views.servido, name="pedidos_servido"),
    path("buscar", api_views.buscar, name="pedidos_buscar"),
]

RECEPTORES = [
  path("get_lista", api_views.get_lista, name="receptores_get_lista"),
  path("set_settings", api_views.set_settings, name="receptores_set_settings"),
]
SECCIONES = [

]

SUGERENCIAS = [
   path("ls", api_views.sugerencia_ls, name="sugericia_ls"),
   path("add", api_views.sugerencia_add, name="sugericia_add"),
]

TPV = []
VENTAS = [
  path("get_nulos", api_views.get_nulos, name="get_nulos"),
  path("get_infmesa", api_views.get_infmesa, name="get_infmesa"),
]
ZONAS = []




urlpatterns = [] + (ARQUEOS + ARITICULOS + COMANDAS + CAMAREROS + SYNC + CUENTA +
                   FAMILIAS + GASTOS + HISTORIAL + IMPRESION + INFMESAS + MESAS +
                   PEDIDOS + RECEPTORES + SECCIONES + SUGERENCIAS + TPV + VENTAS +
                   ZONAS + TEST)
