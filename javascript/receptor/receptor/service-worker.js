if(!self.define){let e,t={};const r=(r,s)=>(r=new URL(r+".js",s).href,t[r]||new Promise((t=>{if("document"in self){const e=document.createElement("script");e.src=r,e.onload=t,document.head.appendChild(e)}else e=r,importScripts(r),t()})).then((()=>{let e=t[r];if(!e)throw new Error(`Module ${r} didn’t register its module`);return e})));self.define=(s,i)=>{const o=e||("document"in self?document.currentScript.src:"")||location.href;if(t[o])return;let n={};const c=e=>r(e,o),l={module:{uri:o},exports:n,require:c};t[o]=Promise.all(s.map((e=>l[e]||c(e)))).then((e=>(i(...e),n)))}}define(["./workbox-2d118ab0"],(function(e){"use strict";e.setCacheNameDetails({prefix:"receptor"}),self.addEventListener("message",(e=>{e.data&&"SKIP_WAITING"===e.data.type&&self.skipWaiting()})),e.precacheAndRoute([{url:"/static/receptor/css/chunk-vendors.fdfe8573.css",revision:null},{url:"/static/receptor/css/index.e479e439.css",revision:null},{url:"/static/receptor/fonts/materialdesignicons-webfont.21f691f0.ttf",revision:null},{url:"/static/receptor/fonts/materialdesignicons-webfont.54b0f60d.woff2",revision:null},{url:"/static/receptor/fonts/materialdesignicons-webfont.5d875350.eot",revision:null},{url:"/static/receptor/fonts/materialdesignicons-webfont.d671cbf6.woff",revision:null},{url:"/static/receptor/index.html",revision:"63f26ce01ab65a088c3b803b2bb32161"},{url:"/static/receptor/js/chunk-vendors.e51d33b9.js",revision:null},{url:"/static/receptor/js/index.5192ebf7.js",revision:null},{url:"/static/receptor/js/webfontloader.0549a253.js",revision:null},{url:"/static/receptor/manifest.json",revision:"1a37d502b9e8eb36b13f1b90a4a9079a"},{url:"/static/receptor/media/mario.4fd75cc9.mp3",revision:null},{url:"/static/receptor/robots.txt",revision:"b6216d61c03e6ce0c9aea6ca7808f7ca"}],{})}));
//# sourceMappingURL=service-worker.js.map
