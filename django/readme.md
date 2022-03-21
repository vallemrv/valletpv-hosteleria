[![django][django]](https://www.djangoproject.com/)
<br/>

##Aplicaciones con django

Esta carpeta contiene todas las herramientas creadas con Django. Com son 

* [Servidor de datos, gestion, control de las comunicaciones, contabilidad, control de stock, etc](tpv_server/)
* [Pagina web del producto](valletpv_web/)


## Instalación
### Servidor TPV

```sh
git clone git@github.com:vallemrv/valletpv-hosteleria.git
cd valletpv-hosteleria/django/tpv
python3 -m venv .env
source .env/bin/activate

pip install -r requeriments.txt
python create_tpv.py
python manage_<nombre_servidor_tpv>.py migrate
sh loaddata.sh manage_<nombre_servidor_tpv>.py db.json
python manage_<nombre_servidor_tpv>.py createsuperuser
python manage_<nombre_servidor_tpv>.py runserver

```






[django]: https://neuralcovenant.files.wordpress.com/2020/09/django.png?w=1568