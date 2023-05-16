estructura_base='''
Table camareros:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Nombre', 'varchar(100)', 1, None, 0)
(2, 'Apellidos', 'varchar(100)', 1, None, 0)
(3, 'Email', 'varchar(50)', 0, None, 0)
(4, 'Pass', 'varchar(100)', 0, None, 0)
(5, 'Activo', 'INTEGER', 1, None, 0)
(6, 'Autorizado', 'INTEGER', 1, None, 0)
(7, 'Permisos', 'varchar(150)', 0, None, 0)
Table gestion_iconchoices:
(0, 'id', 'INTEGER', 1, None, 1)
Table mesas:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Nombre', 'varchar(50)', 1, None, 0)
(2, 'Orden', 'INTEGER', 1, None, 0)
Table gestion_permisoschoices:
(0, 'id', 'INTEGER', 1, None, 1)
Table receptores:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Nombre', 'varchar(40)', 1, None, 0)
(2, 'nomImp', 'varchar(40)', 1, None, 0)
(3, 'Activo', 'bool', 1, None, 0)
(4, 'Descripcion', 'varchar(200)', 1, None, 0)
Table secciones:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Nombre', 'varchar(50)', 1, None, 0)
(2, 'RGB', 'varchar(11)', 1, None, 0)
(3, 'Orden', 'INTEGER', 1, None, 0)
Table gestion_sync:
(0, 'id', 'INTEGER', 1, None, 1)
(1, 'nombre', 'varchar(50)', 1, None, 0)
(2, 'last', 'varchar(26)', 1, None, 0)
Table teclas:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Nombre', 'varchar(50)', 1, None, 0)
(2, 'P1', 'decimal', 1, None, 0)
(3, 'P2', 'decimal', 1, None, 0)
(4, 'Orden', 'INTEGER', 1, None, 0)
(5, 'Tag', 'varchar(100)', 1, None, 0)
(6, 'TTF', 'varchar(50)', 1, None, 0)
(7, 'Descripcion_r', 'varchar(300)', 0, None, 0)
(8, 'Descripcion_t', 'varchar(300)', 0, None, 0)
(9, 'tipo', 'varchar(2)', 1, None, 0)
(10, 'IDFamilia', 'INTEGER', 1, None, 0)
Table zonas:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Nombre', 'varchar(50)', 1, None, 0)
(2, 'Tarifa', 'INTEGER', 1, None, 0)
(3, 'RGB', 'varchar(50)', 1, None, 0)
Table ticketlineas:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'IDLinea', 'INTEGER', 1, None, 0)
(2, 'IDTicket', 'INTEGER', 1, None, 0)
Table teclaseccion:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'IDSeccion', 'INTEGER', 1, None, 0)
(2, 'IDTecla', 'INTEGER', 1, None, 0)
Table teclascom:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Orden', 'INTEGER', 1, None, 0)
(2, 'IDSeccion', 'INTEGER', 1, None, 0)
(3, 'IDTecla', 'INTEGER', 0, None, 0)
Table sugerencias:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Sugerencia', 'varchar(300)', 1, None, 0)
(2, 'IDTecla', 'INTEGER', 1, None, 0)
Table subteclas:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'nombre', 'varchar(100)', 0, None, 0)
(2, 'incremento', 'decimal', 0, None, 0)
(3, 'Descripcion_r', 'varchar(300)', 0, None, 0)
(4, 'Descripcion_t', 'varchar(300)', 0, None, 0)
(5, 'Orden', 'INTEGER', 1, None, 0)
(6, 'IDTecla', 'INTEGER', 1, None, 0)
Table servidos:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'IDLinea', 'INTEGER', 1, None, 0)
Table gestion_peticionesautoria:
(0, 'id', 'INTEGER', 1, None, 1)
(1, 'accion', 'varchar(150)', 1, None, 0)
(2, 'instrucciones', 'varchar(300)', 1, None, 0)
(3, 'idautorizado_id', 'INTEGER', 1, None, 0)
Table pedidos:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Hora', 'varchar(5)', 1, None, 0)
(2, 'IDCam', 'INTEGER', 1, None, 0)
(3, 'uid_device', 'varchar(150)', 1, None, 0)
(4, 'UID', 'varchar(100)', 1, None, 0)
Table mesaszona:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'IDMesa', 'INTEGER', 1, None, 0)
(2, 'IDZona', 'INTEGER', 1, None, 0)
Table mesasabiertas:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'UID', 'varchar(100)', 1, None, 0)
(2, 'IDMesa', 'INTEGER', 1, None, 0)
Table lineaspedido:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'IDArt', 'INTEGER', 1, None, 0)
(2, 'Estado', 'varchar(1)', 1, None, 0)
(3, 'Precio', 'decimal', 1, None, 0)
(4, 'Descripcion', 'varchar(400)', 0, None, 0)
(5, 'es_compuesta', 'bool', 1, None, 0)
(6, 'cantidad', 'INTEGER', 1, None, 0)
(7, 'Descripcion_t', 'varchar(300)', 0, None, 0)
(8, 'UID', 'varchar(100)', 1, None, 0)
(9, 'IDPedido', 'INTEGER', 1, None, 0)
(10, 'tecla_id', 'INTEGER', 0, None, 0)
Table gestion_lineascompuestas:
(0, 'id', 'INTEGER', 1, None, 1)
(1, 'linea_compuesta', 'INTEGER', 1, None, 0)
(2, 'composicion_id', 'INTEGER', 1, None, 0)
(3, 'linea_principal_id', 'INTEGER', 1, None, 0)
Table historialnulos:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Hora', 'varchar(5)', 1, None, 0)
(2, 'Motivo', 'varchar(200)', 1, None, 0)
(3, 'IDCam', 'INTEGER', 1, None, 0)
(4, 'IDLPedido', 'INTEGER', 1, None, 0)
Table gastos:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Descripcion', 'varchar(100)', 1, None, 0)
(2, 'Importe', 'decimal', 1, None, 0)
(3, 'IDArqueo', 'INTEGER', 1, None, 0)
Table familias:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Nombre', 'varchar(40)', 1, None, 0)
(2, 'Tipo', 'varchar(150)', 1, None, 0)
(3, 'NumTapas', 'INTEGER', 0, None, 0)
(4, 'IDReceptor', 'INTEGER', 1, None, 0)
Table efectivo:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Can', 'INTEGER', 1, None, 0)
(2, 'Moneda', 'decimal', 1, None, 0)
(3, 'IDArqueo', 'INTEGER', 1, None, 0)
Table composicionteclas:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'composicion', 'varchar(300)', 1, None, 0)
(2, 'cantidad', 'INTEGER', 1, None, 0)
(3, 'IDTecla', 'INTEGER', 1, None, 0)
Table arqueocaja:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'Cambio', 'REAL', 1, None, 0)
(2, 'Descuadre', 'REAL', 1, None, 0)
(3, 'IDCierre', 'INTEGER', 1, None, 0)
Table cierrecaja:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'TicketCom', 'INTEGER', 1, None, 0)
(2, 'TicketFinal', 'INTEGER', 1, None, 0)
(3, 'Hora', 'varchar(5)', 1, None, 0)
(4, 'Fecha', 'date', 1, None, 0)
Table gestion_historialmensajes:
(0, 'id', 'INTEGER', 1, None, 1)
(1, 'mensaje', 'varchar(300)', 1, None, 0)
(2, 'hora', 'varchar(10)', 1, None, 0)
(3, 'camarero_id', 'INTEGER', 1, None, 0)
(4, 'receptor_id', 'INTEGER', 1, None, 0)
(5, 'fecha', 'date', 1, None, 0)
Table infmesa:
(0, 'UID', 'varchar(100)', 1, None, 1)
(1, 'Hora', 'varchar(5)', 1, None, 0)
(2, 'NumCopias', 'INTEGER', 1, None, 0)
(3, 'IDCam', 'INTEGER', 1, None, 0)
(4, 'Fecha', 'date', 1, None, 0)
Table ticket:
(0, 'ID', 'INTEGER', 1, None, 1)
(1, 'IDCam', 'INTEGER', 1, None, 0)
(2, 'Hora', 'varchar(5)', 1, None, 0)
(3, 'Entrega', 'decimal', 1, None, 0)
(4, 'UID', 'varchar(100)', 1, None, 0)
(5, 'Mesa', 'varchar(40)', 1, None, 0)
(6, 'url_factura', 'varchar(140)', 1, None, 0)
(7, 'Fecha', 'date', 1, None, 0)


'''