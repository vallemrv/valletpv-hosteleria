import PreciosView from '@/views/gestion/teclados/PreciosView'
import SeccionesView from '@/views/gestion/teclados/SeccionesView'
import TeclasView from '@/views/gestion/teclados/TeclasView'
import CamarerosView from '@/views/gestion/camareros/CamarerosView'
import CamarerosPase from '@/views/gestion/camareros/CamarerosPaseView'
import VistaTeclas from '@/views/gestion/teclados/VistaTeclas'
import TecladosComanda from '@/views/gestion/teclados/TecladosComanda'
import TecladosTPV from '@/views/gestion/teclados/TecladosTPV'
import FamiliasView from '@/views/gestion/teclados/FamiliasView'
import TeclasEspeciales from '@/views/gestion/teclados/TeclasEspeciales'
import ReceptoresView from '@/views/gestion/teclados/ReceptoresView'
import ZonasView from '@/views/gestion/mesas/ZonasView'
import MesasView from '@/views/gestion/mesas/MesasView'

const camarerosRoutes = [
    {
        path: "/gestion/camareros/pase",
        component: CamarerosPase,
        name: 'camarerospase'
    },
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
        path: "/gestion/teclados/precio",
        component: PreciosView,
        name: 'cambioprecios'
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
        path: '/gestion/teclados/vista_teclas',
        component: VistaTeclas,
        name: 'vista'
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
    }
]


export const gestionRoutes = [
    ...camarerosRoutes, ...tecladosRoutes, ...zonasRoutes,
]