import PreciosView from '@/views/gestion/teclados/PreciosView'
import SeccionesView from '@/views/gestion/teclados/SeccionesView'
import TeclasView from '@/views/gestion/teclados/TeclasView'
import CamarerosView from '@/views/gestion/camareros/CamarerosView'
import CamarerosPase from '@/views/gestion/camareros/CamarerosPaseView'

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

const tecladosRoutes = [
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
    }
]


export const gestionRoutes = [
    ...camarerosRoutes, ...tecladosRoutes
]