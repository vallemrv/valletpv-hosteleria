from tokenapi.http import JsonError

def superuser_or_staff_required(view_func):
    def _wrapped_view(request, *args, **kwargs):
        if request.user.is_authenticated:
            if request.user.is_superuser or request.user.is_staff:
                return view_func(request, *args, **kwargs)
        return JsonError({"error":'Permisos insuficientes'})
    return _wrapped_view

def superuser_required(view_func):
    def _wrapped_view(request, *args, **kwargs):
        if request.user.is_authenticated:
            if request.user.is_superuser:
                return view_func(request, *args, **kwargs)
        return JsonError({"error":'Permisos insuficientes'})
    return _wrapped_view
