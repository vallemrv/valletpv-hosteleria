
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
          let buttonAudio = document.getElementById('eventAudio');
          buttonAudio.setAttribute('src',audio)
          buttonAudio.play()
        }
      }
  }
}