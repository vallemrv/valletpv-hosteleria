// src/store/chatStore.js
import { defineStore } from "pinia";

export const useChatStore = defineStore("chat", {
  state: () => ({
    items: [],
  }),
  actions: {
    setItems(newItems) {
      this.items = newItems;
    },
    addItems(newItem) {
      this.items.push(newItem);
    },
  },
});
