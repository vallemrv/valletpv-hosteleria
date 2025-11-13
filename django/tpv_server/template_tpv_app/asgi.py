# @Author: Manuel Rodriguez <valle>
# @Date:   05-Jul-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 05-Jul-2018
# @License: Apache license vesion 2.0


"""
ASGI entrypoint. Configures Django and then runs the application
defined in the ASGI_APPLICATION setting.
"""

import os
import django
from channels.routing import get_default_application

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "server_{{name_tpv}}.settings")

django.setup()
application = get_default_application()
