def comunicar_cambios_devices(op, tb, obj, device = "" ):
    from api_android.tools import send_mensaje_devices
    update = {
        "op": op,
        "device": device,
        "tb": tb,
        "obj": obj,
        "receptor": "devices",
        }
    send_mensaje_devices(update)


def send_mensaje_impresora(men):
    from api_android.tools.ws_tools import send_mensaje_impresora
    send_mensaje_impresora(men)
       