# @Author: Manuel Rodriguez <valle>
# @Date:   01-Jan-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-05-05T00:12:11+02:00
# @License: Apache license vesion 2.0


"""service_web URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.10/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.conf.urls import url, include
    2. Add a URL to urlpatterns:  url(r'^blog/', include('blog.urls'))
"""
from django.urls import  include, path, re_path
from django.contrib.staticfiles.storage import staticfiles_storage
from django.views.generic.base import RedirectView, TemplateView



urlpatterns = [
    path('api/', include('api_android.urls'), name="api"),
    path('token/', include('tokenapi.urls')),
    path('favicon.ico',
            RedirectView.as_view( # the redirecting function
                url=staticfiles_storage.url('favicon.ico'), 
            ),
            name="favicon" # name of our view
        ),
    path("app/", include("app.urls"), name="app"),
    re_path(r'^.*$', TemplateView.as_view(template_name="index.html"))

]
