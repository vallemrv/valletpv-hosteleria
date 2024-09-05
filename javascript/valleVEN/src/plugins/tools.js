export default {
    install: (app, options) => {
        
        app.config.globalProperties.$tools = {
            valid_form(item, form){
                var text = "";
               form.forEach((e) => {
                   if (e.required && item[e.col] == null){
                     text += e.label + " es obligatorio "
                   }
               }) 
               return text == "" ? null : text    
            },
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
                    if (e.default != null) item[e.col] = e.default;
                    else if (e.tp == "text" || e.tp == "password") item[e.col] = "dddd";
                    else if( e.tp == "multiple") item[e.col] = [];
                    else item[e.col] = null
                });
                return item
            },
            stringToArray(str){
                if (typeof(str) == "string"){
                    str = str.replaceAll("[", "").replaceAll("]","")
                    str = str.replaceAll("'", "").replaceAll('"',"")
                    if (str == "" || str == "0") str = []
                    else str = str.split(", ")
                }
                return str
            } 
        }
    }
}
