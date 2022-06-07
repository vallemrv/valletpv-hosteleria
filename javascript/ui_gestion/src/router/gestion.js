import TeclasView from '@/views/gestion/teclados/TeclasView/TeclasView'
import CamarerosView from '@/views/gestion/camareros/CamarerosView'

import SeccionesView from '@/views/gestion/teclados/SeccionesView'
import TecladosComanda from '@/views/gestion/teclados/TecladosComanda'
import TecladosTPV from '@/views/gestion/teclados/TecladosTPV'
import FamiliasView from '@/views/gestion/teclados/FamiliasView'
import TeclasEspeciales from '@/views/gestion/teclados/TeclasEspeciales'
import ReceptoresView from '@/views/gestion/teclados/ReceptoresView'
import ZonasView from '@/views/gestion/mesas/ZonasView'
import MesasView from '@/views/gestion/mesas/MesasView'
import TeclasByFamilias from '@/views/gestion/teclados/TeclasByFamilias'

const camarerosRoutes = [
    {
        path: "/gestion/camareros/edicion",
        component: CamarerosView,
        name: 'camareros'
    }
]
const zonasRoutes = [
    {
        path: "/gestion/zonas",
        component: ZonasView,
        name: 'zonas'
    },
    {
        path: "/gestion/mesas",
        component: MesasView,
        name: 'mesas'
    }
]

const tecladosRoutes = [
    {
        path: "/gestion/teclados/receptores",
        component: ReceptoresView,
        name: 'receptores'
    },
    {
        path: "/gestion/teclados/composicion",
        component: TeclasEspeciales,
        name: 'composicion'
    },
    {
        path: "/gestion/teclados/familias",
        component: FamiliasView,
        name: 'familias'
    },
    {
        path: "/gestion/teclados/secciones",
        component: SeccionesView,
        name: 'secciones'
    },
    {
        path: '/gestion/teclados/edicion',
        component: TeclasView,
        name: 'teclas'
    },
    {
        path: '/gestion/teclados/teclados_comanda',
        component: TecladosComanda,
        name: 'tecladoscom'
    },
    {
        path: '/gestion/teclados/teclados_tpv/:id?',
        component: TecladosTPV,
        name: 'tecladostpv'
    },
    {
        path: '/gestion/teclados/byfamilias/',
        component: TeclasByFamilias,
        name: 'byfamilias'
    }
]


export const gestionRoutes = [
    ...camarerosRoutes, ...tecladosRoutes, ...zonasRoutes,
]