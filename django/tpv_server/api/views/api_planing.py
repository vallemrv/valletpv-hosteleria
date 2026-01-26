import os
import json
from django.conf import settings
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt

def get_server_path():
    return os.path.join(settings.MEDIA_ROOT, 'planing')

def get_server_url():
    server_name = getattr(settings, 'EMPRESA', 'default')
    # Assuming MEDIA_ROOT is inside STATIC_ROOT as 'media_serverName'
    return f"{settings.STATIC_URL}media_{server_name}/planing"

@csrf_exempt
def get_iconos(request):
    try:
        base_path = get_server_path()
        base_url = get_server_url()
        
        clicables_path = os.path.join(base_path, 'elementos_clicables')
        no_clicables_path = os.path.join(base_path, 'no_clicable')
        
        iconos = []
        
        # Helper to scan directory
        def scan_dir(path, tipo, url_prefix):
            if os.path.exists(path):
                for f in os.listdir(path):
                    if f.lower().endswith(('.png', '.jpg', '.jpeg', '.svg', '.gif')):
                        iconos.append({
                            'tipo': tipo,
                            'nombre': f,
                            'url': f"{url_prefix}/{f}"
                        })

        scan_dir(clicables_path, 'clicable', f"{base_url}/elementos_clicables")
        scan_dir(no_clicables_path, 'no_clicable', f"{base_url}/no_clicable")
        
        # Sort by name
        iconos.sort(key=lambda x: x['nombre'])
        
        return JsonResponse(iconos, safe=False)
    except Exception as e:
        return JsonResponse({'error': str(e)}, status=500)


@csrf_exempt
def guardar_planing(request):
    if request.method != 'POST':
        return JsonResponse({'error': 'Method not allowed'}, status=405)
    
    try:
        data = json.loads(request.body)
        zona_id = data.get('id')
        if not zona_id:
             return JsonResponse({'error': 'Missing id'}, status=400)
             
        base_path = get_server_path()
        if not os.path.exists(base_path):
            os.makedirs(base_path)
            
        file_path = os.path.join(base_path, f'planing_{zona_id}.json')
        
        with open(file_path, 'w') as f:
            json.dump(data, f)
            
        return JsonResponse({'success': True})
        
    except Exception as e:
        return JsonResponse({'error': str(e)}, status=500)

@csrf_exempt
def descargar_planing(request):
    # Expect 'id' in POST or GET
    zona_id = request.POST.get('id') or request.GET.get('id')
    
    # If using POST with JSON body
    if not zona_id and request.body:
        try:
            data = json.loads(request.body)
            zona_id = data.get('id')
        except:
            pass

    if not zona_id:
        return JsonResponse({'error': 'Missing id parameter'}, status=400)
        
    try:
        base_path = get_server_path()
        file_path = os.path.join(base_path, f'planing_{zona_id}.json')
        
        if not os.path.exists(file_path):
             # Return empty structure if not exists, or 404
             # User prompt implies "descargar", implies fetch. If not exists, maybe return default/empty?
             # returning 404 is safer.
             return JsonResponse({'error': 'Planing not found'}, status=404)
             
        with open(file_path, 'r') as f:
            data = json.load(f)
            
        return JsonResponse(data)
        
    except Exception as e:
        return JsonResponse({'error': str(e)}, status=500)
