estructura_bd = sql_tables = {
    'articulos': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "Nombre" varchar(50) NOT NULL,
        "Precio" decimal NOT NULL,
        "IDFam" integer NOT NULL,
        "IDTecla" integer NOT NULL REFERENCES "teclas" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "Tipo" varchar(2) NOT NULL,
        "Para llevar" bool NOT NULL,
        "Visible" bool NOT NULL
    )
    ''',
    'cajas': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "Nombre" varchar(50) NOT NULL,
        "Descripcion" varchar(200) NULL,
        "FechaBaja" varchar(10) NULL
    )
    ''',
    'camareros': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "Nombre" varchar(50) NOT NULL,
        "Apellidos" varchar(4) NOT NULL,
        "Autorizado" bool NOT NULL,
        "Activo" bool NOT NULL
    )
    ''',
    'comandas': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "Nombre" varchar(50) NOT NULL,
        "Pin" varchar(4) NOT NULL,
        "EsAdmin" bool NOT NULL,
        "Borrado" bool NOT NULL
    )
    ''',
    'composicionteclas': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "IDComp" integer NOT NULL REFERENCES "teclas" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "IDTecla" integer NOT NULL REFERENCES "teclas" ("ID") DEFERRABLE INITIALLY DEFERRED
    )
    ''',
    'familias': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "Nombre" varchar(40) NOT NULL,
        "Tipo" varchar(150) NOT NULL,
        "NumTapas" integer NULL,
        "IDReceptor" integer NOT NULL REFERENCES "receptores" ("ID") DEFERRABLE INITIALLY DEFERRED
    )
    ''',
    'gastos': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "Descripcion" varchar(100) NOT NULL,
        "Importe" decimal NOT NULL,
        "IDArqueo" integer NOT NULL REFERENCES "arqueocaja" ("ID") DEFERRABLE INITIALLY DEFERRED
    )
    ''',
    'gestion_lineascompuestas': '''
    (
        "id" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "linea_compuesta" integer NOT NULL,
        "composicion_id" integer NOT NULL REFERENCES "composicionteclas" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "linea_principal_id" integer NOT NULL REFERENCES "lineaspedido" ("ID") DEFERRABLE INITIALLY DEFERRED
    )
    ''',
    'historialnulos': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "Hora" varchar(5) NOT NULL,
        "Motivo" varchar(200) NOT NULL,
        "IDCam" integer NOT NULL REFERENCES "camareros" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "IDLPedido" integer NOT NULL REFERENCES "lineaspedido" ("ID") DEFERRABLE INITIALLY DEFERRED
    )
    ''',
    'historialprecios': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "Fecha" varchar(10) NOT NULL,
        "Hora" varchar(5) NOT NULL,
        "IDArt" integer NOT NULL REFERENCES "articulos" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "PrecioAnt" decimal NOT NULL,
        "PrecioAct" decimal NOT NULL
    )
    ''',
    'lineaspedido': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "IDPedido" integer NOT NULL REFERENCES "pedidos" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "IDArt" integer NOT NULL REFERENCES "articulos" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "IDCam" integer NOT NULL REFERENCES "camareros" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "Unidades" integer NOT NULL,
        "Precio" decimal NOT NULL,
        "EsCopia" bool NOT NULL,
        "Hora" varchar(5) NOT NULL,
        "Para llevar" bool NOT NULL,
        "Pagado" bool NOT NULL
    )
    ''',
    'mesas': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "Nombre" varchar(50) NOT NULL,
        "IDZona" integer NOT NULL REFERENCES "zonas" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "Activa" bool NOT NULL
    )
    ''',
    'pagos': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "IDPedido" integer NOT NULL REFERENCES "pedidos" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "TipoPago" varchar(20) NOT NULL,
        "Importe" decimal NOT NULL,
        "IDCam" integer NOT NULL REFERENCES "camareros" ("ID") DEFERRABLE INITIALLY DEFERRED
    )
    ''',
    'pedidos': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "IDMesa" integer NOT NULL REFERENCES "mesas" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "IDCam" integer NOT NULL REFERENCES "camareros" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "Fecha" varchar(10) NOT NULL,
        "HoraIni" varchar(5) NOT NULL,
        "HoraFin" varchar(5) NULL,
        "Comensales" integer NULL,
        "EsCopia" bool NOT NULL,
        "Para llevar" bool NOT NULL
    )
    ''',
    'receptores': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "Nombre" varchar(50) NOT NULL,
        "Descripcion" varchar(200) NULL,
        "FechaBaja" varchar(10) NULL
    )
    ''',
    'teclas': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "Nombre" varchar(50) NOT NULL,
        "Descripcion" varchar(200) NULL,
        "IDTeclaSuperior" integer NULL REFERENCES "teclas" ("ID") DEFERRABLE INITIALLY DEFERRED
    )
    ''',
    'zonas': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "Nombre" varchar(50) NOT NULL,
        "Descripcion" varchar(200) NULL,
        "Activa" bool NOT NULL
    )
    ''',
    'arqueocaja': '''
    (
        "ID" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
        "Fecha" varchar(10) NOT NULL,
        "IDCamApertura" integer NOT NULL REFERENCES "camareros" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "IDCamCierre" integer NULL REFERENCES "camareros" ("ID") DEFERRABLE INITIALLY DEFERRED,
        "FondoInicial" decimal NOT NULL,
        "TotalVentas" decimal NOT NULL,
        "TotalGastos" decimal NOT NULL,
        "TotalArqueo" decimal NOT NULL,
        "Descuadre" decimal NOT NULL,
        "Comentarios" varchar(200) NULL
    )
    '''
}
