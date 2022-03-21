# @Author: Manuel Rodriguez <valle>
# @Date:   01-Jan-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 02-Jan-2018
# @License: Apache license vesion 2.0

try:
    from django.conf.urls import url
except :
    from django.urls import re_path as url
    
from . import views

urlpatterns = [
  url(r"^$", views.index, name="index")
]
