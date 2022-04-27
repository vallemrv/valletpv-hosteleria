export default {
    install: (app, options) => {
        
        app.config.globalProperties.$tools = {
            componentToHex(c)  {
               var hex = c.toString(16);
               return hex.length == 1 ? "0" + hex : hex;
            },      
            rgbToHex(val){
                let rgb = val.split(",");
                let r = parseInt(rgb[0]);
                let g = parseInt(rgb[1]);
                let b = parseInt(rgb[2]);
                return "#" + this.componentToHex(r) + this.componentToHex(g) + this.componentToHex(b);
            },
            salir(store){
                localStorage.removeItem("token")
                localStorage.removeItem("user")
                store.state.user = null
                store.state.token = null
              },
            newItem(form){
                let item = {}
                form.forEach((e) => {
                    if (e.tp == "text") item[e.col] = "";
                    else if( e.tp == "multiple") item[e.col] = []
                });
                return item
            },
            stringToArray(str){
                if (typeof(str) == "string"){
                    str = str.replaceAll("[", "").replaceAll("]","")
                    str = str.replaceAll("'", "").replaceAll('"',"")
                    if (str == "") str = []
                    else str = str.split(", ")
                }
                return str
            } 
        }
    }
}
