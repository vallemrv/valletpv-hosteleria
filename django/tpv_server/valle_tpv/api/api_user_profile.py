from tokenapi.http import JsonResponse
from tokenapi.tokens import PasswordResetTokenGenerator
from tokenapi.decorators import token_required


@token_required
def get_profile(request):
    profile = request.user # Obtiene el perfil del usuario
    return JsonResponse({'username': profile.username, 'email': profile.email, 'name': profile.first_name, 'last_name': profile.last_name})

@token_required
def update_profile(request):
    if request.method == 'POST':
        user = request.user
        password = request.POST.get('password')
        email = request.POST.get('email')
        name = request.POST.get('name')
        last_name = request.POST.get('lastname')

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
