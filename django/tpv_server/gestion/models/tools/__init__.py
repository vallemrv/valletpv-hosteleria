from gestion.models import Mesasabiertas, Lineaspedido, Historialnulos
from comunicacion.tools import comunicar_cambios_devices
from datetime import datetime


def get_lineas_by_mesasabiertas():
    mesas = Mesasabiertas.objects.all()
    lineas = []
    for m in mesas:
        lineas = [*lineas, *m.get_lineaspedido()]
            
    return lineas


def borrar_mesa_abierta(idc, idm, motivo):
    mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
    if mesa:
        uid = mesa.infmesa.pk
        reg = Lineaspedido.objects.filter((Q(estado="P") | Q(estado="M") | Q(estado="R")) & Q(infmesa__pk=uid))
        for r in reg:
            historial = Historialnulos()
            historial.lineapedido_id = r.pk
            historial.camarero_id = idc
            historial.motivo = motivo
            historial.hora = datetime.now().strftime("%H:%M")
            historial.save()
            r.estado = 'A'
            r.save()
            comunicar_cambios_devices("rm", "lineaspedido", {"ID":r.id}, {"op": "borrado", "precio": float(r.precio)})


        obj = mesa.serialize()
        obj["abierta"] = 0
        obj["num"] = 0
        comunicar_cambios_devices("md", "mesasabiertas", obj)
        mesa.delete()
        
