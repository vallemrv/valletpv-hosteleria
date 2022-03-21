# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-20T12:02:20+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-26T08:55:39+01:00
# @License: Apache License v2.0


from django.forms.widgets import Widget
from django.template import loader
from django.utils.safestring import mark_safe


class ColorPicket(Widget):
    template_name = 'widget/color_picket.html'

    def get_context(self, name, value, attrs=None):
        return {'widget': {
            'name': name,
            'value': value,
        }}

    def render(self, name, value, attrs=None, renderer=None):
        context = self.get_context(name, value, attrs)
        template = loader.get_template(self.template_name).render(context)
        return mark_safe(template)
