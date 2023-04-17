# @Author: Manuel Rodriguez <valle>
# @Date:   01-Jan-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-05-05T00:10:07+02:00
# @License: Apache license vesion 2.0

"""
Django settings for service_web project.

Generated by 'django-admin startproject' using Django 1.10.7.

For more information on this file, see
https://docs.djangoproject.com/en/1.10/topics/settings/

For the full list of settings and their values, see
https://docs.djangoproject.com/en/1.10/ref/settings/
"""

import os

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
IVA = 10
BRAND_TITLE = "{{nombre_empresa}}"
BRAND = "{{nombre_empresa}}"
EMPRESA = "{{name_tpv}}"
MAIL = "{{email}}"
RAZON_SOCIAL = "{{razon_social}}."
NIF = "{{nif}}"
DIRECCION = "{{direccion}}"
TELEFONO = "{{telefono}}"
POBLACION= "{{poblacion}}"
PROVINCIA= "{{provincia}}"
CP="{{cp}}"



# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/1.10/howto/deployment/checklist/

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = '{{secret_key}}'

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = {{is_debug}}

ALLOWED_HOSTS = ["*"]

# Application definition
INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'channels',
    'comunicacion',
    'contabilidad',
    'gestion',
    'app',
    'api_android',
    'corsheaders',
    'valleIA'
]

MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
    'corsheaders.middleware.CorsMiddleware',
]

AUTHENTICATION_BACKENDS = [
     'django.contrib.auth.backends.ModelBackend',
     'tokenapi.backends.TokenBackend',
]

DEFAULT_AUTO_FIELD = 'django.db.models.AutoField'

CORS_ORIGIN_ALLOW_ALL = True

ROOT_URLCONF = 'server_{{name_tpv}}.urls'

STATICFILES_DIRS = (
        os.path.join(BASE_DIR, 'static', 'resources'),
    )

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = 'server_{{name_tpv}}.wsgi.application'

#channels
ASGI_APPLICATION = "server_{{name_tpv}}.routing.application"

CHANNEL_LAYERS = {
    'default': {
        'BACKEND': 'channels_redis.core.RedisChannelLayer',
        'CONFIG': {
            "hosts": [('127.0.0.1', 6379)],
        },
    },
}


# Database
# https://docs.djangoproject.com/en/1.10/ref/settings/#databases
{% if sql_mode == "mysql" %}
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.mysql',
        'NAME': '{{name_db}}',
        'USER': '{{name_user_db}}',
        'PASSWORD': '{{password_db}}',
        'OPTIONS': {
            'init_command': 'SET default_storage_engine=INNODB;' +
                            "SET sql_mode=(SELECT REPLACE(@@sql_mode, 'ONLY_FULL_GROUP_BY', ''))",
        },
    }
}
{% else %}
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3', # Add 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
        'NAME': '{{name_db}}.sqlite3',          # Or path to database file if using sqlite3.
    }
}
{% endif %}

# Password validation
# https://docs.djangoproject.com/en/1.10/ref/settings/#auth-password-validators

AUTH_PASSWORD_VALIDATORS = [
    {
        'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator',
    },
]

#smtp config
EMAIL_BACKEND = 'django.core.mail.backends.smtp.EmailBackend'
EMAIL_HOST_PASSWORD = '{{password_mail}}'
EMAIL_HOST_USER = '{{email_smtp}}'
EMAIL_PORT = '{{port_smtp}}'
EMAIL_HOST = '{{host_smtp}}'
EMAIL_USE_SSL = True

# session expire at browser close
SESSION_EXPIRE_AT_BROWSER_CLOSE = True
TOKEN_TIMEOUT_DAYS = 360


# Internationalization
# https://docs.djangoproject.com/en/1.10/topics/i18n/

LANGUAGE_CODE = 'es-es'

TIME_ZONE = 'Europe/Madrid'

USE_I18N = True

USE_L10N = True

USE_TZ = True


# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/1.10/howto/static-files/

MEDIA_URL   = '/media/'
STATIC_URL  = '/static/'
STATIC_ROOT =  os.path.join(BASE_DIR, 'static')
MEDIA_ROOT  =  os.path.join(STATIC_ROOT, 'media_{{name_tpv}}')
