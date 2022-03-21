# -*- coding: utf-8 -*-

# @Author: Manuel Rodriguez <valle>
# @Date:   01-Jan-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 19-Jun-2018
# @License: Apache license vesion 2.0


from django.shortcuts import render

# Create your views here.

def index(request):
    return render(request, "no_found.html")
