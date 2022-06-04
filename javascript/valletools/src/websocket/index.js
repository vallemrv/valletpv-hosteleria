import * as types from "@/store/mutations_types"

class Timer {
    constructor(callback, timerCalc){
      this.callback  = callback;
      this.timerCalc = timerCalc;
      this.timer     = null;
      this.tries     = 0;
    }
  
    reset(){
      this.tries = 0
      clearTimeout(this.timer)
    }

    scheduleTimeout(){
      clearTimeout(this.timer)
      this.timer = setTimeout(() => {
        this.tries = this.tries + 1
        this.callback()
      }, this.timerCalc(this.tries + 1))
    }
  }

export default class  {
    constructor(server, ws_name, commit){
        this.socketUrl = "ws://"+server+"/ws/comunicacion/"+ws_name;
        this.customSocket = null;
        this.commit = commit;
        this.ws_name = ws_name;
        this.reconnectTimer = new Timer(() => {
            this.disconnect()
            this.connect()
        }, this.reconnectAfterMs)
    }

    reconnectAfterMs(tries){
        return [1000, 2000, 5000, 10000][tries - 1] || 10000
    }

    connect(){
      console.log("holaa ws")
        // Create new socket
        if (this.customSocket) this.disconnect();
        this.customSocket = new WebSocket(this.socketUrl)

        // onopen - called when connection is open and ready to send and receive data.
        this.customSocket.onopen = (event) => {
          this.reconnectTimer.reset();
          this.commit(types.ON_CONNECT)
          //console.log(event)
        }

        // onclsoe - called when the connection's closes.
        this.customSocket.onclose = (event) => {
            this.commit(types.ON_DISCONECT)
            this.reconnectTimer.scheduleTimeout()
            //console.log(event)

        }

        // onerror - called when an error occurs.
        this.customSocket.onerror = (event) => {
            this.commit(types.ON_DISCONECT)
            //console.log(event)
        }

        // onmessage - called when a message is received from the server.
        this.customSocket.onmessage = (event) => {
          this.commit(types.ON_MENSAJE, JSON.parse(event.data));
        }
           
    }

    disconnect(){
      console.log("adios ws")
      this.customSocket.onclose = function(){}
      this.customSocket.close()
    }
}