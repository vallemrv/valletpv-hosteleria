export const contains = (instrucciones, inst, reg) => {
                let contains = false;
                instrucciones.forEach( (obj) => {
                    if (obj.id == inst.id){
                        if (reg){
                            var col_obj = Object.keys(obj.reg)[0]
                            var col_inst = Object.keys(inst.reg)[0]
                            if (col_obj == col_inst) {
                                obj.reg = inst.reg
                            }
                        }        
                        contains = true
                    }
                });
                return contains;
            }   
