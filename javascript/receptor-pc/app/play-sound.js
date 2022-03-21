
const path = require("path")

exports.play = (res) => {
  console.log(res)
}


exports.click_sound = () => {
   url_sound = path.join(__dirname, "../sound/beep-06.wav")
   console.log(url_sound)

   var player  = require('play-sound')(opts={})
   player.play(url_sound, (err)=>{
       if (err) console.log(err)
   })
}
