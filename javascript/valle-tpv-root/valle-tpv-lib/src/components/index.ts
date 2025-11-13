import type { Component } from 'vue';
//impot dialogos
import UiDialogScaffold from './dialogs/UiDialogScaffold.vue';
import UiKeyboardScaffold from './dialogs/UiKeyboardScaffold.vue';
import UiCobroDialog from './dialogs/UiCobroDialog.vue';
import UiOpcionesDialog from './dialogs/UiOpcionesDialog.vue';
import UiTicketsDialog from './dialogs/UiTicketsDialog.vue';
import UiReceptoresDialog from './dialogs/UiReceptoresDialog.vue';
import UiVariosDialog from './dialogs/UiVariosDialog.vue';
import UiSepararTicketDialog from './dialogs/UiSepararTicketDialog.vue';
import UiBorrarDialog from './dialogs/UiBorrarDialog.vue';
import UiAutorizacionDialog from './dialogs/UiAutorizacionDialog.vue';
import UiCashKeeperDialog from './dialogs/UiCashKeeperDialog.vue';
import UiPinPadDialog from './dialogs/UiPinPadDialog.vue';
import UiArqueoConCashKeeperDialog from './dialogs/UiArqueoConCashKeeperDialog.vue';
import UiCambioCashKeeperDialog from './dialogs/UiCambioCashKeeperDialog.vue';
import UiSeleccionarSugerenciaDialog from './dialogs/UiSeleccionarSugerenciaDialog.vue';
import UiNumericKeyboard from './dialogs/UiNumericKeyboard.vue';
import UiLetterKeyboard from './dialogs/UiLetterKeyboard.vue';

//import botones
import UiBotonInfoEmpresa from './botones/UiBotonInfoEmpresa.vue';
import UiCamareroBtn from './botones/UiCamareroBtn.vue';
import UiMesaButton from './botones/UiMesaButton.vue';
import UiBotonTecla from './botones/UiBotonTecla.vue';
import UiBotonAsociable from './botones/UiBotonAsociable.vue';
import UiActionButton from './botones/UiActionButton.vue';
import UiMenuButton from './botones/UiMenuButton.vue';

//import ui
import UiMainWindows from './ui/UiMainWindows.vue';
import UiCuentaLista from './ui/UiCuentaLista.vue';
import UiTecladoNumerico from './ui/UiTecladoNumerico.vue';
import UiBotoneraProductos from './ui/UiBotoneraProductos.vue';
import UiTaskbarBusqueda from './ui/UiTaskbarBusqueda.vue';
import UiInfoCobroSnackBar from './ui/UiInfoCobroSnackBar.vue';
import UiSnackbar from './ui/UiSnackbar.vue';

// Para facilitar la instalación automática
export const componentList: Record<string, Component> = {
  UiMainWindows,
  UiActionButton,
  UiMenuButton,
  UiDialogScaffold,
  UiKeyboardScaffold,
  UiCobroDialog,
  UiOpcionesDialog,
  UiBotonInfoEmpresa,
  UiCamareroBtn,
  UiMesaButton,
  UiBotonTecla,
  UiBotonAsociable,
  UiCuentaLista,
  UiTecladoNumerico,
  UiNumericKeyboard,
  UiLetterKeyboard,
  UiBotoneraProductos,
  UiVariosDialog,
  UiSepararTicketDialog,
  UiBorrarDialog,
  UiTaskbarBusqueda,
  UiInfoCobroSnackBar,
  UiSnackbar,
  UiTicketsDialog,
  UiReceptoresDialog,
  UiAutorizacionDialog,
  UiCambioCashKeeperDialog,
  UiCashKeeperDialog,
  UiPinPadDialog,
  UiArqueoConCashKeeperDialog,
  UiSeleccionarSugerenciaDialog,
};

// Exportar componentes individualmente para importación con nombre
export {
  UiMainWindows,
  UiActionButton,
  UiMenuButton,
  UiDialogScaffold,
  UiKeyboardScaffold,
  UiCobroDialog,
  UiOpcionesDialog,
  UiBotonInfoEmpresa,
  UiCamareroBtn,
  UiMesaButton,
  UiBotonTecla,
  UiBotonAsociable,
  UiCuentaLista,
  UiTecladoNumerico,
  UiNumericKeyboard,
  UiLetterKeyboard,
  UiBotoneraProductos,
  UiVariosDialog,
  UiSepararTicketDialog,
  UiBorrarDialog,
  UiTaskbarBusqueda,
  UiInfoCobroSnackBar,
  UiSnackbar,
  UiTicketsDialog,
  UiReceptoresDialog,
  UiAutorizacionDialog,
  UiCambioCashKeeperDialog,
  UiCashKeeperDialog,
  UiPinPadDialog,
  UiArqueoConCashKeeperDialog,
  UiSeleccionarSugerenciaDialog,
};
