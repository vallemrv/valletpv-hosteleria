import { defineStore } from 'pinia';
import { fb_get, fb_set } from '@/api';


export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
  }),
  actions: {
    async set(user) {
        // Buscamos si el usuario esta en la base de datos en la colección usuarios
        if (!user){ this.user = user; return;}
        let {doc: usuario} = await fb_get("usuarios", user.uid);
        if (!usuario){
            let {doc: userNew } = await fb_set("usuarios", user.uid, {
                displayName: user.displayName,
                email: user.email,
                photoURL: user.photoURL,
                emailVerified: user.emailVerified,
            });
            usuario = userNew;
        }
        this.user = usuario;
          
    },
    removeUser() {
      this.user = null;
    },
    getDisplayName() {
      if (!this.user) return "No user";
      return this.user.displayName || this.user.email ;
    }
  },
});