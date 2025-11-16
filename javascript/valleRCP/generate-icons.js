const fs = require('fs');
const path = require('path');

// Script para generar iconos PWA desde SVG
// Requiere: npm install sharp --save-dev

const sharp = require('sharp');

const iconSizes = [
  { name: 'favicon-16x16.png', size: 16 },
  { name: 'favicon-32x32.png', size: 32 },
  { name: 'apple-touch-icon-60x60.png', size: 60 },
  { name: 'apple-touch-icon-76x76.png', size: 76 },
  { name: 'apple-touch-icon-120x120.png', size: 120 },
  { name: 'apple-touch-icon-152x152.png', size: 152 },
  { name: 'apple-touch-icon-180x180.png', size: 180 },
  { name: 'apple-touch-icon.png', size: 180 },
  { name: 'msapplication-icon-144x144.png', size: 144 },
  { name: 'mstile-150x150.png', size: 150 },
  { name: 'android-chrome-192x192.png', size: 192 },
  { name: 'android-chrome-512x512.png', size: 512 },
  { name: 'android-chrome-maskable-192x192.png', size: 192, padding: 0.1 },
  { name: 'android-chrome-maskable-512x512.png', size: 512, padding: 0.1 }
];

const svgPath = path.join(__dirname, 'public', 'img', 'icons', 'icon.svg');
const iconsDir = path.join(__dirname, 'public', 'img', 'icons');

async function generateIcons() {
  console.log('🎨 Generando iconos PWA...\n');

  for (const icon of iconSizes) {
    try {
      const outputPath = path.join(iconsDir, icon.name);
      
      let sharpInstance = sharp(svgPath).resize(icon.size, icon.size);
      
      // Para iconos maskable, añadir padding
      if (icon.padding) {
        const paddedSize = Math.round(icon.size * (1 - icon.padding * 2));
        const padding = Math.round(icon.size * icon.padding);
        
        sharpInstance = sharp(svgPath)
          .resize(paddedSize, paddedSize)
          .extend({
            top: padding,
            bottom: padding,
            left: padding,
            right: padding,
            background: { r: 207, g: 182, b: 212, alpha: 1 }
          });
      }
      
      await sharpInstance.png().toFile(outputPath);
      console.log(`✓ Generado: ${icon.name} (${icon.size}x${icon.size})`);
    } catch (error) {
      console.error(`✗ Error generando ${icon.name}:`, error.message);
    }
  }

  // Generar favicon.ico
  try {
    await sharp(svgPath)
      .resize(32, 32)
      .png()
      .toFile(path.join(__dirname, 'public', 'favicon-temp.png'));
    
    console.log('\n✓ Iconos generados exitosamente!');
    console.log('\n💡 Nota: Para favicon.ico, puedes usar una herramienta online como');
    console.log('   https://favicon.io/ o https://realfavicongenerator.net/');
  } catch (error) {
    console.error('Error:', error.message);
  }
}

generateIcons();
