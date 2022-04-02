# @Author: Manuel Rodriguez <valle>
# @Date:   01-Jan-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-18T20:25:49+02:00
# @License: Apache license vesion 2.0


from django.urls import path
from . import views


app_name = "gestion"


CAMAREROS = [
    path("lista_camareros", views.lista_camareros, name="lista_camareros"),
    path("add_camarero", views.add_camarero, name="add_camarero"),
    path("rm_camarero/<int:id>", views.rm_camarero, name="rm_camarero"),
    path("autorizar_cam/<int:id>", views.autorizar_cam, name="autorizar_cam"),
    path("rm_pass_camarero/<int:id>", views.rm_pass_camarero, name="rm_pass_camarero"),
    path("edit_camarero/<int:id>", views.edit_camarero, name="edit_camarero"),
]

SECCIONES = [
    path("lista_secciones", views.lista_secciones, name="lista_secciones"),
    path("add_seccion", views.add_seccion, name="add_seccion"),
    path("edit_seccion/<int:id>", views.edit_seccion, name="edit_seccion"),
    path("rm_seccion/<int:id>", views.rm_seccion, name="rm_seccion"),
    path("rm_tecla_seccion/<int:id>/<int:idsecc>", views.rm_tecla_seccion, name="rm_tecla_seccion"),
    path("add_teclaseccion/<int:id>/<int:idsecc>", views.add_teclaseccion, name="add_teclaseccion"),
    path("lista_teclas_seccion", views.lista_teclas_seccion, name="lista_teclas_seccion"),
    path("lista_teclas_seccion/<int:id>", views.lista_teclas_seccion, name="lista_teclas_seccion"),
]

SECCIONES_COM = [
    path("listado_secciones_comanda", views.listado_secciones_comanda, name="modificar_secciones_comanda"),
    path("edit_seccion_com/<int:id>", views.edit_seccion_com, name="edit_seccion_com"),
    path("lista_teclas_seccion_com", views.lista_teclas_seccion_com, name="lista_teclas_seccion_com"),
    path("lista_teclas_seccion_com/<int:id>", views.lista_teclas_seccion_com, name="lista_teclas_seccion_com"),
    path("rm_tecla_com/<int:id>/<int:idsec>", views.rm_tecla_com, name="rm_tecla_com"),
    path("edit_teclascom_orden/<int:id>/<int:idsec>", views.edit_teclascom_orden, name="edit_teclascom_orden"),
]

TECLAS_COM = [
    path("lista_subteclas/<int:id>", views.lista_subteclas, name="lista_subteclas"),
    path("lista_subteclas_tecla/<int:id>", views.lista_subteclas_tecla, name="lista_subteclas_tecla"),
    path("rm_subtecla/<int:id>", views.rm_subtecla, name="rm_subtecla"),
    path("add_subtecla/<int:id>", views.add_subtecla, name="add_subtecla"),
    path("edit_subtecla/<int:id>", views.edit_subtecla, name="edit_subtecla"),
    path("lista_teclas_subtecla", views.lista_teclas_subtecla, name="lista_teclas_subtecla"),
    path("add_teclascom/<int:id>/<int:idsec>", views.add_teclascom, name="add_teclascom"),
    path("crear_tecla_add_teclascom/<int:id>", views.crear_tecla_add_teclascom, name="crear_tecla_add_teclascom"),
    path("lista_sugerencias/<int:id>", views.lista_sugerencias, name="lista_sugerencias"),
    path("lista_sugerencia_tecla/<int:id>", views.lista_sugerencia_tecla, name="lista_sugerencia_tecla"),
    path("rm_sugerencia/<int:id>", views.rm_sugerencia, name="rm_sugerencia"),
    path("add_sugerencia/<int:id>", views.add_sugerencia, name="add_sugerencia"),
    path("edit_sugerencia/<int:id>", views.edit_sugerencia, name="edit_sugerencia"),
    path("lista_teclas_sugerencia", views.lista_teclas_sugerencia, name="lista_teclas_sugerencia"),
    path("sugerencias", views.sugerencias, name="sugerencias"),
    path("subteclas", views.subteclas, name="subteclas"),
]


TECLAS = [
   path("lista_teclas", views.lista_teclas, name="articulos"),
   path("lista_teclas", views.lista_teclas, name="lista_teclas"),
   path("lista_teclas_add", views.lista_teclas_add, name="lista_teclas_add"),
   path("lista_teclas/<int:id>", views.lista_teclas, name="lista_teclas"),
   path("listado_familias", views.lista_familias, name="lista_familias"),
   path("rm_familia/<int:id>", views.rm_familia, name="rm_familia"),
   path("edit_familia/<int:id>", views.edit_familia, name="edit_familia"),
   path("add_articulo", views.add_articulo, name="add_articulo"),
   path("rm_tecla/<int:id>", views.rm_tecla, name="rm_tecla"),
   path("edit_articulo/<int:id>", views.edit_articulo, name="edit_articulo"),
   path("add_familia", views.add_familia, name="add_familia"),
   path("lista_subteclas_grupo/<int:id>", views.lista_subteclas_grupo, name="lista_subteclas_grupo"),
   path("add_subtecla_grupo/<int:id>/<int:IDTecla>", views.add_subtecla_grupo, name="add_subtecla_grupo"),
   path("rm_subtecla_grupo/<int:id>/<int:IDTecla>", views.rm_subtecla_grupo, name="rm_subtecla_grupo"),
   path("edit_teclas_orden/<int:id>/", views.edit_teclas_orden, name="edit_teclas_orden"),
 
]

MESAS = [
   path("lista_mesas", views.lista_mesas, name="mesas"),
   path("lista_mesas", views.lista_mesas, name="lista_mesas"),
   path("lista_mesas_by_zona", views.lista_mesas_by_zona, name="lista_mesas_by_zona"),
   path("lista_mesas/<int:id>", views.lista_mesas_by_zona, name="lista_mesas_by_zona"),
   path("listado_zonas", views.zonas, name="zonas"),
   path("rm_zona/<int:id>", views.rm_zona, name="rm_zona"),
   path("edit_zona/<int:id>", views.edit_zona, name="edit_zona"),
   path("add_mesa>", views.add_mesa, name="add_mesa"),
   path("add_mesa/<int:id>", views.add_mesa, name="add_mesa"),
   path("rm_mesa/<int:id>", views.rm_mesa, name="rm_mesa"),
   path("edit_mesa/<int:id>", views.edit_mesa, name="edit_mesa"),
   path("add_zona", views.add_zona, name="add_zona"),
   path("asociar_mesa/<int:id>/<int:idzona>", views.asociar_mesa, name="asociar_mesa"),
]

SYNC = [
    path("menu_sync", views.menu_sync, name="menu_sync"),
    path("sync_secciones", views.sync_secciones, name="sync_secciones"),
    path("sync_camareros", views.sync_camareros, name="sync_camareros"),
    path("sync_subteclas", views.sync_subteclas, name="sync_subteclas"),
    path("sync_teclascom", views.sync_teclascom, name="sync_teclascom"),
    path("sync_mesas", views.sync_mesas, name="sync_mesas"),
]


urlpatterns = [
    path("", views.inicio, name="inicio")
]+ TECLAS + CAMAREROS + SYNC + SECCIONES + SECCIONES_COM + TECLAS_COM + MESAS
