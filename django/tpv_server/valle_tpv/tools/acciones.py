# Version: 0.1
# Date: 2019-11-25
# importaciones
import os
from django.conf import settings
from django.forms import model_to_dict
from valle_tpv.tools.ws import comunicar_cambios_devices


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
    
    
    comunicar_cambios_devices("create", tb_name, [obj])    

    return obj

def modifcar_handler(model, tb_name, reg, filter):
    obj = model.objects.filter(**filter).first()
    if (obj):
        for key in reg:
            k_lower = key.lower()
            attr = reg[key]
            if hasattr(obj, k_lower):
                field = getattr(model, k_lower)
                if "FileField" in field.__class__.__name__:
                    file = getattr(obj, k_lower)
                    if file:
                        if os.path.isfile(os.path.join(settings.MEDIA_ROOT, file.name)):
                            os.remove(os.path.join(settings.MEDIA_ROOT, file.name))
                elif "ImageField" in field.__class__.__name__:
                    file = getattr(obj, k_lower)
                    if file:
                        if os.path.isfile(os.path.join(settings.MEDIA_ROOT, file.name)):
                            os.remove(os.path.join(settings.MEDIA_ROOT, file.name))

                elif "ForwardManyToOneDescriptor" in field.__class__.__name__:
                    k_lower =  k_lower+"_id"
                
            setattr(obj, k_lower, attr)        
            
        
        obj.save()
        obj = obj.serialize() if hasattr(obj, "serialize") else model_to_dict(obj)
            
        comunicar_cambios_devices("update", tb_name, [obj])    
        return obj 
    return None

def delete_handler(model, tb_name, filter):
    objs = model.objects.filter(**filter)
    result = []
    for obj in objs:
        for k, v in obj.__dict__.items():
            if "FileField" in v.__class__.__name__:
                file = getattr(obj, k)
                if file:
                    if os.path.isfile(os.path.join(settings.MEDIA_ROOT, file.name)):
                        os.remove(os.path.join(settings.MEDIA_ROOT, file.name))
            elif "ImageField" in v.__class__.__name__:
                file = getattr(obj, k)
                if file:
                    if os.path.isfile(os.path.join(settings.MEDIA_ROOT, file.name)):
                        os.remove(os.path.join(settings.MEDIA_ROOT, file.name))
        result.append(obj.pk)
        obj.delete()
        
    comunicar_cambios_devices("delete", tb_name, result)  
    return result
