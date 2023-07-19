from tokenapi.http import JsonResponse
from tokenapi.tokens import PasswordResetTokenGenerator
from tokenapi.decorators import token_required
from valle_tpv.tools.decorators import superuser_required, superuser_or_staff_required
from valle_tpv.models import HorarioUsr
from django.contrib.auth.models import User
import json

@token_required
@superuser_required
def create_user(request):
    if request.method == 'POST':
        user = request.user
        reg = json.loads(request.POST.get('reg'))
        username = reg.get('username')
        password = reg.get('password')
        email = reg.get('email')
        firt_name = reg.get('first_name')
        last_name = reg.get('last_name')
        is_superuser = reg.get('is_superuser')
        is_staff = reg.get('is_staff')

        user = User.objects.create_user(username, email, password)
        user.first_name = firt_name
        user.last_name = last_name
        user.is_superuser = is_superuser
        user.is_staff = is_staff
        user.save()

        obj = {}
            
        if 'horario' in reg:
            horario = reg.get('horario')
            horarios = HorarioUsr()
            horarios.hora_ini = horario['hora_ini'] if 'hora_ini' in horario else horarios.hora_ini
            horarios.hora_fin = horario['hora_fin'] if 'hora_fin' in horario else horarios.hora_fin
            horarios.usurario = user
            horarios.save()
            obj = {
                'hora_ini': horarios.hora_ini,
                'hora_fin': horarios.hora_fin,
            }

        return JsonResponse({'username': user.username, 'email': user.email, 'id': user.id,
                            'first_name': user.first_name, 'last_name': user.last_name,
                             'horario': obj})
          
       
    # Devuelve una respuesta de método no permitido si no es una solicitud POST
    return JsonResponse({'error': 'Método no permitido'})


@token_required
@superuser_or_staff_required
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
                         'first_name': profile.first_name, 'last_name': profile.last_name,
                         'horario': obj})

@token_required
@superuser_or_staff_required
def update_profile(request):
    if request.method == 'POST':
        is_me = False
        reg = json.loads(request.POST.get('reg'))
        if "id" in reg:
            user = User.objects.get(id=reg.get('id'))
        else:
            user = request.user
            is_me = True

        print(user)
        username = reg.get('username') if 'username' in reg else ''
        password = reg.get('password') if 'password' in reg else ''
        email = reg.get('email') if 'email' in reg else ''
        name = reg.get('first_name') if 'first_name' in reg else ''
        last_name = reg.get('last_name') if 'last_name' in reg else ''
        is_staff = reg.get('is_staff') if 'is_staff' in reg else ''
        is_active = reg.get('is_active') if 'is_active' in reg else ''
        data = {}
        if 'horario' in reg:
            horario = reg.get('horario')
            horarios  = HorarioUsr.objects.filter(usurario=user).first()
            if not horarios:
                horarios = HorarioUsr()

            horarios.hora_ini = horario['hora_ini'] if 'hora_ini' in horario else horarios.hora_ini
            horarios.hora_fin = horario['hora_fin'] if 'hora_fin' in horario else horarios.hora_fin
            horarios.usurario = user
            horarios.save()
            data['horario'] = {
                'hora_ini': horarios.hora_ini,
                'hora_fin': horarios.hora_fin,
            }
           
               
        # Actualiza la contraseña y el correo electrónico del usuario
        if (password != ''):
            user.set_password(password)
        user.username = username
        user.email = email
        user.first_name = name
        user.last_name = last_name
        user.is_active = is_active
        user.is_staff =  is_staff
        user.save()
        
        if is_me:
            if password != '':
                token_generator = PasswordResetTokenGenerator()
                token = token_generator.make_token(user)
                data['token'] = token
            else:
                data['token'] = request.POST.get('token')

            data['user'] = request.POST.get('user')   
            data['profile'] = {
                'username': user.username,
                'email': user.email,
                'first_name': user.first_name,
                'last_name': user.last_name,
            }

            if 'horario' in data:
                data['profile']['horario'] = data['horario']
                del data['horario'] 
                # Devuelve una respuesta exitosa
            return JsonResponse({'data': data})
        else:
            return JsonResponse({'reg':{'id':user.id, 'username': user.username, 'email': user.email, 
                            'first_name': user.first_name, 'last_name': user.last_name,
                             'horario': data['horario'] if 'horario' in data else {}}})
        

    # Devuelve una respuesta de método no permitido si no es una solicitud POST
    return JsonResponse({'error': 'Método no permitido'})

@token_required
@superuser_or_staff_required
def get_list(request):
    if request.method == 'POST':
        users = User.objects.filter(is_superuser=False)
        data = []
        for user in users:
            horarios  = HorarioUsr.objects.filter(usurario=user).first()
            obj = {}
            if horarios:
                obj = {
                    'hora_ini': horarios.hora_ini,
                    'hora_fin': horarios.hora_fin,
                }
            data.append({
                'id': user.id,
                'username': user.username,
                'email': user.email,
                'first_name': user.first_name,
                'last_name': user.last_name,
                'horario': obj,
                'is_active': user.is_active,
            })
        return JsonResponse({'regs': data})