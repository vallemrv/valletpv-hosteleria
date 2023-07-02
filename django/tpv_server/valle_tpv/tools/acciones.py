# Version: 0.1
# Date: 2019-11-25
# importaciones

from django.forms import model_to_dict
from valle_tpv.tools.ws import send_mensaje_devices


def add_handler(model, tb_name, reg):
    obj = model()
    for key in reg:
        k_lower = key.lower()
        if hasattr(model, k_lower):
            field = getattr(model, k_lower)
            attr = reg[key]
            if "ForwardManyToOneDescriptor" in field.__class__.__name__:
                k_lower =  k_lower+"_id"
            else:
                attr = reg[key] 
            setattr(obj, k_lower, attr)  
        
    obj.save()

    if (hasattr(obj, "serialize")):
        obj = obj.serialize()
    else:
        obj = model_to_dict(obj)
    
    update = {
        "op": "insert",
        "device": "",
        "tb": tb_name,
        "obj": obj,
        "receptor": "devices",
    }
    send_mensaje_devices(update) 

    return obj

def modifcar_handler(model, tb_name, reg, filter):
    obj = model.objects.filter(**filter).first()
    if (obj):
        for key in reg:
            k_lower = key.lower()
            attr = reg[key]
            if hasattr(obj, k_lower):
                field = getattr(model, k_lower)
                if "ForwardManyToOneDescriptor" in field.__class__.__name__:
                    k_lower =  k_lower+"_id"
                
            setattr(obj, k_lower, attr)        
            
        
        obj.save()
        obj = obj.serialize() if hasattr(obj, "serialize") else model_to_dict(obj)
            
        update = {
            "op": "md",
            "device": "",
            "tb": tb_name,
            "obj":obj,
            "receptor": "devices",
            }
        
        send_mensaje_devices(update)
        return obj 
    return None

def delete_handler(model, tb_name, filter):
    objs = model.objects.filter(**filter)
    result = []
    for obj in objs:
        result.append(obj.pk)
        obj.delete()
    update = {
        "op": "rm",
        "device": "",
        "tb": tb_name,
        "obj": result,
        "receptor": "devices",
    }
    send_mensaje_devices(update) 
    return result
