from tokenapi.http import JsonResponse
from tokenapi.tokens import PasswordResetTokenGenerator
from tokenapi.decorators import token_required
from valle_tpv.models import HorarioUsr
from django.contrib.auth.models import User
import json

@token_required
def create_user(request):
    if request.method == 'POST':
        user = request.user
        if user.is_superuser:
            username = request.POST.get('username')
            password = request.POST.get('password')
            email = request.POST.get('email')
            name = request.POST.get('name')
            is_superuser = request.POST.get('is_superuser')
            is_staff = request.POST.get('is_staff')
            user = User.objects.create_user(username, email, password)
            user.first_name = name
            user.is_superuser = is_superuser
            user.is_staff = is_staff
            user.save()
            return JsonResponse({'username': username, 'name': name})
        else:
            return JsonResponse({'error': 'No tienes permisos para crear usuarios'}, status=403)

    # Devuelve una respuesta de método no permitido si no es una solicitud POST
    return JsonResponse({'error': 'Método no permitido'}, status=405)



@token_required
def get_profile(request):
    profile = request.user # Obtiene el perfil del usuario
    horarios  = HorarioUsr.objects.filter(usurario=profile).first()
    obj = {}
    if horarios:
        obj = {
            'hora_ini': horarios.hora_ini,
            'hora_fin': horarios.hora_fin,
        }

    return JsonResponse({'username': profile.username, 'email': profile.email, 
                         'name': profile.first_name, 'last_name': profile.last_name,
                         'horarios': obj})

@token_required
def update_profile(request):
    if request.method == 'POST':
        user = request.user
        password = request.POST.get('password')
        email = request.POST.get('email')
        name = request.POST.get('name')
        last_name = request.POST.get('lastname')
        if 'horario' in request.POST:
            horario = json.loads(request.POST.get('horario'))
            horarios  = HorarioUsr.objects.filter(usurario=user).first()
            if horarios:
                horarios.hora_ini = horario['hora_ini']
                horarios.hora_fin = horario['hora_fin']
                horarios.save()
            else:
                horarios = HorarioUsr()
                horarios.hora_ini = horario['hora_ini']
                horarios.hora_fin = horario['hora_fin']
                horarios.usurario = user
                horarios.save()


        # Actualiza la contraseña y el correo electrónico del usuario
        if (password != ''):
            user.set_password(password)
        user.email = email
        user.first_name = name
        user.last_name = last_name
        user.save()
        
        
        if password != '':
            token_generator = PasswordResetTokenGenerator()
            token = token_generator.make_token(user)
            token = {'user': request.POST.get('user'), 'token': token}
        else:
            token = {'user': request.POST.get('user'), 'token': request.POST.get('token')}

        
        # Devuelve una respuesta exitosa
        return JsonResponse({'token': token})

    # Devuelve una respuesta de método no permitido si no es una solicitud POST
    return JsonResponse({'error': 'Método no permitido'}, status=405)
