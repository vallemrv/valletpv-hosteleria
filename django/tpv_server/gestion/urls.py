# @Author: Manuel Rodriguez <valle>
# @Date:   01-Jan-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-18T20:25:49+02:00
# @License: Apache license vesion 2.0


from django.urls import path
from . import views


app_name = "gestion"

urlpatterns = [
    path("", views.inicio, name="inicio")
]
