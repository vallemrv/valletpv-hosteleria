
// Importar archivos de audio
import audio from '@/assets/mario.mp3'


 
 // Método 2: agregar atributos personalizados
document.body.addEventListener('click',function( e ){
  let event = e || window.event;
  let target = event.target;
  let clickMusic = target.getAttribute('clickMusic')
  if(clickMusic==='true') Vue.prototype.playAudio();
  else return false;
})

export default {
  install: (app, options) => {
      app.config.globalProperties.$notification = {
        playAudio() {
          try {
            let buttonAudio = document.getElementById('eventAudio');
            if (buttonAudio) {
              // Si ya tiene la fuente correcta, solo reproduce
              if (buttonAudio.src !== audio) {
                buttonAudio.setAttribute('src', audio);
              }
              
              // Esperar a que esté listo antes de reproducir
              const playPromise = buttonAudio.play();
              
              if (playPromise !== undefined) {
                playPromise.catch(error => {
                  console.log('Error al reproducir audio:', error);
                  // Si falla, intentar cargar y reproducir después
                  buttonAudio.addEventListener('canplay', () => {
                    buttonAudio.play().catch(e => console.log('Error al reproducir:', e));
                  }, { once: true });
                });
              }
            }
          } catch (error) {
            console.log('Error en playAudio:', error);
          }
        }
      }
  }
}