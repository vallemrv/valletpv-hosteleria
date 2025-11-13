/**
 * Temas Vuetify para valle-tpv-lib
 * Proporciona dos temas elegantes y agradables: `valleLight` y `valleDark`.
 * Importar y usar con createVuetify({ theme: { defaultTheme, themes } })
 */

export const valleLight = {
  dark: false,
  colors: {
    background: '#F6F8FA', // fondo muy suave
    surface: '#FFFFFF',
    primary: '#1565C0', // azul elegante
    'primary-darken-1': '#0D47A1',
    secondary: '#6A1B9A', // morado profundo
    accent: '#FFB300', // ámbar cálido
    info: '#1976D2',
    success: '#2E7D32',
    warning: '#F9A825',
    error: '#C62828',
    'on-primary': '#FFFFFF',
    'on-secondary': '#FFFFFF',
    'on-surface': '#1F2933'
  }
};

export const valleDark = {
  dark: false,
  colors: {
    background: '#0F1720', // oscuro suave
    surface: '#141921',
    primary: '#90CAF9', // azul claro
    'primary-darken-1': '#64B5F6',
    secondary: '#CE93D8', // lila suave
    accent: '#FFD54F',
    info: '#90CAF9',
    success: '#81C784',
    warning: '#FFF176',
    error: '#EF9A9A',
    'on-primary': '#0F1720',
    'on-secondary': '#0F1720',
    'on-surface': '#E6EEF8'
  }
};

export const pastelElegance = {
  dark: false,
  colors: {
    background: '#FBFBFF',   // Un blanco muy sutil con un toque de azul
    surface: '#FFFFFF',      // Blanco puro para tarjetas y elementos elevados
    primary: '#A0C4FF',      // Azul pastel suave
    'primary-darken-1': '#89AEEF',
    secondary: '#FFD6A5',    // Melocotón pastel cálido
    accent: '#CAFFBF',       // Verde menta fresco
    info: '#BDB2FF',         // Lavanda suave
    success: '#9BF6AE',      // Verde pastel brillante
    warning: '#FFD6A5',      // (usando el secundario)
    error: '#FFADAD',        // Rojo pastel / salmón
    'on-primary': '#1A237E',  // Azul muy oscuro para contraste sobre el primario
    'on-secondary': '#5D4037',// Marrón oscuro para contraste sobre el secundario
    'on-surface': '#333742'  // Un gris oscuro y suave para el texto principal
  }
};


export const miTemaModerno = {
  dark: false,
  colors: {
    primary: '#8A81D8',
    secondary: '#5D9CEC',
    background: '#F7F8FA',
    surface: '#FFFFFF',
    success: '#29a245db',
    error: '#e57373',  
    warning: '#FFC107',
    info: '#17A2B8',
    'on-background': '#343A40',
    'on-surface': '#343A40',
    'on-primary': '#FFFFFF'
  }
};

export default {
  valleLight,
  valleDark,
  pastelElegance,
  miTemaModerno
};
