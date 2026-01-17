var __defProp = Object.defineProperty;
var __defNormalProp = (obj, key, value) => key in obj ? __defProp(obj, key, { enumerable: true, configurable: true, writable: true, value }) : obj[key] = value;
var __publicField = (obj, key, value) => __defNormalProp(obj, typeof key !== "symbol" ? key + "" : key, value);
(function() {
  "use strict";
  var commonjsGlobal = typeof globalThis !== "undefined" ? globalThis : typeof window !== "undefined" ? window : typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : {};
  function getDefaultExportFromCjs(x) {
    return x && x.__esModule && Object.prototype.hasOwnProperty.call(x, "default") ? x["default"] : x;
  }
  var dexie_min$1 = { exports: {} };
  var dexie_min = dexie_min$1.exports;
  var hasRequiredDexie_min;
  function requireDexie_min() {
    if (hasRequiredDexie_min) return dexie_min$1.exports;
    hasRequiredDexie_min = 1;
    (function(module, exports$1) {
      (function(e, t) {
        module.exports = t();
      })(dexie_min, function() {
        var s = function(e2, t2) {
          return (s = Object.setPrototypeOf || { __proto__: [] } instanceof Array && function(e3, t3) {
            e3.__proto__ = t3;
          } || function(e3, t3) {
            for (var n2 in t3) Object.prototype.hasOwnProperty.call(t3, n2) && (e3[n2] = t3[n2]);
          })(e2, t2);
        };
        var _ = function() {
          return (_ = Object.assign || function(e2) {
            for (var t2, n2 = 1, r2 = arguments.length; n2 < r2; n2++) for (var i2 in t2 = arguments[n2]) Object.prototype.hasOwnProperty.call(t2, i2) && (e2[i2] = t2[i2]);
            return e2;
          }).apply(this, arguments);
        };
        function i(e2, t2, n2) {
          for (var r2, i2 = 0, o2 = t2.length; i2 < o2; i2++) !r2 && i2 in t2 || ((r2 = r2 || Array.prototype.slice.call(t2, 0, i2))[i2] = t2[i2]);
          return e2.concat(r2 || Array.prototype.slice.call(t2));
        }
        var f = "undefined" != typeof globalThis ? globalThis : "undefined" != typeof self ? self : "undefined" != typeof window ? window : commonjsGlobal, O = Object.keys, x = Array.isArray;
        function a(t2, n2) {
          return "object" != typeof n2 || O(n2).forEach(function(e2) {
            t2[e2] = n2[e2];
          }), t2;
        }
        "undefined" == typeof Promise || f.Promise || (f.Promise = Promise);
        var c = Object.getPrototypeOf, n = {}.hasOwnProperty;
        function m(e2, t2) {
          return n.call(e2, t2);
        }
        function r(t2, n2) {
          "function" == typeof n2 && (n2 = n2(c(t2))), ("undefined" == typeof Reflect ? O : Reflect.ownKeys)(n2).forEach(function(e2) {
            l(t2, e2, n2[e2]);
          });
        }
        var u = Object.defineProperty;
        function l(e2, t2, n2, r2) {
          u(e2, t2, a(n2 && m(n2, "get") && "function" == typeof n2.get ? { get: n2.get, set: n2.set, configurable: true } : { value: n2, configurable: true, writable: true }, r2));
        }
        function o(t2) {
          return { from: function(e2) {
            return t2.prototype = Object.create(e2.prototype), l(t2.prototype, "constructor", t2), { extend: r.bind(null, t2.prototype) };
          } };
        }
        var h = Object.getOwnPropertyDescriptor;
        var d = [].slice;
        function b(e2, t2, n2) {
          return d.call(e2, t2, n2);
        }
        function p(e2, t2) {
          return t2(e2);
        }
        function y(e2) {
          if (!e2) throw new Error("Assertion Failed");
        }
        function v(e2) {
          f.setImmediate ? setImmediate(e2) : setTimeout(e2, 0);
        }
        function g(e2, t2) {
          if ("string" == typeof t2 && m(e2, t2)) return e2[t2];
          if (!t2) return e2;
          if ("string" != typeof t2) {
            for (var n2 = [], r2 = 0, i2 = t2.length; r2 < i2; ++r2) {
              var o2 = g(e2, t2[r2]);
              n2.push(o2);
            }
            return n2;
          }
          var a2 = t2.indexOf(".");
          if (-1 !== a2) {
            var u2 = e2[t2.substr(0, a2)];
            return null == u2 ? void 0 : g(u2, t2.substr(a2 + 1));
          }
        }
        function w(e2, t2, n2) {
          if (e2 && void 0 !== t2 && !("isFrozen" in Object && Object.isFrozen(e2))) if ("string" != typeof t2 && "length" in t2) {
            y("string" != typeof n2 && "length" in n2);
            for (var r2 = 0, i2 = t2.length; r2 < i2; ++r2) w(e2, t2[r2], n2[r2]);
          } else {
            var o2, a2, u2 = t2.indexOf(".");
            -1 !== u2 ? (o2 = t2.substr(0, u2), "" === (a2 = t2.substr(u2 + 1)) ? void 0 === n2 ? x(e2) && !isNaN(parseInt(o2)) ? e2.splice(o2, 1) : delete e2[o2] : e2[o2] = n2 : w(u2 = !(u2 = e2[o2]) || !m(e2, o2) ? e2[o2] = {} : u2, a2, n2)) : void 0 === n2 ? x(e2) && !isNaN(parseInt(t2)) ? e2.splice(t2, 1) : delete e2[t2] : e2[t2] = n2;
          }
        }
        function k(e2) {
          var t2, n2 = {};
          for (t2 in e2) m(e2, t2) && (n2[t2] = e2[t2]);
          return n2;
        }
        var t = [].concat;
        function P(e2) {
          return t.apply([], e2);
        }
        var e = "BigUint64Array,BigInt64Array,Array,Boolean,String,Date,RegExp,Blob,File,FileList,FileSystemFileHandle,FileSystemDirectoryHandle,ArrayBuffer,DataView,Uint8ClampedArray,ImageBitmap,ImageData,Map,Set,CryptoKey".split(",").concat(P([8, 16, 32, 64].map(function(t2) {
          return ["Int", "Uint", "Float"].map(function(e2) {
            return e2 + t2 + "Array";
          });
        }))).filter(function(e2) {
          return f[e2];
        }), K = new Set(e.map(function(e2) {
          return f[e2];
        }));
        var E = null;
        function S(e2) {
          E = /* @__PURE__ */ new WeakMap();
          e2 = (function e3(t2) {
            if (!t2 || "object" != typeof t2) return t2;
            var n2 = E.get(t2);
            if (n2) return n2;
            if (x(t2)) {
              n2 = [], E.set(t2, n2);
              for (var r2 = 0, i2 = t2.length; r2 < i2; ++r2) n2.push(e3(t2[r2]));
            } else if (K.has(t2.constructor)) n2 = t2;
            else {
              var o2, a2 = c(t2);
              for (o2 in n2 = a2 === Object.prototype ? {} : Object.create(a2), E.set(t2, n2), t2) m(t2, o2) && (n2[o2] = e3(t2[o2]));
            }
            return n2;
          })(e2);
          return E = null, e2;
        }
        var j = {}.toString;
        function A(e2) {
          return j.call(e2).slice(8, -1);
        }
        var C = "undefined" != typeof Symbol ? Symbol.iterator : "@@iterator", T = "symbol" == typeof C ? function(e2) {
          var t2;
          return null != e2 && (t2 = e2[C]) && t2.apply(e2);
        } : function() {
          return null;
        };
        function I(e2, t2) {
          t2 = e2.indexOf(t2);
          return 0 <= t2 && e2.splice(t2, 1), 0 <= t2;
        }
        var q = {};
        function D(e2) {
          var t2, n2, r2, i2;
          if (1 === arguments.length) {
            if (x(e2)) return e2.slice();
            if (this === q && "string" == typeof e2) return [e2];
            if (i2 = T(e2)) {
              for (n2 = []; !(r2 = i2.next()).done; ) n2.push(r2.value);
              return n2;
            }
            if (null == e2) return [e2];
            if ("number" != typeof (t2 = e2.length)) return [e2];
            for (n2 = new Array(t2); t2--; ) n2[t2] = e2[t2];
            return n2;
          }
          for (t2 = arguments.length, n2 = new Array(t2); t2--; ) n2[t2] = arguments[t2];
          return n2;
        }
        var B = "undefined" != typeof Symbol ? function(e2) {
          return "AsyncFunction" === e2[Symbol.toStringTag];
        } : function() {
          return false;
        }, R = ["Unknown", "Constraint", "Data", "TransactionInactive", "ReadOnly", "Version", "NotFound", "InvalidState", "InvalidAccess", "Abort", "Timeout", "QuotaExceeded", "Syntax", "DataClone"], F = ["Modify", "Bulk", "OpenFailed", "VersionChange", "Schema", "Upgrade", "InvalidTable", "MissingAPI", "NoSuchDatabase", "InvalidArgument", "SubTransaction", "Unsupported", "Internal", "DatabaseClosed", "PrematureCommit", "ForeignAwait"].concat(R), M = { VersionChanged: "Database version changed by other database connection", DatabaseClosed: "Database has been closed", Abort: "Transaction aborted", TransactionInactive: "Transaction has already completed or failed", MissingAPI: "IndexedDB API missing. Please visit https://tinyurl.com/y2uuvskb" };
        function N(e2, t2) {
          this.name = e2, this.message = t2;
        }
        function L(e2, t2) {
          return e2 + ". Errors: " + Object.keys(t2).map(function(e3) {
            return t2[e3].toString();
          }).filter(function(e3, t3, n2) {
            return n2.indexOf(e3) === t3;
          }).join("\n");
        }
        function U(e2, t2, n2, r2) {
          this.failures = t2, this.failedKeys = r2, this.successCount = n2, this.message = L(e2, t2);
        }
        function V(e2, t2) {
          this.name = "BulkError", this.failures = Object.keys(t2).map(function(e3) {
            return t2[e3];
          }), this.failuresByPos = t2, this.message = L(e2, this.failures);
        }
        o(N).from(Error).extend({ toString: function() {
          return this.name + ": " + this.message;
        } }), o(U).from(N), o(V).from(N);
        var z = F.reduce(function(e2, t2) {
          return e2[t2] = t2 + "Error", e2;
        }, {}), W = N, Y = F.reduce(function(e2, n2) {
          var r2 = n2 + "Error";
          function t2(e3, t3) {
            this.name = r2, e3 ? "string" == typeof e3 ? (this.message = "".concat(e3).concat(t3 ? "\n " + t3 : ""), this.inner = t3 || null) : "object" == typeof e3 && (this.message = "".concat(e3.name, " ").concat(e3.message), this.inner = e3) : (this.message = M[n2] || r2, this.inner = null);
          }
          return o(t2).from(W), e2[n2] = t2, e2;
        }, {});
        Y.Syntax = SyntaxError, Y.Type = TypeError, Y.Range = RangeError;
        var $ = R.reduce(function(e2, t2) {
          return e2[t2 + "Error"] = Y[t2], e2;
        }, {});
        var Q = F.reduce(function(e2, t2) {
          return -1 === ["Syntax", "Type", "Range"].indexOf(t2) && (e2[t2 + "Error"] = Y[t2]), e2;
        }, {});
        function G() {
        }
        function X(e2) {
          return e2;
        }
        function H(t2, n2) {
          return null == t2 || t2 === X ? n2 : function(e2) {
            return n2(t2(e2));
          };
        }
        function J(e2, t2) {
          return function() {
            e2.apply(this, arguments), t2.apply(this, arguments);
          };
        }
        function Z(i2, o2) {
          return i2 === G ? o2 : function() {
            var e2 = i2.apply(this, arguments);
            void 0 !== e2 && (arguments[0] = e2);
            var t2 = this.onsuccess, n2 = this.onerror;
            this.onsuccess = null, this.onerror = null;
            var r2 = o2.apply(this, arguments);
            return t2 && (this.onsuccess = this.onsuccess ? J(t2, this.onsuccess) : t2), n2 && (this.onerror = this.onerror ? J(n2, this.onerror) : n2), void 0 !== r2 ? r2 : e2;
          };
        }
        function ee(n2, r2) {
          return n2 === G ? r2 : function() {
            n2.apply(this, arguments);
            var e2 = this.onsuccess, t2 = this.onerror;
            this.onsuccess = this.onerror = null, r2.apply(this, arguments), e2 && (this.onsuccess = this.onsuccess ? J(e2, this.onsuccess) : e2), t2 && (this.onerror = this.onerror ? J(t2, this.onerror) : t2);
          };
        }
        function te(i2, o2) {
          return i2 === G ? o2 : function(e2) {
            var t2 = i2.apply(this, arguments);
            a(e2, t2);
            var n2 = this.onsuccess, r2 = this.onerror;
            this.onsuccess = null, this.onerror = null;
            e2 = o2.apply(this, arguments);
            return n2 && (this.onsuccess = this.onsuccess ? J(n2, this.onsuccess) : n2), r2 && (this.onerror = this.onerror ? J(r2, this.onerror) : r2), void 0 === t2 ? void 0 === e2 ? void 0 : e2 : a(t2, e2);
          };
        }
        function ne(e2, t2) {
          return e2 === G ? t2 : function() {
            return false !== t2.apply(this, arguments) && e2.apply(this, arguments);
          };
        }
        function re(i2, o2) {
          return i2 === G ? o2 : function() {
            var e2 = i2.apply(this, arguments);
            if (e2 && "function" == typeof e2.then) {
              for (var t2 = this, n2 = arguments.length, r2 = new Array(n2); n2--; ) r2[n2] = arguments[n2];
              return e2.then(function() {
                return o2.apply(t2, r2);
              });
            }
            return o2.apply(this, arguments);
          };
        }
        Q.ModifyError = U, Q.DexieError = N, Q.BulkError = V;
        var ie = "undefined" != typeof location && /^(http|https):\/\/(localhost|127\.0\.0\.1)/.test(location.href);
        function oe(e2) {
          ie = e2;
        }
        var ae = {}, ue = 100, e = "undefined" == typeof Promise ? [] : (function() {
          var e2 = Promise.resolve();
          if ("undefined" == typeof crypto || !crypto.subtle) return [e2, c(e2), e2];
          var t2 = crypto.subtle.digest("SHA-512", new Uint8Array([0]));
          return [t2, c(t2), e2];
        })(), R = e[0], F = e[1], e = e[2], F = F && F.then, se = R && R.constructor, ce = !!e;
        var le = function(e2, t2) {
          be.push([e2, t2]), he && (queueMicrotask(Se), he = false);
        }, fe = true, he = true, de = [], pe = [], ye = X, ve = { id: "global", global: true, ref: 0, unhandleds: [], onunhandled: G, pgp: false, env: {}, finalize: G }, me = ve, be = [], ge = 0, we = [];
        function _e(e2) {
          if ("object" != typeof this) throw new TypeError("Promises must be constructed via new");
          this._listeners = [], this._lib = false;
          var t2 = this._PSD = me;
          if ("function" != typeof e2) {
            if (e2 !== ae) throw new TypeError("Not a function");
            return this._state = arguments[1], this._value = arguments[2], void (false === this._state && Oe(this, this._value));
          }
          this._state = null, this._value = null, ++t2.ref, (function t3(r2, e3) {
            try {
              e3(function(n2) {
                if (null === r2._state) {
                  if (n2 === r2) throw new TypeError("A promise cannot be resolved with itself.");
                  var e4 = r2._lib && je();
                  n2 && "function" == typeof n2.then ? t3(r2, function(e5, t4) {
                    n2 instanceof _e ? n2._then(e5, t4) : n2.then(e5, t4);
                  }) : (r2._state = true, r2._value = n2, Pe(r2)), e4 && Ae();
                }
              }, Oe.bind(null, r2));
            } catch (e4) {
              Oe(r2, e4);
            }
          })(this, e2);
        }
        var xe = { get: function() {
          var u2 = me, t2 = Fe;
          function e2(n2, r2) {
            var i2 = this, o2 = !u2.global && (u2 !== me || t2 !== Fe), a2 = o2 && !Ue(), e3 = new _e(function(e4, t3) {
              Ke(i2, new ke(Qe(n2, u2, o2, a2), Qe(r2, u2, o2, a2), e4, t3, u2));
            });
            return this._consoleTask && (e3._consoleTask = this._consoleTask), e3;
          }
          return e2.prototype = ae, e2;
        }, set: function(e2) {
          l(this, "then", e2 && e2.prototype === ae ? xe : { get: function() {
            return e2;
          }, set: xe.set });
        } };
        function ke(e2, t2, n2, r2, i2) {
          this.onFulfilled = "function" == typeof e2 ? e2 : null, this.onRejected = "function" == typeof t2 ? t2 : null, this.resolve = n2, this.reject = r2, this.psd = i2;
        }
        function Oe(e2, t2) {
          var n2, r2;
          pe.push(t2), null === e2._state && (n2 = e2._lib && je(), t2 = ye(t2), e2._state = false, e2._value = t2, r2 = e2, de.some(function(e3) {
            return e3._value === r2._value;
          }) || de.push(r2), Pe(e2), n2 && Ae());
        }
        function Pe(e2) {
          var t2 = e2._listeners;
          e2._listeners = [];
          for (var n2 = 0, r2 = t2.length; n2 < r2; ++n2) Ke(e2, t2[n2]);
          var i2 = e2._PSD;
          --i2.ref || i2.finalize(), 0 === ge && (++ge, le(function() {
            0 == --ge && Ce();
          }, []));
        }
        function Ke(e2, t2) {
          if (null !== e2._state) {
            var n2 = e2._state ? t2.onFulfilled : t2.onRejected;
            if (null === n2) return (e2._state ? t2.resolve : t2.reject)(e2._value);
            ++t2.psd.ref, ++ge, le(Ee, [n2, e2, t2]);
          } else e2._listeners.push(t2);
        }
        function Ee(e2, t2, n2) {
          try {
            var r2, i2 = t2._value;
            !t2._state && pe.length && (pe = []), r2 = ie && t2._consoleTask ? t2._consoleTask.run(function() {
              return e2(i2);
            }) : e2(i2), t2._state || -1 !== pe.indexOf(i2) || (function(e3) {
              var t3 = de.length;
              for (; t3; ) if (de[--t3]._value === e3._value) return de.splice(t3, 1);
            })(t2), n2.resolve(r2);
          } catch (e3) {
            n2.reject(e3);
          } finally {
            0 == --ge && Ce(), --n2.psd.ref || n2.psd.finalize();
          }
        }
        function Se() {
          $e(ve, function() {
            je() && Ae();
          });
        }
        function je() {
          var e2 = fe;
          return he = fe = false, e2;
        }
        function Ae() {
          var e2, t2, n2;
          do {
            for (; 0 < be.length; ) for (e2 = be, be = [], n2 = e2.length, t2 = 0; t2 < n2; ++t2) {
              var r2 = e2[t2];
              r2[0].apply(null, r2[1]);
            }
          } while (0 < be.length);
          he = fe = true;
        }
        function Ce() {
          var e2 = de;
          de = [], e2.forEach(function(e3) {
            e3._PSD.onunhandled.call(null, e3._value, e3);
          });
          for (var t2 = we.slice(0), n2 = t2.length; n2; ) t2[--n2]();
        }
        function Te(e2) {
          return new _e(ae, false, e2);
        }
        function Ie(n2, r2) {
          var i2 = me;
          return function() {
            var e2 = je(), t2 = me;
            try {
              return We(i2, true), n2.apply(this, arguments);
            } catch (e3) {
              r2 && r2(e3);
            } finally {
              We(t2, false), e2 && Ae();
            }
          };
        }
        r(_e.prototype, { then: xe, _then: function(e2, t2) {
          Ke(this, new ke(null, null, e2, t2, me));
        }, catch: function(e2) {
          if (1 === arguments.length) return this.then(null, e2);
          var t2 = e2, n2 = arguments[1];
          return "function" == typeof t2 ? this.then(null, function(e3) {
            return (e3 instanceof t2 ? n2 : Te)(e3);
          }) : this.then(null, function(e3) {
            return (e3 && e3.name === t2 ? n2 : Te)(e3);
          });
        }, finally: function(t2) {
          return this.then(function(e2) {
            return _e.resolve(t2()).then(function() {
              return e2;
            });
          }, function(e2) {
            return _e.resolve(t2()).then(function() {
              return Te(e2);
            });
          });
        }, timeout: function(r2, i2) {
          var o2 = this;
          return r2 < 1 / 0 ? new _e(function(e2, t2) {
            var n2 = setTimeout(function() {
              return t2(new Y.Timeout(i2));
            }, r2);
            o2.then(e2, t2).finally(clearTimeout.bind(null, n2));
          }) : this;
        } }), "undefined" != typeof Symbol && Symbol.toStringTag && l(_e.prototype, Symbol.toStringTag, "Dexie.Promise"), ve.env = Ye(), r(_e, { all: function() {
          var o2 = D.apply(null, arguments).map(Ve);
          return new _e(function(n2, r2) {
            0 === o2.length && n2([]);
            var i2 = o2.length;
            o2.forEach(function(e2, t2) {
              return _e.resolve(e2).then(function(e3) {
                o2[t2] = e3, --i2 || n2(o2);
              }, r2);
            });
          });
        }, resolve: function(n2) {
          return n2 instanceof _e ? n2 : n2 && "function" == typeof n2.then ? new _e(function(e2, t2) {
            n2.then(e2, t2);
          }) : new _e(ae, true, n2);
        }, reject: Te, race: function() {
          var e2 = D.apply(null, arguments).map(Ve);
          return new _e(function(t2, n2) {
            e2.map(function(e3) {
              return _e.resolve(e3).then(t2, n2);
            });
          });
        }, PSD: { get: function() {
          return me;
        }, set: function(e2) {
          return me = e2;
        } }, totalEchoes: { get: function() {
          return Fe;
        } }, newPSD: Ne, usePSD: $e, scheduler: { get: function() {
          return le;
        }, set: function(e2) {
          le = e2;
        } }, rejectionMapper: { get: function() {
          return ye;
        }, set: function(e2) {
          ye = e2;
        } }, follow: function(i2, n2) {
          return new _e(function(e2, t2) {
            return Ne(function(n3, r2) {
              var e3 = me;
              e3.unhandleds = [], e3.onunhandled = r2, e3.finalize = J(function() {
                var t3, e4 = this;
                t3 = function() {
                  0 === e4.unhandleds.length ? n3() : r2(e4.unhandleds[0]);
                }, we.push(function e5() {
                  t3(), we.splice(we.indexOf(e5), 1);
                }), ++ge, le(function() {
                  0 == --ge && Ce();
                }, []);
              }, e3.finalize), i2();
            }, n2, e2, t2);
          });
        } }), se && (se.allSettled && l(_e, "allSettled", function() {
          var e2 = D.apply(null, arguments).map(Ve);
          return new _e(function(n2) {
            0 === e2.length && n2([]);
            var r2 = e2.length, i2 = new Array(r2);
            e2.forEach(function(e3, t2) {
              return _e.resolve(e3).then(function(e4) {
                return i2[t2] = { status: "fulfilled", value: e4 };
              }, function(e4) {
                return i2[t2] = { status: "rejected", reason: e4 };
              }).then(function() {
                return --r2 || n2(i2);
              });
            });
          });
        }), se.any && "undefined" != typeof AggregateError && l(_e, "any", function() {
          var e2 = D.apply(null, arguments).map(Ve);
          return new _e(function(n2, r2) {
            0 === e2.length && r2(new AggregateError([]));
            var i2 = e2.length, o2 = new Array(i2);
            e2.forEach(function(e3, t2) {
              return _e.resolve(e3).then(function(e4) {
                return n2(e4);
              }, function(e4) {
                o2[t2] = e4, --i2 || r2(new AggregateError(o2));
              });
            });
          });
        }), se.withResolvers && (_e.withResolvers = se.withResolvers));
        var qe = { awaits: 0, echoes: 0, id: 0 }, De = 0, Be = [], Re = 0, Fe = 0, Me = 0;
        function Ne(e2, t2, n2, r2) {
          var i2 = me, o2 = Object.create(i2);
          o2.parent = i2, o2.ref = 0, o2.global = false, o2.id = ++Me, ve.env, o2.env = ce ? { Promise: _e, PromiseProp: { value: _e, configurable: true, writable: true }, all: _e.all, race: _e.race, allSettled: _e.allSettled, any: _e.any, resolve: _e.resolve, reject: _e.reject } : {}, t2 && a(o2, t2), ++i2.ref, o2.finalize = function() {
            --this.parent.ref || this.parent.finalize();
          };
          r2 = $e(o2, e2, n2, r2);
          return 0 === o2.ref && o2.finalize(), r2;
        }
        function Le() {
          return qe.id || (qe.id = ++De), ++qe.awaits, qe.echoes += ue, qe.id;
        }
        function Ue() {
          return !!qe.awaits && (0 == --qe.awaits && (qe.id = 0), qe.echoes = qe.awaits * ue, true);
        }
        function Ve(e2) {
          return qe.echoes && e2 && e2.constructor === se ? (Le(), e2.then(function(e3) {
            return Ue(), e3;
          }, function(e3) {
            return Ue(), Xe(e3);
          })) : e2;
        }
        function ze() {
          var e2 = Be[Be.length - 1];
          Be.pop(), We(e2, false);
        }
        function We(e2, t2) {
          var n2, r2 = me;
          (t2 ? !qe.echoes || Re++ && e2 === me : !Re || --Re && e2 === me) || queueMicrotask(t2 ? (function(e3) {
            ++Fe, qe.echoes && 0 != --qe.echoes || (qe.echoes = qe.awaits = qe.id = 0), Be.push(me), We(e3, true);
          }).bind(null, e2) : ze), e2 !== me && (me = e2, r2 === ve && (ve.env = Ye()), ce && (n2 = ve.env.Promise, t2 = e2.env, (r2.global || e2.global) && (Object.defineProperty(f, "Promise", t2.PromiseProp), n2.all = t2.all, n2.race = t2.race, n2.resolve = t2.resolve, n2.reject = t2.reject, t2.allSettled && (n2.allSettled = t2.allSettled), t2.any && (n2.any = t2.any))));
        }
        function Ye() {
          var e2 = f.Promise;
          return ce ? { Promise: e2, PromiseProp: Object.getOwnPropertyDescriptor(f, "Promise"), all: e2.all, race: e2.race, allSettled: e2.allSettled, any: e2.any, resolve: e2.resolve, reject: e2.reject } : {};
        }
        function $e(e2, t2, n2, r2, i2) {
          var o2 = me;
          try {
            return We(e2, true), t2(n2, r2, i2);
          } finally {
            We(o2, false);
          }
        }
        function Qe(t2, n2, r2, i2) {
          return "function" != typeof t2 ? t2 : function() {
            var e2 = me;
            r2 && Le(), We(n2, true);
            try {
              return t2.apply(this, arguments);
            } finally {
              We(e2, false), i2 && queueMicrotask(Ue);
            }
          };
        }
        function Ge(e2) {
          Promise === se && 0 === qe.echoes ? 0 === Re ? e2() : enqueueNativeMicroTask(e2) : setTimeout(e2, 0);
        }
        -1 === ("" + F).indexOf("[native code]") && (Le = Ue = G);
        var Xe = _e.reject;
        var He = String.fromCharCode(65535), Je = "Invalid key provided. Keys must be of type string, number, Date or Array<string | number | Date>.", Ze = "String expected.", et = [], tt = "__dbnames", nt = "readonly", rt = "readwrite";
        function it(e2, t2) {
          return e2 ? t2 ? function() {
            return e2.apply(this, arguments) && t2.apply(this, arguments);
          } : e2 : t2;
        }
        var ot = { type: 3, lower: -1 / 0, lowerOpen: false, upper: [[]], upperOpen: false };
        function at(t2) {
          return "string" != typeof t2 || /\./.test(t2) ? function(e2) {
            return e2;
          } : function(e2) {
            return void 0 === e2[t2] && t2 in e2 && delete (e2 = S(e2))[t2], e2;
          };
        }
        function ut() {
          throw Y.Type("Entity instances must never be new:ed. Instances are generated by the framework bypassing the constructor.");
        }
        function st(e2, t2) {
          try {
            var n2 = ct(e2), r2 = ct(t2);
            if (n2 !== r2) return "Array" === n2 ? 1 : "Array" === r2 ? -1 : "binary" === n2 ? 1 : "binary" === r2 ? -1 : "string" === n2 ? 1 : "string" === r2 ? -1 : "Date" === n2 ? 1 : "Date" !== r2 ? NaN : -1;
            switch (n2) {
              case "number":
              case "Date":
              case "string":
                return t2 < e2 ? 1 : e2 < t2 ? -1 : 0;
              case "binary":
                return (function(e3, t3) {
                  for (var n3 = e3.length, r3 = t3.length, i2 = n3 < r3 ? n3 : r3, o2 = 0; o2 < i2; ++o2) if (e3[o2] !== t3[o2]) return e3[o2] < t3[o2] ? -1 : 1;
                  return n3 === r3 ? 0 : n3 < r3 ? -1 : 1;
                })(lt(e2), lt(t2));
              case "Array":
                return (function(e3, t3) {
                  for (var n3 = e3.length, r3 = t3.length, i2 = n3 < r3 ? n3 : r3, o2 = 0; o2 < i2; ++o2) {
                    var a2 = st(e3[o2], t3[o2]);
                    if (0 !== a2) return a2;
                  }
                  return n3 === r3 ? 0 : n3 < r3 ? -1 : 1;
                })(e2, t2);
            }
          } catch (e3) {
          }
          return NaN;
        }
        function ct(e2) {
          var t2 = typeof e2;
          if ("object" != t2) return t2;
          if (ArrayBuffer.isView(e2)) return "binary";
          e2 = A(e2);
          return "ArrayBuffer" === e2 ? "binary" : e2;
        }
        function lt(e2) {
          return e2 instanceof Uint8Array ? e2 : ArrayBuffer.isView(e2) ? new Uint8Array(e2.buffer, e2.byteOffset, e2.byteLength) : new Uint8Array(e2);
        }
        function ft(t2, n2, r2) {
          var e2 = t2.schema.yProps;
          return e2 ? (n2 && 0 < r2.numFailures && (n2 = n2.filter(function(e3, t3) {
            return !r2.failures[t3];
          })), Promise.all(e2.map(function(e3) {
            e3 = e3.updatesTable;
            return n2 ? t2.db.table(e3).where("k").anyOf(n2).delete() : t2.db.table(e3).clear();
          })).then(function() {
            return r2;
          })) : r2;
        }
        var ht = (dt.prototype.execute = function(e2) {
          var t2 = this["@@propmod"];
          if (void 0 !== t2.add) {
            var n2 = t2.add;
            if (x(n2)) return i(i([], x(e2) ? e2 : [], true), n2).sort();
            if ("number" == typeof n2) return (Number(e2) || 0) + n2;
            if ("bigint" == typeof n2) try {
              return BigInt(e2) + n2;
            } catch (e3) {
              return BigInt(0) + n2;
            }
            throw new TypeError("Invalid term ".concat(n2));
          }
          if (void 0 !== t2.remove) {
            var r2 = t2.remove;
            if (x(r2)) return x(e2) ? e2.filter(function(e3) {
              return !r2.includes(e3);
            }).sort() : [];
            if ("number" == typeof r2) return Number(e2) - r2;
            if ("bigint" == typeof r2) try {
              return BigInt(e2) - r2;
            } catch (e3) {
              return BigInt(0) - r2;
            }
            throw new TypeError("Invalid subtrahend ".concat(r2));
          }
          n2 = null === (n2 = t2.replacePrefix) || void 0 === n2 ? void 0 : n2[0];
          return n2 && "string" == typeof e2 && e2.startsWith(n2) ? t2.replacePrefix[1] + e2.substring(n2.length) : e2;
        }, dt);
        function dt(e2) {
          this["@@propmod"] = e2;
        }
        function pt(e2, t2) {
          for (var n2 = O(t2), r2 = n2.length, i2 = false, o2 = 0; o2 < r2; ++o2) {
            var a2 = n2[o2], u2 = t2[a2], s2 = g(e2, a2);
            u2 instanceof ht ? (w(e2, a2, u2.execute(s2)), i2 = true) : s2 !== u2 && (w(e2, a2, u2), i2 = true);
          }
          return i2;
        }
        var yt = (vt.prototype._trans = function(e2, r2, t2) {
          var n2 = this._tx || me.trans, i2 = this.name, o2 = ie && "undefined" != typeof console && console.createTask && console.createTask("Dexie: ".concat("readonly" === e2 ? "read" : "write", " ").concat(this.name));
          function a2(e3, t3, n3) {
            if (!n3.schema[i2]) throw new Y.NotFound("Table " + i2 + " not part of transaction");
            return r2(n3.idbtrans, n3);
          }
          var u2 = je();
          try {
            var s2 = n2 && n2.db._novip === this.db._novip ? n2 === me.trans ? n2._promise(e2, a2, t2) : Ne(function() {
              return n2._promise(e2, a2, t2);
            }, { trans: n2, transless: me.transless || me }) : (function t3(n3, r3, i3, o3) {
              if (n3.idbdb && (n3._state.openComplete || me.letThrough || n3._vip)) {
                var a3 = n3._createTransaction(r3, i3, n3._dbSchema);
                try {
                  a3.create(), n3._state.PR1398_maxLoop = 3;
                } catch (e3) {
                  return e3.name === z.InvalidState && n3.isOpen() && 0 < --n3._state.PR1398_maxLoop ? (console.warn("Dexie: Need to reopen db"), n3.close({ disableAutoOpen: false }), n3.open().then(function() {
                    return t3(n3, r3, i3, o3);
                  })) : Xe(e3);
                }
                return a3._promise(r3, function(e3, t4) {
                  return Ne(function() {
                    return me.trans = a3, o3(e3, t4, a3);
                  });
                }).then(function(e3) {
                  if ("readwrite" === r3) try {
                    a3.idbtrans.commit();
                  } catch (e4) {
                  }
                  return "readonly" === r3 ? e3 : a3._completion.then(function() {
                    return e3;
                  });
                });
              }
              if (n3._state.openComplete) return Xe(new Y.DatabaseClosed(n3._state.dbOpenError));
              if (!n3._state.isBeingOpened) {
                if (!n3._state.autoOpen) return Xe(new Y.DatabaseClosed());
                n3.open().catch(G);
              }
              return n3._state.dbReadyPromise.then(function() {
                return t3(n3, r3, i3, o3);
              });
            })(this.db, e2, [this.name], a2);
            return o2 && (s2._consoleTask = o2, s2 = s2.catch(function(e3) {
              return console.trace(e3), Xe(e3);
            })), s2;
          } finally {
            u2 && Ae();
          }
        }, vt.prototype.get = function(t2, e2) {
          var n2 = this;
          return t2 && t2.constructor === Object ? this.where(t2).first(e2) : null == t2 ? Xe(new Y.Type("Invalid argument to Table.get()")) : this._trans("readonly", function(e3) {
            return n2.core.get({ trans: e3, key: t2 }).then(function(e4) {
              return n2.hook.reading.fire(e4);
            });
          }).then(e2);
        }, vt.prototype.where = function(o2) {
          if ("string" == typeof o2) return new this.db.WhereClause(this, o2);
          if (x(o2)) return new this.db.WhereClause(this, "[".concat(o2.join("+"), "]"));
          var n2 = O(o2);
          if (1 === n2.length) return this.where(n2[0]).equals(o2[n2[0]]);
          var e2 = this.schema.indexes.concat(this.schema.primKey).filter(function(t3) {
            if (t3.compound && n2.every(function(e4) {
              return 0 <= t3.keyPath.indexOf(e4);
            })) {
              for (var e3 = 0; e3 < n2.length; ++e3) if (-1 === n2.indexOf(t3.keyPath[e3])) return false;
              return true;
            }
            return false;
          }).sort(function(e3, t3) {
            return e3.keyPath.length - t3.keyPath.length;
          })[0];
          if (e2 && this.db._maxKey !== He) {
            var t2 = e2.keyPath.slice(0, n2.length);
            return this.where(t2).equals(t2.map(function(e3) {
              return o2[e3];
            }));
          }
          !e2 && ie && console.warn("The query ".concat(JSON.stringify(o2), " on ").concat(this.name, " would benefit from a ") + "compound index [".concat(n2.join("+"), "]"));
          var a2 = this.schema.idxByName;
          function u2(e3, t3) {
            return 0 === st(e3, t3);
          }
          var r2 = n2.reduce(function(e3, t3) {
            var n3 = e3[0], r3 = e3[1], e3 = a2[t3], i2 = o2[t3];
            return [n3 || e3, n3 || !e3 ? it(r3, e3 && e3.multi ? function(e4) {
              e4 = g(e4, t3);
              return x(e4) && e4.some(function(e5) {
                return u2(i2, e5);
              });
            } : function(e4) {
              return u2(i2, g(e4, t3));
            }) : r3];
          }, [null, null]), t2 = r2[0], r2 = r2[1];
          return t2 ? this.where(t2.name).equals(o2[t2.keyPath]).filter(r2) : e2 ? this.filter(r2) : this.where(n2).equals("");
        }, vt.prototype.filter = function(e2) {
          return this.toCollection().and(e2);
        }, vt.prototype.count = function(e2) {
          return this.toCollection().count(e2);
        }, vt.prototype.offset = function(e2) {
          return this.toCollection().offset(e2);
        }, vt.prototype.limit = function(e2) {
          return this.toCollection().limit(e2);
        }, vt.prototype.each = function(e2) {
          return this.toCollection().each(e2);
        }, vt.prototype.toArray = function(e2) {
          return this.toCollection().toArray(e2);
        }, vt.prototype.toCollection = function() {
          return new this.db.Collection(new this.db.WhereClause(this));
        }, vt.prototype.orderBy = function(e2) {
          return new this.db.Collection(new this.db.WhereClause(this, x(e2) ? "[".concat(e2.join("+"), "]") : e2));
        }, vt.prototype.reverse = function() {
          return this.toCollection().reverse();
        }, vt.prototype.mapToClass = function(r2) {
          var e2, t2 = this.db, n2 = this.name;
          function i2() {
            return null !== e2 && e2.apply(this, arguments) || this;
          }
          (this.schema.mappedClass = r2).prototype instanceof ut && ((function(e3, t3) {
            if ("function" != typeof t3 && null !== t3) throw new TypeError("Class extends value " + String(t3) + " is not a constructor or null");
            function n3() {
              this.constructor = e3;
            }
            s(e3, t3), e3.prototype = null === t3 ? Object.create(t3) : (n3.prototype = t3.prototype, new n3());
          })(i2, e2 = r2), Object.defineProperty(i2.prototype, "db", { get: function() {
            return t2;
          }, enumerable: false, configurable: true }), i2.prototype.table = function() {
            return n2;
          }, r2 = i2);
          for (var o2 = /* @__PURE__ */ new Set(), a2 = r2.prototype; a2; a2 = c(a2)) Object.getOwnPropertyNames(a2).forEach(function(e3) {
            return o2.add(e3);
          });
          function u2(e3) {
            if (!e3) return e3;
            var t3, n3 = Object.create(r2.prototype);
            for (t3 in e3) if (!o2.has(t3)) try {
              n3[t3] = e3[t3];
            } catch (e4) {
            }
            return n3;
          }
          return this.schema.readHook && this.hook.reading.unsubscribe(this.schema.readHook), this.schema.readHook = u2, this.hook("reading", u2), r2;
        }, vt.prototype.defineClass = function() {
          return this.mapToClass(function(e2) {
            a(this, e2);
          });
        }, vt.prototype.add = function(t2, n2) {
          var r2 = this, e2 = this.schema.primKey, i2 = e2.auto, o2 = e2.keyPath, a2 = t2;
          return o2 && i2 && (a2 = at(o2)(t2)), this._trans("readwrite", function(e3) {
            return r2.core.mutate({ trans: e3, type: "add", keys: null != n2 ? [n2] : null, values: [a2] });
          }).then(function(e3) {
            return e3.numFailures ? _e.reject(e3.failures[0]) : e3.lastResult;
          }).then(function(e3) {
            if (o2) try {
              w(t2, o2, e3);
            } catch (e4) {
            }
            return e3;
          });
        }, vt.prototype.upsert = function(r2, i2) {
          var o2 = this, a2 = this.schema.primKey.keyPath;
          return this._trans("readwrite", function(n2) {
            return o2.core.get({ trans: n2, key: r2 }).then(function(t2) {
              var e2 = null != t2 ? t2 : {};
              return pt(e2, i2), a2 && w(e2, a2, r2), o2.core.mutate({ trans: n2, type: "put", values: [e2], keys: [r2], upsert: true, updates: { keys: [r2], changeSpecs: [i2] } }).then(function(e3) {
                return e3.numFailures ? _e.reject(e3.failures[0]) : !!t2;
              });
            });
          });
        }, vt.prototype.update = function(e2, t2) {
          if ("object" != typeof e2 || x(e2)) return this.where(":id").equals(e2).modify(t2);
          e2 = g(e2, this.schema.primKey.keyPath);
          return void 0 === e2 ? Xe(new Y.InvalidArgument("Given object does not contain its primary key")) : this.where(":id").equals(e2).modify(t2);
        }, vt.prototype.put = function(t2, n2) {
          var r2 = this, e2 = this.schema.primKey, i2 = e2.auto, o2 = e2.keyPath, a2 = t2;
          return o2 && i2 && (a2 = at(o2)(t2)), this._trans("readwrite", function(e3) {
            return r2.core.mutate({ trans: e3, type: "put", values: [a2], keys: null != n2 ? [n2] : null });
          }).then(function(e3) {
            return e3.numFailures ? _e.reject(e3.failures[0]) : e3.lastResult;
          }).then(function(e3) {
            if (o2) try {
              w(t2, o2, e3);
            } catch (e4) {
            }
            return e3;
          });
        }, vt.prototype.delete = function(t2) {
          var n2 = this;
          return this._trans("readwrite", function(e2) {
            return n2.core.mutate({ trans: e2, type: "delete", keys: [t2] }).then(function(e3) {
              return ft(n2, [t2], e3);
            }).then(function(e3) {
              return e3.numFailures ? _e.reject(e3.failures[0]) : void 0;
            });
          });
        }, vt.prototype.clear = function() {
          var t2 = this;
          return this._trans("readwrite", function(e2) {
            return t2.core.mutate({ trans: e2, type: "deleteRange", range: ot }).then(function(e3) {
              return ft(t2, null, e3);
            });
          }).then(function(e2) {
            return e2.numFailures ? _e.reject(e2.failures[0]) : void 0;
          });
        }, vt.prototype.bulkGet = function(t2) {
          var n2 = this;
          return this._trans("readonly", function(e2) {
            return n2.core.getMany({ keys: t2, trans: e2 }).then(function(e3) {
              return e3.map(function(e4) {
                return n2.hook.reading.fire(e4);
              });
            });
          });
        }, vt.prototype.bulkAdd = function(r2, e2, t2) {
          var o2 = this, a2 = Array.isArray(e2) ? e2 : void 0, u2 = (t2 = t2 || (a2 ? void 0 : e2)) ? t2.allKeys : void 0;
          return this._trans("readwrite", function(e3) {
            var t3 = o2.schema.primKey, n2 = t3.auto, t3 = t3.keyPath;
            if (t3 && a2) throw new Y.InvalidArgument("bulkAdd(): keys argument invalid on tables with inbound keys");
            if (a2 && a2.length !== r2.length) throw new Y.InvalidArgument("Arguments objects and keys must have the same length");
            var i2 = r2.length, t3 = t3 && n2 ? r2.map(at(t3)) : r2;
            return o2.core.mutate({ trans: e3, type: "add", keys: a2, values: t3, wantResults: u2 }).then(function(e4) {
              var t4 = e4.numFailures, n3 = e4.results, r3 = e4.lastResult, e4 = e4.failures;
              if (0 === t4) return u2 ? n3 : r3;
              throw new V("".concat(o2.name, ".bulkAdd(): ").concat(t4, " of ").concat(i2, " operations failed"), e4);
            });
          });
        }, vt.prototype.bulkPut = function(r2, e2, t2) {
          var o2 = this, a2 = Array.isArray(e2) ? e2 : void 0, u2 = (t2 = t2 || (a2 ? void 0 : e2)) ? t2.allKeys : void 0;
          return this._trans("readwrite", function(e3) {
            var t3 = o2.schema.primKey, n2 = t3.auto, t3 = t3.keyPath;
            if (t3 && a2) throw new Y.InvalidArgument("bulkPut(): keys argument invalid on tables with inbound keys");
            if (a2 && a2.length !== r2.length) throw new Y.InvalidArgument("Arguments objects and keys must have the same length");
            var i2 = r2.length, t3 = t3 && n2 ? r2.map(at(t3)) : r2;
            return o2.core.mutate({ trans: e3, type: "put", keys: a2, values: t3, wantResults: u2 }).then(function(e4) {
              var t4 = e4.numFailures, n3 = e4.results, r3 = e4.lastResult, e4 = e4.failures;
              if (0 === t4) return u2 ? n3 : r3;
              throw new V("".concat(o2.name, ".bulkPut(): ").concat(t4, " of ").concat(i2, " operations failed"), e4);
            });
          });
        }, vt.prototype.bulkUpdate = function(t2) {
          var h2 = this, n2 = this.core, r2 = t2.map(function(e2) {
            return e2.key;
          }), i2 = t2.map(function(e2) {
            return e2.changes;
          }), d2 = [];
          return this._trans("readwrite", function(e2) {
            return n2.getMany({ trans: e2, keys: r2, cache: "clone" }).then(function(c2) {
              var l2 = [], f2 = [];
              t2.forEach(function(e3, t3) {
                var n3 = e3.key, r3 = e3.changes, i3 = c2[t3];
                if (i3) {
                  for (var o2 = 0, a2 = Object.keys(r3); o2 < a2.length; o2++) {
                    var u2 = a2[o2], s3 = r3[u2];
                    if (u2 === h2.schema.primKey.keyPath) {
                      if (0 !== st(s3, n3)) throw new Y.Constraint("Cannot update primary key in bulkUpdate()");
                    } else w(i3, u2, s3);
                  }
                  d2.push(t3), l2.push(n3), f2.push(i3);
                }
              });
              var s2 = l2.length;
              return n2.mutate({ trans: e2, type: "put", keys: l2, values: f2, updates: { keys: r2, changeSpecs: i2 } }).then(function(e3) {
                var t3 = e3.numFailures, n3 = e3.failures;
                if (0 === t3) return s2;
                for (var r3 = 0, i3 = Object.keys(n3); r3 < i3.length; r3++) {
                  var o2, a2 = i3[r3], u2 = d2[Number(a2)];
                  null != u2 && (o2 = n3[a2], delete n3[a2], n3[u2] = o2);
                }
                throw new V("".concat(h2.name, ".bulkUpdate(): ").concat(t3, " of ").concat(s2, " operations failed"), n3);
              });
            });
          });
        }, vt.prototype.bulkDelete = function(t2) {
          var r2 = this, i2 = t2.length;
          return this._trans("readwrite", function(e2) {
            return r2.core.mutate({ trans: e2, type: "delete", keys: t2 }).then(function(e3) {
              return ft(r2, t2, e3);
            });
          }).then(function(e2) {
            var t3 = e2.numFailures, n2 = e2.lastResult, e2 = e2.failures;
            if (0 === t3) return n2;
            throw new V("".concat(r2.name, ".bulkDelete(): ").concat(t3, " of ").concat(i2, " operations failed"), e2);
          });
        }, vt);
        function vt() {
        }
        function mt(i2) {
          function t2(e3, t3) {
            if (t3) {
              for (var n3 = arguments.length, r2 = new Array(n3 - 1); --n3; ) r2[n3 - 1] = arguments[n3];
              return a2[e3].subscribe.apply(null, r2), i2;
            }
            if ("string" == typeof e3) return a2[e3];
          }
          var a2 = {};
          t2.addEventType = u2;
          for (var e2 = 1, n2 = arguments.length; e2 < n2; ++e2) u2(arguments[e2]);
          return t2;
          function u2(e3, n3, r2) {
            if ("object" != typeof e3) {
              var i3;
              n3 = n3 || ne;
              var o2 = { subscribers: [], fire: r2 = r2 || G, subscribe: function(e4) {
                -1 === o2.subscribers.indexOf(e4) && (o2.subscribers.push(e4), o2.fire = n3(o2.fire, e4));
              }, unsubscribe: function(t3) {
                o2.subscribers = o2.subscribers.filter(function(e4) {
                  return e4 !== t3;
                }), o2.fire = o2.subscribers.reduce(n3, r2);
              } };
              return a2[e3] = t2[e3] = o2;
            }
            O(i3 = e3).forEach(function(e4) {
              var t3 = i3[e4];
              if (x(t3)) u2(e4, i3[e4][0], i3[e4][1]);
              else {
                if ("asap" !== t3) throw new Y.InvalidArgument("Invalid event config");
                var n4 = u2(e4, X, function() {
                  for (var e5 = arguments.length, t4 = new Array(e5); e5--; ) t4[e5] = arguments[e5];
                  n4.subscribers.forEach(function(e6) {
                    v(function() {
                      e6.apply(null, t4);
                    });
                  });
                });
              }
            });
          }
        }
        function bt(e2, t2) {
          return o(t2).from({ prototype: e2 }), t2;
        }
        function gt(e2, t2) {
          return !(e2.filter || e2.algorithm || e2.or) && (t2 ? e2.justLimit : !e2.replayFilter);
        }
        function wt(e2, t2) {
          e2.filter = it(e2.filter, t2);
        }
        function _t(e2, t2, n2) {
          var r2 = e2.replayFilter;
          e2.replayFilter = r2 ? function() {
            return it(r2(), t2());
          } : t2, e2.justLimit = n2 && !r2;
        }
        function xt(e2, t2) {
          if (e2.isPrimKey) return t2.primaryKey;
          var n2 = t2.getIndexByKeyPath(e2.index);
          if (!n2) throw new Y.Schema("KeyPath " + e2.index + " on object store " + t2.name + " is not indexed");
          return n2;
        }
        function kt(e2, t2, n2) {
          var r2 = xt(e2, t2.schema);
          return t2.openCursor({ trans: n2, values: !e2.keysOnly, reverse: "prev" === e2.dir, unique: !!e2.unique, query: { index: r2, range: e2.range } });
        }
        function Ot(e2, o2, t2, n2) {
          var a2 = e2.replayFilter ? it(e2.filter, e2.replayFilter()) : e2.filter;
          if (e2.or) {
            var u2 = {}, r2 = function(e3, t3, n3) {
              var r3, i2;
              a2 && !a2(t3, n3, function(e4) {
                return t3.stop(e4);
              }, function(e4) {
                return t3.fail(e4);
              }) || ("[object ArrayBuffer]" === (i2 = "" + (r3 = t3.primaryKey)) && (i2 = "" + new Uint8Array(r3)), m(u2, i2) || (u2[i2] = true, o2(e3, t3, n3)));
            };
            return Promise.all([e2.or._iterate(r2, t2), Pt(kt(e2, n2, t2), e2.algorithm, r2, !e2.keysOnly && e2.valueMapper)]);
          }
          return Pt(kt(e2, n2, t2), it(e2.algorithm, a2), o2, !e2.keysOnly && e2.valueMapper);
        }
        function Pt(e2, r2, i2, o2) {
          var a2 = Ie(o2 ? function(e3, t2, n2) {
            return i2(o2(e3), t2, n2);
          } : i2);
          return e2.then(function(n2) {
            if (n2) return n2.start(function() {
              var t2 = function() {
                return n2.continue();
              };
              r2 && !r2(n2, function(e3) {
                return t2 = e3;
              }, function(e3) {
                n2.stop(e3), t2 = G;
              }, function(e3) {
                n2.fail(e3), t2 = G;
              }) || a2(n2.value, n2, function(e3) {
                return t2 = e3;
              }), t2();
            });
          });
        }
        var Kt = (Et.prototype._read = function(e2, t2) {
          var n2 = this._ctx;
          return n2.error ? n2.table._trans(null, Xe.bind(null, n2.error)) : n2.table._trans("readonly", e2).then(t2);
        }, Et.prototype._write = function(e2) {
          var t2 = this._ctx;
          return t2.error ? t2.table._trans(null, Xe.bind(null, t2.error)) : t2.table._trans("readwrite", e2, "locked");
        }, Et.prototype._addAlgorithm = function(e2) {
          var t2 = this._ctx;
          t2.algorithm = it(t2.algorithm, e2);
        }, Et.prototype._iterate = function(e2, t2) {
          return Ot(this._ctx, e2, t2, this._ctx.table.core);
        }, Et.prototype.clone = function(e2) {
          var t2 = Object.create(this.constructor.prototype), n2 = Object.create(this._ctx);
          return e2 && a(n2, e2), t2._ctx = n2, t2;
        }, Et.prototype.raw = function() {
          return this._ctx.valueMapper = null, this;
        }, Et.prototype.each = function(t2) {
          var n2 = this._ctx;
          return this._read(function(e2) {
            return Ot(n2, t2, e2, n2.table.core);
          });
        }, Et.prototype.count = function(e2) {
          var i2 = this;
          return this._read(function(e3) {
            var t2 = i2._ctx, n2 = t2.table.core;
            if (gt(t2, true)) return n2.count({ trans: e3, query: { index: xt(t2, n2.schema), range: t2.range } }).then(function(e4) {
              return Math.min(e4, t2.limit);
            });
            var r2 = 0;
            return Ot(t2, function() {
              return ++r2, false;
            }, e3, n2).then(function() {
              return r2;
            });
          }).then(e2);
        }, Et.prototype.sortBy = function(e2, t2) {
          var n2 = e2.split(".").reverse(), r2 = n2[0], i2 = n2.length - 1;
          function o2(e3, t3) {
            return t3 ? o2(e3[n2[t3]], t3 - 1) : e3[r2];
          }
          var a2 = "next" === this._ctx.dir ? 1 : -1;
          function u2(e3, t3) {
            return st(o2(e3, i2), o2(t3, i2)) * a2;
          }
          return this.toArray(function(e3) {
            return e3.sort(u2);
          }).then(t2);
        }, Et.prototype.toArray = function(e2) {
          var o2 = this;
          return this._read(function(e3) {
            var t2 = o2._ctx;
            if ("next" === t2.dir && gt(t2, true) && 0 < t2.limit) {
              var n2 = t2.valueMapper, r2 = xt(t2, t2.table.core.schema);
              return t2.table.core.query({ trans: e3, limit: t2.limit, values: true, query: { index: r2, range: t2.range } }).then(function(e4) {
                e4 = e4.result;
                return n2 ? e4.map(n2) : e4;
              });
            }
            var i2 = [];
            return Ot(t2, function(e4) {
              return i2.push(e4);
            }, e3, t2.table.core).then(function() {
              return i2;
            });
          }, e2);
        }, Et.prototype.offset = function(t2) {
          var e2 = this._ctx;
          return t2 <= 0 || (e2.offset += t2, gt(e2) ? _t(e2, function() {
            var n2 = t2;
            return function(e3, t3) {
              return 0 === n2 || (1 === n2 ? --n2 : t3(function() {
                e3.advance(n2), n2 = 0;
              }), false);
            };
          }) : _t(e2, function() {
            var e3 = t2;
            return function() {
              return --e3 < 0;
            };
          })), this;
        }, Et.prototype.limit = function(e2) {
          return this._ctx.limit = Math.min(this._ctx.limit, e2), _t(this._ctx, function() {
            var r2 = e2;
            return function(e3, t2, n2) {
              return --r2 <= 0 && t2(n2), 0 <= r2;
            };
          }, true), this;
        }, Et.prototype.until = function(r2, i2) {
          return wt(this._ctx, function(e2, t2, n2) {
            return !r2(e2.value) || (t2(n2), i2);
          }), this;
        }, Et.prototype.first = function(e2) {
          return this.limit(1).toArray(function(e3) {
            return e3[0];
          }).then(e2);
        }, Et.prototype.last = function(e2) {
          return this.reverse().first(e2);
        }, Et.prototype.filter = function(t2) {
          var e2;
          return wt(this._ctx, function(e3) {
            return t2(e3.value);
          }), (e2 = this._ctx).isMatch = it(e2.isMatch, t2), this;
        }, Et.prototype.and = function(e2) {
          return this.filter(e2);
        }, Et.prototype.or = function(e2) {
          return new this.db.WhereClause(this._ctx.table, e2, this);
        }, Et.prototype.reverse = function() {
          return this._ctx.dir = "prev" === this._ctx.dir ? "next" : "prev", this._ondirectionchange && this._ondirectionchange(this._ctx.dir), this;
        }, Et.prototype.desc = function() {
          return this.reverse();
        }, Et.prototype.eachKey = function(n2) {
          var e2 = this._ctx;
          return e2.keysOnly = !e2.isMatch, this.each(function(e3, t2) {
            n2(t2.key, t2);
          });
        }, Et.prototype.eachUniqueKey = function(e2) {
          return this._ctx.unique = "unique", this.eachKey(e2);
        }, Et.prototype.eachPrimaryKey = function(n2) {
          var e2 = this._ctx;
          return e2.keysOnly = !e2.isMatch, this.each(function(e3, t2) {
            n2(t2.primaryKey, t2);
          });
        }, Et.prototype.keys = function(e2) {
          var t2 = this._ctx;
          t2.keysOnly = !t2.isMatch;
          var n2 = [];
          return this.each(function(e3, t3) {
            n2.push(t3.key);
          }).then(function() {
            return n2;
          }).then(e2);
        }, Et.prototype.primaryKeys = function(e2) {
          var n2 = this._ctx;
          if ("next" === n2.dir && gt(n2, true) && 0 < n2.limit) return this._read(function(e3) {
            var t2 = xt(n2, n2.table.core.schema);
            return n2.table.core.query({ trans: e3, values: false, limit: n2.limit, query: { index: t2, range: n2.range } });
          }).then(function(e3) {
            return e3.result;
          }).then(e2);
          n2.keysOnly = !n2.isMatch;
          var r2 = [];
          return this.each(function(e3, t2) {
            r2.push(t2.primaryKey);
          }).then(function() {
            return r2;
          }).then(e2);
        }, Et.prototype.uniqueKeys = function(e2) {
          return this._ctx.unique = "unique", this.keys(e2);
        }, Et.prototype.firstKey = function(e2) {
          return this.limit(1).keys(function(e3) {
            return e3[0];
          }).then(e2);
        }, Et.prototype.lastKey = function(e2) {
          return this.reverse().firstKey(e2);
        }, Et.prototype.distinct = function() {
          var e2 = this._ctx, e2 = e2.index && e2.table.schema.idxByName[e2.index];
          if (!e2 || !e2.multi) return this;
          var n2 = {};
          return wt(this._ctx, function(e3) {
            var t2 = e3.primaryKey.toString(), e3 = m(n2, t2);
            return n2[t2] = true, !e3;
          }), this;
        }, Et.prototype.modify = function(x2) {
          var n2 = this, k2 = this._ctx;
          return this._write(function(p2) {
            var y2 = "function" == typeof x2 ? x2 : function(e3) {
              return pt(e3, x2);
            }, v2 = k2.table.core, e2 = v2.schema.primaryKey, m2 = e2.outbound, b2 = e2.extractKey, g2 = 200, e2 = n2.db._options.modifyChunkSize;
            e2 && (g2 = "object" == typeof e2 ? e2[v2.name] || e2["*"] || 200 : e2);
            function w2(e3, t3) {
              var n3 = t3.failures, t3 = t3.numFailures;
              u2 += e3 - t3;
              for (var r2 = 0, i2 = O(n3); r2 < i2.length; r2++) {
                var o2 = i2[r2];
                a2.push(n3[o2]);
              }
            }
            var a2 = [], u2 = 0, t2 = [], _2 = x2 === St;
            return n2.clone().primaryKeys().then(function(f2) {
              function h2(s2) {
                var c2 = Math.min(g2, f2.length - s2), l2 = f2.slice(s2, s2 + c2);
                return (_2 ? Promise.resolve([]) : v2.getMany({ trans: p2, keys: l2, cache: "immutable" })).then(function(e3) {
                  var n3 = [], t3 = [], r2 = m2 ? [] : null, i2 = _2 ? l2 : [];
                  if (!_2) for (var o2 = 0; o2 < c2; ++o2) {
                    var a3 = e3[o2], u3 = { value: S(a3), primKey: f2[s2 + o2] };
                    false !== y2.call(u3, u3.value, u3) && (null == u3.value ? i2.push(f2[s2 + o2]) : m2 || 0 === st(b2(a3), b2(u3.value)) ? (t3.push(u3.value), m2 && r2.push(f2[s2 + o2])) : (i2.push(f2[s2 + o2]), n3.push(u3.value)));
                  }
                  return Promise.resolve(0 < n3.length && v2.mutate({ trans: p2, type: "add", values: n3 }).then(function(e4) {
                    for (var t4 in e4.failures) i2.splice(parseInt(t4), 1);
                    w2(n3.length, e4);
                  })).then(function() {
                    return (0 < t3.length || d2 && "object" == typeof x2) && v2.mutate({ trans: p2, type: "put", keys: r2, values: t3, criteria: d2, changeSpec: "function" != typeof x2 && x2, isAdditionalChunk: 0 < s2 }).then(function(e4) {
                      return w2(t3.length, e4);
                    });
                  }).then(function() {
                    return (0 < i2.length || d2 && _2) && v2.mutate({ trans: p2, type: "delete", keys: i2, criteria: d2, isAdditionalChunk: 0 < s2 }).then(function(e4) {
                      return ft(k2.table, i2, e4);
                    }).then(function(e4) {
                      return w2(i2.length, e4);
                    });
                  }).then(function() {
                    return f2.length > s2 + c2 && h2(s2 + g2);
                  });
                });
              }
              var d2 = gt(k2) && k2.limit === 1 / 0 && ("function" != typeof x2 || _2) && { index: k2.index, range: k2.range };
              return h2(0).then(function() {
                if (0 < a2.length) throw new U("Error modifying one or more objects", a2, u2, t2);
                return f2.length;
              });
            });
          });
        }, Et.prototype.delete = function() {
          var i2 = this._ctx, n2 = i2.range;
          return !gt(i2) || i2.table.schema.yProps || !i2.isPrimKey && 3 !== n2.type ? this.modify(St) : this._write(function(e2) {
            var t2 = i2.table.core.schema.primaryKey, r2 = n2;
            return i2.table.core.count({ trans: e2, query: { index: t2, range: r2 } }).then(function(n3) {
              return i2.table.core.mutate({ trans: e2, type: "deleteRange", range: r2 }).then(function(e3) {
                var t3 = e3.failures, e3 = e3.numFailures;
                if (e3) throw new U("Could not delete some values", Object.keys(t3).map(function(e4) {
                  return t3[e4];
                }), n3 - e3);
                return n3 - e3;
              });
            });
          });
        }, Et);
        function Et() {
        }
        var St = function(e2, t2) {
          return t2.value = null;
        };
        function jt(e2, t2) {
          return e2 < t2 ? -1 : e2 === t2 ? 0 : 1;
        }
        function At(e2, t2) {
          return t2 < e2 ? -1 : e2 === t2 ? 0 : 1;
        }
        function Ct(e2, t2, n2) {
          e2 = e2 instanceof Bt ? new e2.Collection(e2) : e2;
          return e2._ctx.error = new (n2 || TypeError)(t2), e2;
        }
        function Tt(e2) {
          return new e2.Collection(e2, function() {
            return Dt("");
          }).limit(0);
        }
        function It(e2, s2, n2, r2) {
          var i2, c2, l2, f2, h2, d2, p2, y2 = n2.length;
          if (!n2.every(function(e3) {
            return "string" == typeof e3;
          })) return Ct(e2, Ze);
          function t2(e3) {
            i2 = "next" === e3 ? function(e4) {
              return e4.toUpperCase();
            } : function(e4) {
              return e4.toLowerCase();
            }, c2 = "next" === e3 ? function(e4) {
              return e4.toLowerCase();
            } : function(e4) {
              return e4.toUpperCase();
            }, l2 = "next" === e3 ? jt : At;
            var t3 = n2.map(function(e4) {
              return { lower: c2(e4), upper: i2(e4) };
            }).sort(function(e4, t4) {
              return l2(e4.lower, t4.lower);
            });
            f2 = t3.map(function(e4) {
              return e4.upper;
            }), h2 = t3.map(function(e4) {
              return e4.lower;
            }), p2 = "next" === (d2 = e3) ? "" : r2;
          }
          t2("next");
          e2 = new e2.Collection(e2, function() {
            return qt(f2[0], h2[y2 - 1] + r2);
          });
          e2._ondirectionchange = function(e3) {
            t2(e3);
          };
          var v2 = 0;
          return e2._addAlgorithm(function(e3, t3, n3) {
            var r3 = e3.key;
            if ("string" != typeof r3) return false;
            var i3 = c2(r3);
            if (s2(i3, h2, v2)) return true;
            for (var o2 = null, a2 = v2; a2 < y2; ++a2) {
              var u2 = (function(e4, t4, n4, r4, i4, o3) {
                for (var a3 = Math.min(e4.length, r4.length), u3 = -1, s3 = 0; s3 < a3; ++s3) {
                  var c3 = t4[s3];
                  if (c3 !== r4[s3]) return i4(e4[s3], n4[s3]) < 0 ? e4.substr(0, s3) + n4[s3] + n4.substr(s3 + 1) : i4(e4[s3], r4[s3]) < 0 ? e4.substr(0, s3) + r4[s3] + n4.substr(s3 + 1) : 0 <= u3 ? e4.substr(0, u3) + t4[u3] + n4.substr(u3 + 1) : null;
                  i4(e4[s3], c3) < 0 && (u3 = s3);
                }
                return a3 < r4.length && "next" === o3 ? e4 + n4.substr(e4.length) : a3 < e4.length && "prev" === o3 ? e4.substr(0, n4.length) : u3 < 0 ? null : e4.substr(0, u3) + r4[u3] + n4.substr(u3 + 1);
              })(r3, i3, f2[a2], h2[a2], l2, d2);
              null === u2 && null === o2 ? v2 = a2 + 1 : (null === o2 || 0 < l2(o2, u2)) && (o2 = u2);
            }
            return t3(null !== o2 ? function() {
              e3.continue(o2 + p2);
            } : n3), false;
          }), e2;
        }
        function qt(e2, t2, n2, r2) {
          return { type: 2, lower: e2, upper: t2, lowerOpen: n2, upperOpen: r2 };
        }
        function Dt(e2) {
          return { type: 1, lower: e2, upper: e2 };
        }
        var Bt = (Object.defineProperty(Rt.prototype, "Collection", { get: function() {
          return this._ctx.table.db.Collection;
        }, enumerable: false, configurable: true }), Rt.prototype.between = function(e2, t2, n2, r2) {
          n2 = false !== n2, r2 = true === r2;
          try {
            return 0 < this._cmp(e2, t2) || 0 === this._cmp(e2, t2) && (n2 || r2) && (!n2 || !r2) ? Tt(this) : new this.Collection(this, function() {
              return qt(e2, t2, !n2, !r2);
            });
          } catch (e3) {
            return Ct(this, Je);
          }
        }, Rt.prototype.equals = function(e2) {
          return null == e2 ? Ct(this, Je) : new this.Collection(this, function() {
            return Dt(e2);
          });
        }, Rt.prototype.above = function(e2) {
          return null == e2 ? Ct(this, Je) : new this.Collection(this, function() {
            return qt(e2, void 0, true);
          });
        }, Rt.prototype.aboveOrEqual = function(e2) {
          return null == e2 ? Ct(this, Je) : new this.Collection(this, function() {
            return qt(e2, void 0, false);
          });
        }, Rt.prototype.below = function(e2) {
          return null == e2 ? Ct(this, Je) : new this.Collection(this, function() {
            return qt(void 0, e2, false, true);
          });
        }, Rt.prototype.belowOrEqual = function(e2) {
          return null == e2 ? Ct(this, Je) : new this.Collection(this, function() {
            return qt(void 0, e2);
          });
        }, Rt.prototype.startsWith = function(e2) {
          return "string" != typeof e2 ? Ct(this, Ze) : this.between(e2, e2 + He, true, true);
        }, Rt.prototype.startsWithIgnoreCase = function(e2) {
          return "" === e2 ? this.startsWith(e2) : It(this, function(e3, t2) {
            return 0 === e3.indexOf(t2[0]);
          }, [e2], He);
        }, Rt.prototype.equalsIgnoreCase = function(e2) {
          return It(this, function(e3, t2) {
            return e3 === t2[0];
          }, [e2], "");
        }, Rt.prototype.anyOfIgnoreCase = function() {
          var e2 = D.apply(q, arguments);
          return 0 === e2.length ? Tt(this) : It(this, function(e3, t2) {
            return -1 !== t2.indexOf(e3);
          }, e2, "");
        }, Rt.prototype.startsWithAnyOfIgnoreCase = function() {
          var e2 = D.apply(q, arguments);
          return 0 === e2.length ? Tt(this) : It(this, function(t2, e3) {
            return e3.some(function(e4) {
              return 0 === t2.indexOf(e4);
            });
          }, e2, He);
        }, Rt.prototype.anyOf = function() {
          var t2 = this, i2 = D.apply(q, arguments), o2 = this._cmp;
          try {
            i2.sort(o2);
          } catch (e3) {
            return Ct(this, Je);
          }
          if (0 === i2.length) return Tt(this);
          var e2 = new this.Collection(this, function() {
            return qt(i2[0], i2[i2.length - 1]);
          });
          e2._ondirectionchange = function(e3) {
            o2 = "next" === e3 ? t2._ascending : t2._descending, i2.sort(o2);
          };
          var a2 = 0;
          return e2._addAlgorithm(function(e3, t3, n2) {
            for (var r2 = e3.key; 0 < o2(r2, i2[a2]); ) if (++a2 === i2.length) return t3(n2), false;
            return 0 === o2(r2, i2[a2]) || (t3(function() {
              e3.continue(i2[a2]);
            }), false);
          }), e2;
        }, Rt.prototype.notEqual = function(e2) {
          return this.inAnyRange([[-1 / 0, e2], [e2, this.db._maxKey]], { includeLowers: false, includeUppers: false });
        }, Rt.prototype.noneOf = function() {
          var e2 = D.apply(q, arguments);
          if (0 === e2.length) return new this.Collection(this);
          try {
            e2.sort(this._ascending);
          } catch (e3) {
            return Ct(this, Je);
          }
          var t2 = e2.reduce(function(e3, t3) {
            return e3 ? e3.concat([[e3[e3.length - 1][1], t3]]) : [[-1 / 0, t3]];
          }, null);
          return t2.push([e2[e2.length - 1], this.db._maxKey]), this.inAnyRange(t2, { includeLowers: false, includeUppers: false });
        }, Rt.prototype.inAnyRange = function(e2, t2) {
          var o2 = this, a2 = this._cmp, u2 = this._ascending, n2 = this._descending, s2 = this._min, c2 = this._max;
          if (0 === e2.length) return Tt(this);
          if (!e2.every(function(e3) {
            return void 0 !== e3[0] && void 0 !== e3[1] && u2(e3[0], e3[1]) <= 0;
          })) return Ct(this, "First argument to inAnyRange() must be an Array of two-value Arrays [lower,upper] where upper must not be lower than lower", Y.InvalidArgument);
          var r2 = !t2 || false !== t2.includeLowers, i2 = t2 && true === t2.includeUppers;
          var l2, f2 = u2;
          function h2(e3, t3) {
            return f2(e3[0], t3[0]);
          }
          try {
            (l2 = e2.reduce(function(e3, t3) {
              for (var n3 = 0, r3 = e3.length; n3 < r3; ++n3) {
                var i3 = e3[n3];
                if (a2(t3[0], i3[1]) < 0 && 0 < a2(t3[1], i3[0])) {
                  i3[0] = s2(i3[0], t3[0]), i3[1] = c2(i3[1], t3[1]);
                  break;
                }
              }
              return n3 === r3 && e3.push(t3), e3;
            }, [])).sort(h2);
          } catch (e3) {
            return Ct(this, Je);
          }
          var d2 = 0, p2 = i2 ? function(e3) {
            return 0 < u2(e3, l2[d2][1]);
          } : function(e3) {
            return 0 <= u2(e3, l2[d2][1]);
          }, y2 = r2 ? function(e3) {
            return 0 < n2(e3, l2[d2][0]);
          } : function(e3) {
            return 0 <= n2(e3, l2[d2][0]);
          };
          var v2 = p2, e2 = new this.Collection(this, function() {
            return qt(l2[0][0], l2[l2.length - 1][1], !r2, !i2);
          });
          return e2._ondirectionchange = function(e3) {
            f2 = "next" === e3 ? (v2 = p2, u2) : (v2 = y2, n2), l2.sort(h2);
          }, e2._addAlgorithm(function(e3, t3, n3) {
            for (var r3, i3 = e3.key; v2(i3); ) if (++d2 === l2.length) return t3(n3), false;
            return !p2(r3 = i3) && !y2(r3) || (0 === o2._cmp(i3, l2[d2][1]) || 0 === o2._cmp(i3, l2[d2][0]) || t3(function() {
              f2 === u2 ? e3.continue(l2[d2][0]) : e3.continue(l2[d2][1]);
            }), false);
          }), e2;
        }, Rt.prototype.startsWithAnyOf = function() {
          var e2 = D.apply(q, arguments);
          return e2.every(function(e3) {
            return "string" == typeof e3;
          }) ? 0 === e2.length ? Tt(this) : this.inAnyRange(e2.map(function(e3) {
            return [e3, e3 + He];
          })) : Ct(this, "startsWithAnyOf() only works with strings");
        }, Rt);
        function Rt() {
        }
        function Ft(t2) {
          return Ie(function(e2) {
            return Mt(e2), t2(e2.target.error), false;
          });
        }
        function Mt(e2) {
          e2.stopPropagation && e2.stopPropagation(), e2.preventDefault && e2.preventDefault();
        }
        var Nt = "storagemutated", Lt = "x-storagemutated-1", Ut = mt(null, Nt), Vt = (zt.prototype._lock = function() {
          return y(!me.global), ++this._reculock, 1 !== this._reculock || me.global || (me.lockOwnerFor = this), this;
        }, zt.prototype._unlock = function() {
          if (y(!me.global), 0 == --this._reculock) for (me.global || (me.lockOwnerFor = null); 0 < this._blockedFuncs.length && !this._locked(); ) {
            var e2 = this._blockedFuncs.shift();
            try {
              $e(e2[1], e2[0]);
            } catch (e3) {
            }
          }
          return this;
        }, zt.prototype._locked = function() {
          return this._reculock && me.lockOwnerFor !== this;
        }, zt.prototype.create = function(t2) {
          var n2 = this;
          if (!this.mode) return this;
          var e2 = this.db.idbdb, r2 = this.db._state.dbOpenError;
          if (y(!this.idbtrans), !t2 && !e2) switch (r2 && r2.name) {
            case "DatabaseClosedError":
              throw new Y.DatabaseClosed(r2);
            case "MissingAPIError":
              throw new Y.MissingAPI(r2.message, r2);
            default:
              throw new Y.OpenFailed(r2);
          }
          if (!this.active) throw new Y.TransactionInactive();
          return y(null === this._completion._state), (t2 = this.idbtrans = t2 || (this.db.core || e2).transaction(this.storeNames, this.mode, { durability: this.chromeTransactionDurability })).onerror = Ie(function(e3) {
            Mt(e3), n2._reject(t2.error);
          }), t2.onabort = Ie(function(e3) {
            Mt(e3), n2.active && n2._reject(new Y.Abort(t2.error)), n2.active = false, n2.on("abort").fire(e3);
          }), t2.oncomplete = Ie(function() {
            n2.active = false, n2._resolve(), "mutatedParts" in t2 && Ut.storagemutated.fire(t2.mutatedParts);
          }), this;
        }, zt.prototype._promise = function(n2, r2, i2) {
          var o2 = this;
          if ("readwrite" === n2 && "readwrite" !== this.mode) return Xe(new Y.ReadOnly("Transaction is readonly"));
          if (!this.active) return Xe(new Y.TransactionInactive());
          if (this._locked()) return new _e(function(e3, t2) {
            o2._blockedFuncs.push([function() {
              o2._promise(n2, r2, i2).then(e3, t2);
            }, me]);
          });
          if (i2) return Ne(function() {
            var e3 = new _e(function(e4, t2) {
              o2._lock();
              var n3 = r2(e4, t2, o2);
              n3 && n3.then && n3.then(e4, t2);
            });
            return e3.finally(function() {
              return o2._unlock();
            }), e3._lib = true, e3;
          });
          var e2 = new _e(function(e3, t2) {
            var n3 = r2(e3, t2, o2);
            n3 && n3.then && n3.then(e3, t2);
          });
          return e2._lib = true, e2;
        }, zt.prototype._root = function() {
          return this.parent ? this.parent._root() : this;
        }, zt.prototype.waitFor = function(e2) {
          var t2, r2 = this._root(), i2 = _e.resolve(e2);
          r2._waitingFor ? r2._waitingFor = r2._waitingFor.then(function() {
            return i2;
          }) : (r2._waitingFor = i2, r2._waitingQueue = [], t2 = r2.idbtrans.objectStore(r2.storeNames[0]), (function e3() {
            for (++r2._spinCount; r2._waitingQueue.length; ) r2._waitingQueue.shift()();
            r2._waitingFor && (t2.get(-1 / 0).onsuccess = e3);
          })());
          var o2 = r2._waitingFor;
          return new _e(function(t3, n2) {
            i2.then(function(e3) {
              return r2._waitingQueue.push(Ie(t3.bind(null, e3)));
            }, function(e3) {
              return r2._waitingQueue.push(Ie(n2.bind(null, e3)));
            }).finally(function() {
              r2._waitingFor === o2 && (r2._waitingFor = null);
            });
          });
        }, zt.prototype.abort = function() {
          this.active && (this.active = false, this.idbtrans && this.idbtrans.abort(), this._reject(new Y.Abort()));
        }, zt.prototype.table = function(e2) {
          var t2 = this._memoizedTables || (this._memoizedTables = {});
          if (m(t2, e2)) return t2[e2];
          var n2 = this.schema[e2];
          if (!n2) throw new Y.NotFound("Table " + e2 + " not part of transaction");
          n2 = new this.db.Table(e2, n2, this);
          return n2.core = this.db.core.table(e2), t2[e2] = n2;
        }, zt);
        function zt() {
        }
        function Wt(e2, t2, n2, r2, i2, o2, a2, u2) {
          return { name: e2, keyPath: t2, unique: n2, multi: r2, auto: i2, compound: o2, src: (n2 && !a2 ? "&" : "") + (r2 ? "*" : "") + (i2 ? "++" : "") + Yt(t2), type: u2 };
        }
        function Yt(e2) {
          return "string" == typeof e2 ? e2 : e2 ? "[" + [].join.call(e2, "+") + "]" : "";
        }
        function $t(e2, t2, n2) {
          return { name: e2, primKey: t2, indexes: n2, mappedClass: null, idxByName: (r2 = function(e3) {
            return [e3.name, e3];
          }, n2.reduce(function(e3, t3, n3) {
            n3 = r2(t3, n3);
            return n3 && (e3[n3[0]] = n3[1]), e3;
          }, {})) };
          var r2;
        }
        var Qt = function(e2) {
          try {
            return e2.only([[]]), Qt = function() {
              return [[]];
            }, [[]];
          } catch (e3) {
            return Qt = function() {
              return He;
            }, He;
          }
        };
        function Gt(t2) {
          return null == t2 ? function() {
          } : "string" == typeof t2 ? 1 === (n2 = t2).split(".").length ? function(e2) {
            return e2[n2];
          } : function(e2) {
            return g(e2, n2);
          } : function(e2) {
            return g(e2, t2);
          };
          var n2;
        }
        function Xt(e2) {
          return [].slice.call(e2);
        }
        var Ht = 0;
        function Jt(e2) {
          return null == e2 ? ":id" : "string" == typeof e2 ? e2 : "[".concat(e2.join("+"), "]");
        }
        function Zt(e2, i2, t2) {
          function _2(e3) {
            if (3 === e3.type) return null;
            if (4 === e3.type) throw new Error("Cannot convert never type to IDBKeyRange");
            var t3 = e3.lower, n3 = e3.upper, r3 = e3.lowerOpen, e3 = e3.upperOpen;
            return void 0 === t3 ? void 0 === n3 ? null : i2.upperBound(n3, !!e3) : void 0 === n3 ? i2.lowerBound(t3, !!r3) : i2.bound(t3, n3, !!r3, !!e3);
          }
          function n2(e3) {
            var h2, w2 = e3.name;
            return { name: w2, schema: e3, mutate: function(e4) {
              var y2 = e4.trans, v2 = e4.type, m2 = e4.keys, b2 = e4.values, g2 = e4.range;
              return new Promise(function(t3, e5) {
                t3 = Ie(t3);
                var n3 = y2.objectStore(w2), r3 = null == n3.keyPath, i3 = "put" === v2 || "add" === v2;
                if (!i3 && "delete" !== v2 && "deleteRange" !== v2) throw new Error("Invalid operation type: " + v2);
                var o3, a3 = (m2 || b2 || { length: 1 }).length;
                if (m2 && b2 && m2.length !== b2.length) throw new Error("Given keys array must have same length as given values array.");
                if (0 === a3) return t3({ numFailures: 0, failures: {}, results: [], lastResult: void 0 });
                function u3(e6) {
                  ++l2, Mt(e6);
                }
                var s3 = [], c3 = [], l2 = 0;
                if ("deleteRange" === v2) {
                  if (4 === g2.type) return t3({ numFailures: l2, failures: c3, results: [], lastResult: void 0 });
                  3 === g2.type ? s3.push(o3 = n3.clear()) : s3.push(o3 = n3.delete(_2(g2)));
                } else {
                  var r3 = i3 ? r3 ? [b2, m2] : [b2, null] : [m2, null], f2 = r3[0], h3 = r3[1];
                  if (i3) for (var d2 = 0; d2 < a3; ++d2) s3.push(o3 = h3 && void 0 !== h3[d2] ? n3[v2](f2[d2], h3[d2]) : n3[v2](f2[d2])), o3.onerror = u3;
                  else for (d2 = 0; d2 < a3; ++d2) s3.push(o3 = n3[v2](f2[d2])), o3.onerror = u3;
                }
                function p2(e6) {
                  e6 = e6.target.result, s3.forEach(function(e7, t4) {
                    return null != e7.error && (c3[t4] = e7.error);
                  }), t3({ numFailures: l2, failures: c3, results: "delete" === v2 ? m2 : s3.map(function(e7) {
                    return e7.result;
                  }), lastResult: e6 });
                }
                o3.onerror = function(e6) {
                  u3(e6), p2(e6);
                }, o3.onsuccess = p2;
              });
            }, getMany: function(e4) {
              var f2 = e4.trans, h3 = e4.keys;
              return new Promise(function(t3, e5) {
                t3 = Ie(t3);
                for (var n3, r3 = f2.objectStore(w2), i3 = h3.length, o3 = new Array(i3), a3 = 0, u3 = 0, s3 = function(e6) {
                  e6 = e6.target;
                  o3[e6._pos] = e6.result, ++u3 === a3 && t3(o3);
                }, c3 = Ft(e5), l2 = 0; l2 < i3; ++l2) null != h3[l2] && ((n3 = r3.get(h3[l2]))._pos = l2, n3.onsuccess = s3, n3.onerror = c3, ++a3);
                0 === a3 && t3(o3);
              });
            }, get: function(e4) {
              var r3 = e4.trans, i3 = e4.key;
              return new Promise(function(t3, e5) {
                t3 = Ie(t3);
                var n3 = r3.objectStore(w2).get(i3);
                n3.onsuccess = function(e6) {
                  return t3(e6.target.result);
                }, n3.onerror = Ft(e5);
              });
            }, query: (h2 = s2, function(f2) {
              return new Promise(function(n3, e4) {
                n3 = Ie(n3);
                var r3, i3, o3, t3 = f2.trans, a3 = f2.values, u3 = f2.limit, s3 = f2.query, c3 = u3 === 1 / 0 ? void 0 : u3, l2 = s3.index, s3 = s3.range, t3 = t3.objectStore(w2), l2 = l2.isPrimaryKey ? t3 : t3.index(l2.name), s3 = _2(s3);
                if (0 === u3) return n3({ result: [] });
                h2 ? ((c3 = a3 ? l2.getAll(s3, c3) : l2.getAllKeys(s3, c3)).onsuccess = function(e5) {
                  return n3({ result: e5.target.result });
                }, c3.onerror = Ft(e4)) : (r3 = 0, i3 = !a3 && "openKeyCursor" in l2 ? l2.openKeyCursor(s3) : l2.openCursor(s3), o3 = [], i3.onsuccess = function(e5) {
                  var t4 = i3.result;
                  return t4 ? (o3.push(a3 ? t4.value : t4.primaryKey), ++r3 === u3 ? n3({ result: o3 }) : void t4.continue()) : n3({ result: o3 });
                }, i3.onerror = Ft(e4));
              });
            }), openCursor: function(e4) {
              var c3 = e4.trans, o3 = e4.values, a3 = e4.query, u3 = e4.reverse, l2 = e4.unique;
              return new Promise(function(t3, n3) {
                t3 = Ie(t3);
                var e5 = a3.index, r3 = a3.range, i3 = c3.objectStore(w2), i3 = e5.isPrimaryKey ? i3 : i3.index(e5.name), e5 = u3 ? l2 ? "prevunique" : "prev" : l2 ? "nextunique" : "next", s3 = !o3 && "openKeyCursor" in i3 ? i3.openKeyCursor(_2(r3), e5) : i3.openCursor(_2(r3), e5);
                s3.onerror = Ft(n3), s3.onsuccess = Ie(function(e6) {
                  var r4, i4, o4, a4, u4 = s3.result;
                  u4 ? (u4.___id = ++Ht, u4.done = false, r4 = u4.continue.bind(u4), i4 = (i4 = u4.continuePrimaryKey) && i4.bind(u4), o4 = u4.advance.bind(u4), a4 = function() {
                    throw new Error("Cursor not stopped");
                  }, u4.trans = c3, u4.stop = u4.continue = u4.continuePrimaryKey = u4.advance = function() {
                    throw new Error("Cursor not started");
                  }, u4.fail = Ie(n3), u4.next = function() {
                    var e7 = this, t4 = 1;
                    return this.start(function() {
                      return t4-- ? e7.continue() : e7.stop();
                    }).then(function() {
                      return e7;
                    });
                  }, u4.start = function(e7) {
                    function t4() {
                      if (s3.result) try {
                        e7();
                      } catch (e8) {
                        u4.fail(e8);
                      }
                      else u4.done = true, u4.start = function() {
                        throw new Error("Cursor behind last entry");
                      }, u4.stop();
                    }
                    var n4 = new Promise(function(t5, e8) {
                      t5 = Ie(t5), s3.onerror = Ft(e8), u4.fail = e8, u4.stop = function(e9) {
                        u4.stop = u4.continue = u4.continuePrimaryKey = u4.advance = a4, t5(e9);
                      };
                    });
                    return s3.onsuccess = Ie(function(e8) {
                      s3.onsuccess = t4, t4();
                    }), u4.continue = r4, u4.continuePrimaryKey = i4, u4.advance = o4, t4(), n4;
                  }, t3(u4)) : t3(null);
                }, n3);
              });
            }, count: function(e4) {
              var t3 = e4.query, i3 = e4.trans, o3 = t3.index, a3 = t3.range;
              return new Promise(function(t4, e5) {
                var n3 = i3.objectStore(w2), r3 = o3.isPrimaryKey ? n3 : n3.index(o3.name), n3 = _2(a3), r3 = n3 ? r3.count(n3) : r3.count();
                r3.onsuccess = Ie(function(e6) {
                  return t4(e6.target.result);
                }), r3.onerror = Ft(e5);
              });
            } };
          }
          var r2, o2, a2, u2 = (o2 = t2, a2 = Xt((r2 = e2).objectStoreNames), { schema: { name: r2.name, tables: a2.map(function(e3) {
            return o2.objectStore(e3);
          }).map(function(t3) {
            var e3 = t3.keyPath, n3 = t3.autoIncrement, r3 = x(e3), i3 = {}, n3 = { name: t3.name, primaryKey: { name: null, isPrimaryKey: true, outbound: null == e3, compound: r3, keyPath: e3, autoIncrement: n3, unique: true, extractKey: Gt(e3) }, indexes: Xt(t3.indexNames).map(function(e4) {
              return t3.index(e4);
            }).map(function(e4) {
              var t4 = e4.name, n4 = e4.unique, r4 = e4.multiEntry, e4 = e4.keyPath, r4 = { name: t4, compound: x(e4), keyPath: e4, unique: n4, multiEntry: r4, extractKey: Gt(e4) };
              return i3[Jt(e4)] = r4;
            }), getIndexByKeyPath: function(e4) {
              return i3[Jt(e4)];
            } };
            return i3[":id"] = n3.primaryKey, null != e3 && (i3[Jt(e3)] = n3.primaryKey), n3;
          }) }, hasGetAll: 0 < a2.length && "getAll" in o2.objectStore(a2[0]) && !("undefined" != typeof navigator && /Safari/.test(navigator.userAgent) && !/(Chrome\/|Edge\/)/.test(navigator.userAgent) && [].concat(navigator.userAgent.match(/Safari\/(\d*)/))[1] < 604) }), t2 = u2.schema, s2 = u2.hasGetAll, u2 = t2.tables.map(n2), c2 = {};
          return u2.forEach(function(e3) {
            return c2[e3.name] = e3;
          }), { stack: "dbcore", transaction: e2.transaction.bind(e2), table: function(e3) {
            if (!c2[e3]) throw new Error("Table '".concat(e3, "' not found"));
            return c2[e3];
          }, MIN_KEY: -1 / 0, MAX_KEY: Qt(i2), schema: t2 };
        }
        function en(e2, t2, n2, r2) {
          var i2 = n2.IDBKeyRange;
          return n2.indexedDB, { dbcore: (r2 = Zt(t2, i2, r2), e2.dbcore.reduce(function(e3, t3) {
            t3 = t3.create;
            return _(_({}, e3), t3(e3));
          }, r2)) };
        }
        function tn(n2, e2) {
          var t2 = e2.db, e2 = en(n2._middlewares, t2, n2._deps, e2);
          n2.core = e2.dbcore, n2.tables.forEach(function(e3) {
            var t3 = e3.name;
            n2.core.schema.tables.some(function(e4) {
              return e4.name === t3;
            }) && (e3.core = n2.core.table(t3), n2[t3] instanceof n2.Table && (n2[t3].core = e3.core));
          });
        }
        function nn(i2, e2, t2, o2) {
          t2.forEach(function(n2) {
            var r2 = o2[n2];
            e2.forEach(function(e3) {
              var t3 = (function e4(t4, n3) {
                return h(t4, n3) || (t4 = c(t4)) && e4(t4, n3);
              })(e3, n2);
              (!t3 || "value" in t3 && void 0 === t3.value) && (e3 === i2.Transaction.prototype || e3 instanceof i2.Transaction ? l(e3, n2, { get: function() {
                return this.table(n2);
              }, set: function(e4) {
                u(this, n2, { value: e4, writable: true, configurable: true, enumerable: true });
              } }) : e3[n2] = new i2.Table(n2, r2));
            });
          });
        }
        function rn(n2, e2) {
          e2.forEach(function(e3) {
            for (var t2 in e3) e3[t2] instanceof n2.Table && delete e3[t2];
          });
        }
        function on(e2, t2) {
          return e2._cfg.version - t2._cfg.version;
        }
        function an(n2, r2, i2, e2) {
          var o2 = n2._dbSchema;
          i2.objectStoreNames.contains("$meta") && !o2.$meta && (o2.$meta = $t("$meta", pn("")[0], []), n2._storeNames.push("$meta"));
          var a2 = n2._createTransaction("readwrite", n2._storeNames, o2);
          a2.create(i2), a2._completion.catch(e2);
          var u2 = a2._reject.bind(a2), s2 = me.transless || me;
          Ne(function() {
            return me.trans = a2, me.transless = s2, 0 !== r2 ? (tn(n2, i2), t2 = r2, ((e3 = a2).storeNames.includes("$meta") ? e3.table("$meta").get("version").then(function(e4) {
              return null != e4 ? e4 : t2;
            }) : _e.resolve(t2)).then(function(e4) {
              return c2 = e4, l2 = a2, f2 = i2, t3 = [], e4 = (s3 = n2)._versions, h2 = s3._dbSchema = hn(0, s3.idbdb, f2), 0 !== (e4 = e4.filter(function(e5) {
                return e5._cfg.version >= c2;
              })).length ? (e4.forEach(function(u3) {
                t3.push(function() {
                  var t4 = h2, e5 = u3._cfg.dbschema;
                  dn(s3, t4, f2), dn(s3, e5, f2), h2 = s3._dbSchema = e5;
                  var n3 = sn(t4, e5);
                  n3.add.forEach(function(e6) {
                    cn(f2, e6[0], e6[1].primKey, e6[1].indexes);
                  }), n3.change.forEach(function(e6) {
                    if (e6.recreate) throw new Y.Upgrade("Not yet support for changing primary key");
                    var t5 = f2.objectStore(e6.name);
                    e6.add.forEach(function(e7) {
                      return fn(t5, e7);
                    }), e6.change.forEach(function(e7) {
                      t5.deleteIndex(e7.name), fn(t5, e7);
                    }), e6.del.forEach(function(e7) {
                      return t5.deleteIndex(e7);
                    });
                  });
                  var r3 = u3._cfg.contentUpgrade;
                  if (r3 && u3._cfg.version > c2) {
                    tn(s3, f2), l2._memoizedTables = {};
                    var i3 = k(e5);
                    n3.del.forEach(function(e6) {
                      i3[e6] = t4[e6];
                    }), rn(s3, [s3.Transaction.prototype]), nn(s3, [s3.Transaction.prototype], O(i3), i3), l2.schema = i3;
                    var o3, a3 = B(r3);
                    a3 && Le();
                    n3 = _e.follow(function() {
                      var e6;
                      (o3 = r3(l2)) && a3 && (e6 = Ue.bind(null, null), o3.then(e6, e6));
                    });
                    return o3 && "function" == typeof o3.then ? _e.resolve(o3) : n3.then(function() {
                      return o3;
                    });
                  }
                }), t3.push(function(e5) {
                  var t4, n3, r3 = u3._cfg.dbschema;
                  t4 = r3, n3 = e5, [].slice.call(n3.db.objectStoreNames).forEach(function(e6) {
                    return null == t4[e6] && n3.db.deleteObjectStore(e6);
                  }), rn(s3, [s3.Transaction.prototype]), nn(s3, [s3.Transaction.prototype], s3._storeNames, s3._dbSchema), l2.schema = s3._dbSchema;
                }), t3.push(function(e5) {
                  s3.idbdb.objectStoreNames.contains("$meta") && (Math.ceil(s3.idbdb.version / 10) === u3._cfg.version ? (s3.idbdb.deleteObjectStore("$meta"), delete s3._dbSchema.$meta, s3._storeNames = s3._storeNames.filter(function(e6) {
                    return "$meta" !== e6;
                  })) : e5.objectStore("$meta").put(u3._cfg.version, "version"));
                });
              }), (function e5() {
                return t3.length ? _e.resolve(t3.shift()(l2.idbtrans)).then(e5) : _e.resolve();
              })().then(function() {
                ln(h2, f2);
              })) : _e.resolve();
              var s3, c2, l2, f2, t3, h2;
            }).catch(u2)) : (O(o2).forEach(function(e4) {
              cn(i2, e4, o2[e4].primKey, o2[e4].indexes);
            }), tn(n2, i2), void _e.follow(function() {
              return n2.on.populate.fire(a2);
            }).catch(u2));
            var e3, t2;
          });
        }
        function un(e2, r2) {
          ln(e2._dbSchema, r2), r2.db.version % 10 != 0 || r2.objectStoreNames.contains("$meta") || r2.db.createObjectStore("$meta").add(Math.ceil(r2.db.version / 10 - 1), "version");
          var t2 = hn(0, e2.idbdb, r2);
          dn(e2, e2._dbSchema, r2);
          for (var n2 = 0, i2 = sn(t2, e2._dbSchema).change; n2 < i2.length; n2++) {
            var o2 = (function(t3) {
              if (t3.change.length || t3.recreate) return console.warn("Unable to patch indexes of table ".concat(t3.name, " because it has changes on the type of index or primary key.")), { value: void 0 };
              var n3 = r2.objectStore(t3.name);
              t3.add.forEach(function(e3) {
                ie && console.debug("Dexie upgrade patch: Creating missing index ".concat(t3.name, ".").concat(e3.src)), fn(n3, e3);
              });
            })(i2[n2]);
            if ("object" == typeof o2) return o2.value;
          }
        }
        function sn(e2, t2) {
          var n2, r2 = { del: [], add: [], change: [] };
          for (n2 in e2) t2[n2] || r2.del.push(n2);
          for (n2 in t2) {
            var i2 = e2[n2], o2 = t2[n2];
            if (i2) {
              var a2 = { name: n2, def: o2, recreate: false, del: [], add: [], change: [] };
              if ("" + (i2.primKey.keyPath || "") != "" + (o2.primKey.keyPath || "") || i2.primKey.auto !== o2.primKey.auto) a2.recreate = true, r2.change.push(a2);
              else {
                var u2 = i2.idxByName, s2 = o2.idxByName, c2 = void 0;
                for (c2 in u2) s2[c2] || a2.del.push(c2);
                for (c2 in s2) {
                  var l2 = u2[c2], f2 = s2[c2];
                  l2 ? l2.src !== f2.src && a2.change.push(f2) : a2.add.push(f2);
                }
                (0 < a2.del.length || 0 < a2.add.length || 0 < a2.change.length) && r2.change.push(a2);
              }
            } else r2.add.push([n2, o2]);
          }
          return r2;
        }
        function cn(e2, t2, n2, r2) {
          var i2 = e2.db.createObjectStore(t2, n2.keyPath ? { keyPath: n2.keyPath, autoIncrement: n2.auto } : { autoIncrement: n2.auto });
          return r2.forEach(function(e3) {
            return fn(i2, e3);
          }), i2;
        }
        function ln(t2, n2) {
          O(t2).forEach(function(e2) {
            n2.db.objectStoreNames.contains(e2) || (ie && console.debug("Dexie: Creating missing table", e2), cn(n2, e2, t2[e2].primKey, t2[e2].indexes));
          });
        }
        function fn(e2, t2) {
          e2.createIndex(t2.name, t2.keyPath, { unique: t2.unique, multiEntry: t2.multi });
        }
        function hn(e2, t2, u2) {
          var s2 = {};
          return b(t2.objectStoreNames, 0).forEach(function(e3) {
            for (var t3 = u2.objectStore(e3), n2 = Wt(Yt(a2 = t3.keyPath), a2 || "", true, false, !!t3.autoIncrement, a2 && "string" != typeof a2, true), r2 = [], i2 = 0; i2 < t3.indexNames.length; ++i2) {
              var o2 = t3.index(t3.indexNames[i2]), a2 = o2.keyPath, o2 = Wt(o2.name, a2, !!o2.unique, !!o2.multiEntry, false, a2 && "string" != typeof a2, false);
              r2.push(o2);
            }
            s2[e3] = $t(e3, n2, r2);
          }), s2;
        }
        function dn(e2, t2, n2) {
          for (var r2 = n2.db.objectStoreNames, i2 = 0; i2 < r2.length; ++i2) {
            var o2 = r2[i2], a2 = n2.objectStore(o2);
            e2._hasGetAll = "getAll" in a2;
            for (var u2 = 0; u2 < a2.indexNames.length; ++u2) {
              var s2 = a2.indexNames[u2], c2 = a2.index(s2).keyPath, l2 = "string" == typeof c2 ? c2 : "[" + b(c2).join("+") + "]";
              !t2[o2] || (c2 = t2[o2].idxByName[l2]) && (c2.name = s2, delete t2[o2].idxByName[l2], t2[o2].idxByName[s2] = c2);
            }
          }
          "undefined" != typeof navigator && /Safari/.test(navigator.userAgent) && !/(Chrome\/|Edge\/)/.test(navigator.userAgent) && f.WorkerGlobalScope && f instanceof f.WorkerGlobalScope && [].concat(navigator.userAgent.match(/Safari\/(\d*)/))[1] < 604 && (e2._hasGetAll = false);
        }
        function pn(e2) {
          return e2.split(",").map(function(e3, t2) {
            var n2 = e3.split(":"), r2 = null === (i2 = n2[1]) || void 0 === i2 ? void 0 : i2.trim(), i2 = (e3 = n2[0].trim()).replace(/([&*]|\+\+)/g, ""), n2 = /^\[/.test(i2) ? i2.match(/^\[(.*)\]$/)[1].split("+") : i2;
            return Wt(i2, n2 || null, /\&/.test(e3), /\*/.test(e3), /\+\+/.test(e3), x(n2), 0 === t2, r2);
          });
        }
        var yn = (vn.prototype._createTableSchema = $t, vn.prototype._parseIndexSyntax = pn, vn.prototype._parseStoresSpec = function(r2, i2) {
          var o2 = this;
          O(r2).forEach(function(e2) {
            if (null !== r2[e2]) {
              var t2 = o2._parseIndexSyntax(r2[e2]), n2 = t2.shift();
              if (!n2) throw new Y.Schema("Invalid schema for table " + e2 + ": " + r2[e2]);
              if (n2.unique = true, n2.multi) throw new Y.Schema("Primary key cannot be multiEntry*");
              t2.forEach(function(e3) {
                if (e3.auto) throw new Y.Schema("Only primary key can be marked as autoIncrement (++)");
                if (!e3.keyPath) throw new Y.Schema("Index must have a name and cannot be an empty string");
              });
              t2 = o2._createTableSchema(e2, n2, t2);
              i2[e2] = t2;
            }
          });
        }, vn.prototype.stores = function(e2) {
          var t2 = this.db;
          this._cfg.storesSource = this._cfg.storesSource ? a(this._cfg.storesSource, e2) : e2;
          var e2 = t2._versions, n2 = {}, r2 = {};
          return e2.forEach(function(e3) {
            a(n2, e3._cfg.storesSource), r2 = e3._cfg.dbschema = {}, e3._parseStoresSpec(n2, r2);
          }), t2._dbSchema = r2, rn(t2, [t2._allTables, t2, t2.Transaction.prototype]), nn(t2, [t2._allTables, t2, t2.Transaction.prototype, this._cfg.tables], O(r2), r2), t2._storeNames = O(r2), this;
        }, vn.prototype.upgrade = function(e2) {
          return this._cfg.contentUpgrade = re(this._cfg.contentUpgrade || G, e2), this;
        }, vn);
        function vn() {
        }
        function mn(e2, t2) {
          var n2 = e2._dbNamesDB;
          return n2 || (n2 = e2._dbNamesDB = new nr(tt, { addons: [], indexedDB: e2, IDBKeyRange: t2 })).version(1).stores({ dbnames: "name" }), n2.table("dbnames");
        }
        function bn(e2) {
          return e2 && "function" == typeof e2.databases;
        }
        function gn(e2) {
          return Ne(function() {
            return me.letThrough = true, e2();
          });
        }
        function wn(e2) {
          return !("from" in e2);
        }
        var _n = function(e2, t2) {
          if (!this) {
            var n2 = new _n();
            return e2 && "d" in e2 && a(n2, e2), n2;
          }
          a(this, arguments.length ? { d: 1, from: e2, to: 1 < arguments.length ? t2 : e2 } : { d: 0 });
        };
        function xn(e2, t2, n2) {
          var r2 = st(t2, n2);
          if (!isNaN(r2)) {
            if (0 < r2) throw RangeError();
            if (wn(e2)) return a(e2, { from: t2, to: n2, d: 1 });
            var i2 = e2.l, r2 = e2.r;
            if (st(n2, e2.from) < 0) return i2 ? xn(i2, t2, n2) : e2.l = { from: t2, to: n2, d: 1, l: null, r: null }, Kn(e2);
            if (0 < st(t2, e2.to)) return r2 ? xn(r2, t2, n2) : e2.r = { from: t2, to: n2, d: 1, l: null, r: null }, Kn(e2);
            st(t2, e2.from) < 0 && (e2.from = t2, e2.l = null, e2.d = r2 ? r2.d + 1 : 1), 0 < st(n2, e2.to) && (e2.to = n2, e2.r = null, e2.d = e2.l ? e2.l.d + 1 : 1);
            n2 = !e2.r;
            i2 && !e2.l && kn(e2, i2), r2 && n2 && kn(e2, r2);
          }
        }
        function kn(e2, t2) {
          wn(t2) || (function e3(t3, n2) {
            var r2 = n2.from, i2 = n2.to, o2 = n2.l, n2 = n2.r;
            xn(t3, r2, i2), o2 && e3(t3, o2), n2 && e3(t3, n2);
          })(e2, t2);
        }
        function On(e2, t2) {
          var n2 = Pn(t2), r2 = n2.next();
          if (r2.done) return false;
          for (var i2 = r2.value, o2 = Pn(e2), a2 = o2.next(i2.from), u2 = a2.value; !r2.done && !a2.done; ) {
            if (st(u2.from, i2.to) <= 0 && 0 <= st(u2.to, i2.from)) return true;
            st(i2.from, u2.from) < 0 ? i2 = (r2 = n2.next(u2.from)).value : u2 = (a2 = o2.next(i2.from)).value;
          }
          return false;
        }
        function Pn(e2) {
          var n2 = wn(e2) ? null : { s: 0, n: e2 };
          return { next: function(e3) {
            for (var t2 = 0 < arguments.length; n2; ) switch (n2.s) {
              case 0:
                if (n2.s = 1, t2) for (; n2.n.l && st(e3, n2.n.from) < 0; ) n2 = { up: n2, n: n2.n.l, s: 1 };
                else for (; n2.n.l; ) n2 = { up: n2, n: n2.n.l, s: 1 };
              case 1:
                if (n2.s = 2, !t2 || st(e3, n2.n.to) <= 0) return { value: n2.n, done: false };
              case 2:
                if (n2.n.r) {
                  n2.s = 3, n2 = { up: n2, n: n2.n.r, s: 0 };
                  continue;
                }
              case 3:
                n2 = n2.up;
            }
            return { done: true };
          } };
        }
        function Kn(e2) {
          var t2, n2, r2 = ((null === (t2 = e2.r) || void 0 === t2 ? void 0 : t2.d) || 0) - ((null === (n2 = e2.l) || void 0 === n2 ? void 0 : n2.d) || 0), i2 = 1 < r2 ? "r" : r2 < -1 ? "l" : "";
          i2 && (t2 = "r" == i2 ? "l" : "r", n2 = _({}, e2), r2 = e2[i2], e2.from = r2.from, e2.to = r2.to, e2[i2] = r2[i2], n2[i2] = r2[t2], (e2[t2] = n2).d = En(n2)), e2.d = En(e2);
        }
        function En(e2) {
          var t2 = e2.r, e2 = e2.l;
          return (t2 ? e2 ? Math.max(t2.d, e2.d) : t2.d : e2 ? e2.d : 0) + 1;
        }
        function Sn(t2, n2) {
          return O(n2).forEach(function(e2) {
            t2[e2] ? kn(t2[e2], n2[e2]) : t2[e2] = (function e3(t3) {
              var n3, r2, i2 = {};
              for (n3 in t3) m(t3, n3) && (r2 = t3[n3], i2[n3] = !r2 || "object" != typeof r2 || K.has(r2.constructor) ? r2 : e3(r2));
              return i2;
            })(n2[e2]);
          }), t2;
        }
        function jn(t2, n2) {
          return t2.all || n2.all || Object.keys(t2).some(function(e2) {
            return n2[e2] && On(n2[e2], t2[e2]);
          });
        }
        r(_n.prototype, ((F = { add: function(e2) {
          return kn(this, e2), this;
        }, addKey: function(e2) {
          return xn(this, e2, e2), this;
        }, addKeys: function(e2) {
          var t2 = this;
          return e2.forEach(function(e3) {
            return xn(t2, e3, e3);
          }), this;
        }, hasKey: function(e2) {
          var t2 = Pn(this).next(e2).value;
          return t2 && st(t2.from, e2) <= 0 && 0 <= st(t2.to, e2);
        } })[C] = function() {
          return Pn(this);
        }, F));
        var An = {}, Cn = {}, Tn = false;
        function In(e2) {
          Sn(Cn, e2), Tn || (Tn = true, setTimeout(function() {
            Tn = false, qn(Cn, !(Cn = {}));
          }, 0));
        }
        function qn(e2, t2) {
          void 0 === t2 && (t2 = false);
          var n2 = /* @__PURE__ */ new Set();
          if (e2.all) for (var r2 = 0, i2 = Object.values(An); r2 < i2.length; r2++) Dn(a2 = i2[r2], e2, n2, t2);
          else for (var o2 in e2) {
            var a2, u2 = /^idb\:\/\/(.*)\/(.*)\//.exec(o2);
            u2 && (o2 = u2[1], u2 = u2[2], (a2 = An["idb://".concat(o2, "/").concat(u2)]) && Dn(a2, e2, n2, t2));
          }
          n2.forEach(function(e3) {
            return e3();
          });
        }
        function Dn(e2, t2, n2, r2) {
          for (var i2 = [], o2 = 0, a2 = Object.entries(e2.queries.query); o2 < a2.length; o2++) {
            for (var u2 = a2[o2], s2 = u2[0], c2 = [], l2 = 0, f2 = u2[1]; l2 < f2.length; l2++) {
              var h2 = f2[l2];
              jn(t2, h2.obsSet) ? h2.subscribers.forEach(function(e3) {
                return n2.add(e3);
              }) : r2 && c2.push(h2);
            }
            r2 && i2.push([s2, c2]);
          }
          if (r2) for (var d2 = 0, p2 = i2; d2 < p2.length; d2++) {
            var y2 = p2[d2], s2 = y2[0], c2 = y2[1];
            e2.queries.query[s2] = c2;
          }
        }
        function Bn(f2) {
          var h2 = f2._state, r2 = f2._deps.indexedDB;
          if (h2.isBeingOpened || f2.idbdb) return h2.dbReadyPromise.then(function() {
            return h2.dbOpenError ? Xe(h2.dbOpenError) : f2;
          });
          h2.isBeingOpened = true, h2.dbOpenError = null, h2.openComplete = false;
          var t2 = h2.openCanceller, d2 = Math.round(10 * f2.verno), p2 = false;
          function e2() {
            if (h2.openCanceller !== t2) throw new Y.DatabaseClosed("db.open() was cancelled");
          }
          function y2() {
            return new _e(function(s2, n3) {
              if (e2(), !r2) throw new Y.MissingAPI();
              var c2 = f2.name, l2 = h2.autoSchema || !d2 ? r2.open(c2) : r2.open(c2, d2);
              if (!l2) throw new Y.MissingAPI();
              l2.onerror = Ft(n3), l2.onblocked = Ie(f2._fireOnBlocked), l2.onupgradeneeded = Ie(function(e3) {
                var t3;
                v2 = l2.transaction, h2.autoSchema && !f2._options.allowEmptyDB ? (l2.onerror = Mt, v2.abort(), l2.result.close(), (t3 = r2.deleteDatabase(c2)).onsuccess = t3.onerror = Ie(function() {
                  n3(new Y.NoSuchDatabase("Database ".concat(c2, " doesnt exist")));
                })) : (v2.onerror = Ft(n3), e3 = e3.oldVersion > Math.pow(2, 62) ? 0 : e3.oldVersion, m2 = e3 < 1, f2.idbdb = l2.result, p2 && un(f2, v2), an(f2, e3 / 10, v2, n3));
              }, n3), l2.onsuccess = Ie(function() {
                v2 = null;
                var e3, t3, n4, r3, i3, o2 = f2.idbdb = l2.result, a2 = b(o2.objectStoreNames);
                if (0 < a2.length) try {
                  var u2 = o2.transaction(1 === (r3 = a2).length ? r3[0] : r3, "readonly");
                  if (h2.autoSchema) t3 = o2, n4 = u2, (e3 = f2).verno = t3.version / 10, n4 = e3._dbSchema = hn(0, t3, n4), e3._storeNames = b(t3.objectStoreNames, 0), nn(e3, [e3._allTables], O(n4), n4);
                  else if (dn(f2, f2._dbSchema, u2), ((i3 = sn(hn(0, (i3 = f2).idbdb, u2), i3._dbSchema)).add.length || i3.change.some(function(e4) {
                    return e4.add.length || e4.change.length;
                  })) && !p2) return console.warn("Dexie SchemaDiff: Schema was extended without increasing the number passed to db.version(). Dexie will add missing parts and increment native version number to workaround this."), o2.close(), d2 = o2.version + 1, p2 = true, s2(y2());
                  tn(f2, u2);
                } catch (e4) {
                }
                et.push(f2), o2.onversionchange = Ie(function(e4) {
                  h2.vcFired = true, f2.on("versionchange").fire(e4);
                }), o2.onclose = Ie(function() {
                  f2.close({ disableAutoOpen: false });
                }), m2 && (i3 = f2._deps, u2 = c2, o2 = i3.indexedDB, i3 = i3.IDBKeyRange, bn(o2) || u2 === tt || mn(o2, i3).put({ name: u2 }).catch(G)), s2();
              }, n3);
            }).catch(function(e3) {
              switch (null == e3 ? void 0 : e3.name) {
                case "UnknownError":
                  if (0 < h2.PR1398_maxLoop) return h2.PR1398_maxLoop--, console.warn("Dexie: Workaround for Chrome UnknownError on open()"), y2();
                  break;
                case "VersionError":
                  if (0 < d2) return d2 = 0, y2();
              }
              return _e.reject(e3);
            });
          }
          var n2, i2 = h2.dbReadyResolve, v2 = null, m2 = false;
          return _e.race([t2, ("undefined" == typeof navigator ? _e.resolve() : !navigator.userAgentData && /Safari\//.test(navigator.userAgent) && !/Chrom(e|ium)\//.test(navigator.userAgent) && indexedDB.databases ? new Promise(function(e3) {
            function t3() {
              return indexedDB.databases().finally(e3);
            }
            n2 = setInterval(t3, 100), t3();
          }).finally(function() {
            return clearInterval(n2);
          }) : Promise.resolve()).then(y2)]).then(function() {
            return e2(), h2.onReadyBeingFired = [], _e.resolve(gn(function() {
              return f2.on.ready.fire(f2.vip);
            })).then(function e3() {
              if (0 < h2.onReadyBeingFired.length) {
                var t3 = h2.onReadyBeingFired.reduce(re, G);
                return h2.onReadyBeingFired = [], _e.resolve(gn(function() {
                  return t3(f2.vip);
                })).then(e3);
              }
            });
          }).finally(function() {
            h2.openCanceller === t2 && (h2.onReadyBeingFired = null, h2.isBeingOpened = false);
          }).catch(function(e3) {
            h2.dbOpenError = e3;
            try {
              v2 && v2.abort();
            } catch (e4) {
            }
            return t2 === h2.openCanceller && f2._close(), Xe(e3);
          }).finally(function() {
            h2.openComplete = true, i2();
          }).then(function() {
            var n3;
            return m2 && (n3 = {}, f2.tables.forEach(function(t3) {
              t3.schema.indexes.forEach(function(e3) {
                e3.name && (n3["idb://".concat(f2.name, "/").concat(t3.name, "/").concat(e3.name)] = new _n(-1 / 0, [[[]]]));
              }), n3["idb://".concat(f2.name, "/").concat(t3.name, "/")] = n3["idb://".concat(f2.name, "/").concat(t3.name, "/:dels")] = new _n(-1 / 0, [[[]]]);
            }), Ut(Nt).fire(n3), qn(n3, true)), f2;
          });
        }
        function Rn(t2) {
          function e2(e3) {
            return t2.next(e3);
          }
          var r2 = n2(e2), i2 = n2(function(e3) {
            return t2.throw(e3);
          });
          function n2(n3) {
            return function(e3) {
              var t3 = n3(e3), e3 = t3.value;
              return t3.done ? e3 : e3 && "function" == typeof e3.then ? e3.then(r2, i2) : x(e3) ? Promise.all(e3).then(r2, i2) : r2(e3);
            };
          }
          return n2(e2)();
        }
        function Fn(e2, t2, n2) {
          for (var r2 = x(e2) ? e2.slice() : [e2], i2 = 0; i2 < n2; ++i2) r2.push(t2);
          return r2;
        }
        var Mn = { stack: "dbcore", name: "VirtualIndexMiddleware", level: 1, create: function(f2) {
          return _(_({}, f2), { table: function(e2) {
            var a2 = f2.table(e2), t2 = a2.schema, u2 = {}, s2 = [];
            function c2(e3, t3, n3) {
              var r3 = Jt(e3), i3 = u2[r3] = u2[r3] || [], o2 = null == e3 ? 0 : "string" == typeof e3 ? 1 : e3.length, a3 = 0 < t3, a3 = _(_({}, n3), { name: a3 ? "".concat(r3, "(virtual-from:").concat(n3.name, ")") : n3.name, lowLevelIndex: n3, isVirtual: a3, keyTail: t3, keyLength: o2, extractKey: Gt(e3), unique: !a3 && n3.unique });
              return i3.push(a3), a3.isPrimaryKey || s2.push(a3), 1 < o2 && c2(2 === o2 ? e3[0] : e3.slice(0, o2 - 1), t3 + 1, n3), i3.sort(function(e4, t4) {
                return e4.keyTail - t4.keyTail;
              }), a3;
            }
            e2 = c2(t2.primaryKey.keyPath, 0, t2.primaryKey);
            u2[":id"] = [e2];
            for (var n2 = 0, r2 = t2.indexes; n2 < r2.length; n2++) {
              var i2 = r2[n2];
              c2(i2.keyPath, 0, i2);
            }
            function l2(e3) {
              var t3, n3 = e3.query.index;
              return n3.isVirtual ? _(_({}, e3), { query: { index: n3.lowLevelIndex, range: (t3 = e3.query.range, n3 = n3.keyTail, { type: 1 === t3.type ? 2 : t3.type, lower: Fn(t3.lower, t3.lowerOpen ? f2.MAX_KEY : f2.MIN_KEY, n3), lowerOpen: true, upper: Fn(t3.upper, t3.upperOpen ? f2.MIN_KEY : f2.MAX_KEY, n3), upperOpen: true }) } }) : e3;
            }
            return _(_({}, a2), { schema: _(_({}, t2), { primaryKey: e2, indexes: s2, getIndexByKeyPath: function(e3) {
              return (e3 = u2[Jt(e3)]) && e3[0];
            } }), count: function(e3) {
              return a2.count(l2(e3));
            }, query: function(e3) {
              return a2.query(l2(e3));
            }, openCursor: function(t3) {
              var e3 = t3.query.index, r3 = e3.keyTail, n3 = e3.isVirtual, i3 = e3.keyLength;
              return n3 ? a2.openCursor(l2(t3)).then(function(e4) {
                return e4 && o2(e4);
              }) : a2.openCursor(t3);
              function o2(n4) {
                return Object.create(n4, { continue: { value: function(e4) {
                  null != e4 ? n4.continue(Fn(e4, t3.reverse ? f2.MAX_KEY : f2.MIN_KEY, r3)) : t3.unique ? n4.continue(n4.key.slice(0, i3).concat(t3.reverse ? f2.MIN_KEY : f2.MAX_KEY, r3)) : n4.continue();
                } }, continuePrimaryKey: { value: function(e4, t4) {
                  n4.continuePrimaryKey(Fn(e4, f2.MAX_KEY, r3), t4);
                } }, primaryKey: { get: function() {
                  return n4.primaryKey;
                } }, key: { get: function() {
                  var e4 = n4.key;
                  return 1 === i3 ? e4[0] : e4.slice(0, i3);
                } }, value: { get: function() {
                  return n4.value;
                } } });
              }
            } });
          } });
        } };
        function Nn(i2, o2, a2, u2) {
          return a2 = a2 || {}, u2 = u2 || "", O(i2).forEach(function(e2) {
            var t2, n2, r2;
            m(o2, e2) ? (t2 = i2[e2], n2 = o2[e2], "object" == typeof t2 && "object" == typeof n2 && t2 && n2 ? (r2 = A(t2)) !== A(n2) ? a2[u2 + e2] = o2[e2] : "Object" === r2 ? Nn(t2, n2, a2, u2 + e2 + ".") : t2 !== n2 && (a2[u2 + e2] = o2[e2]) : t2 !== n2 && (a2[u2 + e2] = o2[e2])) : a2[u2 + e2] = void 0;
          }), O(o2).forEach(function(e2) {
            m(i2, e2) || (a2[u2 + e2] = o2[e2]);
          }), a2;
        }
        function Ln(e2, t2) {
          return "delete" === t2.type ? t2.keys : t2.keys || t2.values.map(e2.extractKey);
        }
        var Un = { stack: "dbcore", name: "HooksMiddleware", level: 2, create: function(e2) {
          return _(_({}, e2), { table: function(r2) {
            var y2 = e2.table(r2), v2 = y2.schema.primaryKey;
            return _(_({}, y2), { mutate: function(e3) {
              var t2 = me.trans, n2 = t2.table(r2).hook, h2 = n2.deleting, d2 = n2.creating, p2 = n2.updating;
              switch (e3.type) {
                case "add":
                  if (d2.fire === G) break;
                  return t2._promise("readwrite", function() {
                    return a2(e3);
                  }, true);
                case "put":
                  if (d2.fire === G && p2.fire === G) break;
                  return t2._promise("readwrite", function() {
                    return a2(e3);
                  }, true);
                case "delete":
                  if (h2.fire === G) break;
                  return t2._promise("readwrite", function() {
                    return a2(e3);
                  }, true);
                case "deleteRange":
                  if (h2.fire === G) break;
                  return t2._promise("readwrite", function() {
                    return (function n3(r3, i2, o2) {
                      return y2.query({ trans: r3, values: false, query: { index: v2, range: i2 }, limit: o2 }).then(function(e4) {
                        var t3 = e4.result;
                        return a2({ type: "delete", keys: t3, trans: r3 }).then(function(e5) {
                          return 0 < e5.numFailures ? Promise.reject(e5.failures[0]) : t3.length < o2 ? { failures: [], numFailures: 0, lastResult: void 0 } : n3(r3, _(_({}, i2), { lower: t3[t3.length - 1], lowerOpen: true }), o2);
                        });
                      });
                    })(e3.trans, e3.range, 1e4);
                  }, true);
              }
              return y2.mutate(e3);
              function a2(c2) {
                var e4, t3, n3, l2 = me.trans, f2 = c2.keys || Ln(v2, c2);
                if (!f2) throw new Error("Keys missing");
                return "delete" !== (c2 = "add" === c2.type || "put" === c2.type ? _(_({}, c2), { keys: f2 }) : _({}, c2)).type && (c2.values = i([], c2.values)), c2.keys && (c2.keys = i([], c2.keys)), e4 = y2, n3 = f2, ("add" === (t3 = c2).type ? Promise.resolve([]) : e4.getMany({ trans: t3.trans, keys: n3, cache: "immutable" })).then(function(u2) {
                  var s2 = f2.map(function(e5, t4) {
                    var n4, r3, i2, o2 = u2[t4], a3 = { onerror: null, onsuccess: null };
                    return "delete" === c2.type ? h2.fire.call(a3, e5, o2, l2) : "add" === c2.type || void 0 === o2 ? (n4 = d2.fire.call(a3, e5, c2.values[t4], l2), null == e5 && null != n4 && (c2.keys[t4] = e5 = n4, v2.outbound || w(c2.values[t4], v2.keyPath, e5))) : (n4 = Nn(o2, c2.values[t4]), (r3 = p2.fire.call(a3, n4, e5, o2, l2)) && (i2 = c2.values[t4], Object.keys(r3).forEach(function(e6) {
                      m(i2, e6) ? i2[e6] = r3[e6] : w(i2, e6, r3[e6]);
                    }))), a3;
                  });
                  return y2.mutate(c2).then(function(e5) {
                    for (var t4 = e5.failures, n4 = e5.results, r3 = e5.numFailures, e5 = e5.lastResult, i2 = 0; i2 < f2.length; ++i2) {
                      var o2 = (n4 || f2)[i2], a3 = s2[i2];
                      null == o2 ? a3.onerror && a3.onerror(t4[i2]) : a3.onsuccess && a3.onsuccess("put" === c2.type && u2[i2] ? c2.values[i2] : o2);
                    }
                    return { failures: t4, results: n4, numFailures: r3, lastResult: e5 };
                  }).catch(function(t4) {
                    return s2.forEach(function(e5) {
                      return e5.onerror && e5.onerror(t4);
                    }), Promise.reject(t4);
                  });
                });
              }
            } });
          } });
        } };
        function Vn(e2, t2, n2) {
          try {
            if (!t2) return null;
            if (t2.keys.length < e2.length) return null;
            for (var r2 = [], i2 = 0, o2 = 0; i2 < t2.keys.length && o2 < e2.length; ++i2) 0 === st(t2.keys[i2], e2[o2]) && (r2.push(n2 ? S(t2.values[i2]) : t2.values[i2]), ++o2);
            return r2.length === e2.length ? r2 : null;
          } catch (e3) {
            return null;
          }
        }
        var zn = { stack: "dbcore", level: -1, create: function(t2) {
          return { table: function(e2) {
            var n2 = t2.table(e2);
            return _(_({}, n2), { getMany: function(t3) {
              if (!t3.cache) return n2.getMany(t3);
              var e3 = Vn(t3.keys, t3.trans._cache, "clone" === t3.cache);
              return e3 ? _e.resolve(e3) : n2.getMany(t3).then(function(e4) {
                return t3.trans._cache = { keys: t3.keys, values: "clone" === t3.cache ? S(e4) : e4 }, e4;
              });
            }, mutate: function(e3) {
              return "add" !== e3.type && (e3.trans._cache = null), n2.mutate(e3);
            } });
          } };
        } };
        function Wn(e2, t2) {
          return "readonly" === e2.trans.mode && !!e2.subscr && !e2.trans.explicit && "disabled" !== e2.trans.db._options.cache && !t2.schema.primaryKey.outbound;
        }
        function Yn(e2, t2) {
          switch (e2) {
            case "query":
              return t2.values && !t2.unique;
            case "get":
            case "getMany":
            case "count":
            case "openCursor":
              return false;
          }
        }
        var $n = { stack: "dbcore", level: 0, name: "Observability", create: function(b2) {
          var g2 = b2.schema.name, w2 = new _n(b2.MIN_KEY, b2.MAX_KEY);
          return _(_({}, b2), { transaction: function(e2, t2, n2) {
            if (me.subscr && "readonly" !== t2) throw new Y.ReadOnly("Readwrite transaction in liveQuery context. Querier source: ".concat(me.querier));
            return b2.transaction(e2, t2, n2);
          }, table: function(d2) {
            var p2 = b2.table(d2), y2 = p2.schema, v2 = y2.primaryKey, e2 = y2.indexes, c2 = v2.extractKey, l2 = v2.outbound, m2 = v2.autoIncrement && e2.filter(function(e3) {
              return e3.compound && e3.keyPath.includes(v2.keyPath);
            }), t2 = _(_({}, p2), { mutate: function(a2) {
              function u2(e4) {
                return e4 = "idb://".concat(g2, "/").concat(d2, "/").concat(e4), n2[e4] || (n2[e4] = new _n());
              }
              var e3, o2, s2, t3 = a2.trans, n2 = a2.mutatedParts || (a2.mutatedParts = {}), r2 = u2(""), i2 = u2(":dels"), c3 = a2.type, l3 = "deleteRange" === a2.type ? [a2.range] : "delete" === a2.type ? [a2.keys] : a2.values.length < 50 ? [Ln(v2, a2).filter(function(e4) {
                return e4;
              }), a2.values] : [], f3 = l3[0], h2 = l3[1], l3 = a2.trans._cache;
              return x(f3) ? (r2.addKeys(f3), (l3 = "delete" === c3 || f3.length === h2.length ? Vn(f3, l3) : null) || i2.addKeys(f3), (l3 || h2) && (e3 = u2, o2 = l3, s2 = h2, y2.indexes.forEach(function(t4) {
                var n3 = e3(t4.name || "");
                function r3(e4) {
                  return null != e4 ? t4.extractKey(e4) : null;
                }
                function i3(e4) {
                  return t4.multiEntry && x(e4) ? e4.forEach(function(e5) {
                    return n3.addKey(e5);
                  }) : n3.addKey(e4);
                }
                (o2 || s2).forEach(function(e4, t5) {
                  var n4 = o2 && r3(o2[t5]), t5 = s2 && r3(s2[t5]);
                  0 !== st(n4, t5) && (null != n4 && i3(n4), null != t5 && i3(t5));
                });
              }))) : f3 ? (h2 = { from: null !== (h2 = f3.lower) && void 0 !== h2 ? h2 : b2.MIN_KEY, to: null !== (h2 = f3.upper) && void 0 !== h2 ? h2 : b2.MAX_KEY }, i2.add(h2), r2.add(h2)) : (r2.add(w2), i2.add(w2), y2.indexes.forEach(function(e4) {
                return u2(e4.name).add(w2);
              })), p2.mutate(a2).then(function(o3) {
                return !f3 || "add" !== a2.type && "put" !== a2.type || (r2.addKeys(o3.results), m2 && m2.forEach(function(t4) {
                  for (var e4 = a2.values.map(function(e5) {
                    return t4.extractKey(e5);
                  }), n3 = t4.keyPath.findIndex(function(e5) {
                    return e5 === v2.keyPath;
                  }), r3 = 0, i3 = o3.results.length; r3 < i3; ++r3) e4[r3][n3] = o3.results[r3];
                  u2(t4.name).addKeys(e4);
                })), t3.mutatedParts = Sn(t3.mutatedParts || {}, n2), o3;
              });
            } }), e2 = function(e3) {
              var t3 = e3.query, e3 = t3.index, t3 = t3.range;
              return [e3, new _n(null !== (e3 = t3.lower) && void 0 !== e3 ? e3 : b2.MIN_KEY, null !== (t3 = t3.upper) && void 0 !== t3 ? t3 : b2.MAX_KEY)];
            }, f2 = { get: function(e3) {
              return [v2, new _n(e3.key)];
            }, getMany: function(e3) {
              return [v2, new _n().addKeys(e3.keys)];
            }, count: e2, query: e2, openCursor: e2 };
            return O(f2).forEach(function(s2) {
              t2[s2] = function(i2) {
                var e3 = me.subscr, t3 = !!e3, n2 = Wn(me, p2) && Yn(s2, i2) ? i2.obsSet = {} : e3;
                if (t3) {
                  var r2 = function(e4) {
                    e4 = "idb://".concat(g2, "/").concat(d2, "/").concat(e4);
                    return n2[e4] || (n2[e4] = new _n());
                  }, o2 = r2(""), a2 = r2(":dels"), e3 = f2[s2](i2), t3 = e3[0], e3 = e3[1];
                  if (("query" === s2 && t3.isPrimaryKey && !i2.values ? a2 : r2(t3.name || "")).add(e3), !t3.isPrimaryKey) {
                    if ("count" !== s2) {
                      var u2 = "query" === s2 && l2 && i2.values && p2.query(_(_({}, i2), { values: false }));
                      return p2[s2].apply(this, arguments).then(function(t4) {
                        if ("query" === s2) {
                          if (l2 && i2.values) return u2.then(function(e5) {
                            e5 = e5.result;
                            return o2.addKeys(e5), t4;
                          });
                          var e4 = i2.values ? t4.result.map(c2) : t4.result;
                          (i2.values ? o2 : a2).addKeys(e4);
                        } else if ("openCursor" === s2) {
                          var n3 = t4, r3 = i2.values;
                          return n3 && Object.create(n3, { key: { get: function() {
                            return a2.addKey(n3.primaryKey), n3.key;
                          } }, primaryKey: { get: function() {
                            var e5 = n3.primaryKey;
                            return a2.addKey(e5), e5;
                          } }, value: { get: function() {
                            return r3 && o2.addKey(n3.primaryKey), n3.value;
                          } } });
                        }
                        return t4;
                      });
                    }
                    a2.add(w2);
                  }
                }
                return p2[s2].apply(this, arguments);
              };
            }), t2;
          } });
        } };
        function Qn(e2, t2, n2) {
          if (0 === n2.numFailures) return t2;
          if ("deleteRange" === t2.type) return null;
          var r2 = t2.keys ? t2.keys.length : "values" in t2 && t2.values ? t2.values.length : 1;
          if (n2.numFailures === r2) return null;
          t2 = _({}, t2);
          return x(t2.keys) && (t2.keys = t2.keys.filter(function(e3, t3) {
            return !(t3 in n2.failures);
          })), "values" in t2 && x(t2.values) && (t2.values = t2.values.filter(function(e3, t3) {
            return !(t3 in n2.failures);
          })), t2;
        }
        function Gn(e2, t2) {
          return n2 = e2, (void 0 === (r2 = t2).lower || (r2.lowerOpen ? 0 < st(n2, r2.lower) : 0 <= st(n2, r2.lower))) && (e2 = e2, void 0 === (t2 = t2).upper || (t2.upperOpen ? st(e2, t2.upper) < 0 : st(e2, t2.upper) <= 0));
          var n2, r2;
        }
        function Xn(e2, d2, t2, n2, r2, i2) {
          if (!t2 || 0 === t2.length) return e2;
          var o2 = d2.query.index, p2 = o2.multiEntry, y2 = d2.query.range, v2 = n2.schema.primaryKey.extractKey, m2 = o2.extractKey, a2 = (o2.lowLevelIndex || o2).extractKey, t2 = t2.reduce(function(e3, t3) {
            var n3 = e3, r3 = [];
            if ("add" === t3.type || "put" === t3.type) for (var i3 = new _n(), o3 = t3.values.length - 1; 0 <= o3; --o3) {
              var a3, u2 = t3.values[o3], s2 = v2(u2);
              i3.hasKey(s2) || (a3 = m2(u2), (p2 && x(a3) ? a3.some(function(e4) {
                return Gn(e4, y2);
              }) : Gn(a3, y2)) && (i3.addKey(s2), r3.push(u2)));
            }
            switch (t3.type) {
              case "add":
                var c2 = new _n().addKeys(d2.values ? e3.map(function(e4) {
                  return v2(e4);
                }) : e3), n3 = e3.concat(d2.values ? r3.filter(function(e4) {
                  e4 = v2(e4);
                  return !c2.hasKey(e4) && (c2.addKey(e4), true);
                }) : r3.map(function(e4) {
                  return v2(e4);
                }).filter(function(e4) {
                  return !c2.hasKey(e4) && (c2.addKey(e4), true);
                }));
                break;
              case "put":
                var l2 = new _n().addKeys(t3.values.map(function(e4) {
                  return v2(e4);
                }));
                n3 = e3.filter(function(e4) {
                  return !l2.hasKey(d2.values ? v2(e4) : e4);
                }).concat(d2.values ? r3 : r3.map(function(e4) {
                  return v2(e4);
                }));
                break;
              case "delete":
                var f2 = new _n().addKeys(t3.keys);
                n3 = e3.filter(function(e4) {
                  return !f2.hasKey(d2.values ? v2(e4) : e4);
                });
                break;
              case "deleteRange":
                var h2 = t3.range;
                n3 = e3.filter(function(e4) {
                  return !Gn(v2(e4), h2);
                });
            }
            return n3;
          }, e2);
          return t2 === e2 ? e2 : (t2.sort(function(e3, t3) {
            return st(a2(e3), a2(t3)) || st(v2(e3), v2(t3));
          }), d2.limit && d2.limit < 1 / 0 && (t2.length > d2.limit ? t2.length = d2.limit : e2.length === d2.limit && t2.length < d2.limit && (r2.dirty = true)), i2 ? Object.freeze(t2) : t2);
        }
        function Hn(e2, t2) {
          return 0 === st(e2.lower, t2.lower) && 0 === st(e2.upper, t2.upper) && !!e2.lowerOpen == !!t2.lowerOpen && !!e2.upperOpen == !!t2.upperOpen;
        }
        function Jn(e2, t2) {
          return (function(e3, t3, n2, r2) {
            if (void 0 === e3) return void 0 !== t3 ? -1 : 0;
            if (void 0 === t3) return 1;
            if (0 === (t3 = st(e3, t3))) {
              if (n2 && r2) return 0;
              if (n2) return 1;
              if (r2) return -1;
            }
            return t3;
          })(e2.lower, t2.lower, e2.lowerOpen, t2.lowerOpen) <= 0 && 0 <= (function(e3, t3, n2, r2) {
            if (void 0 === e3) return void 0 !== t3 ? 1 : 0;
            if (void 0 === t3) return -1;
            if (0 === (t3 = st(e3, t3))) {
              if (n2 && r2) return 0;
              if (n2) return -1;
              if (r2) return 1;
            }
            return t3;
          })(e2.upper, t2.upper, e2.upperOpen, t2.upperOpen);
        }
        function Zn(n2, r2, i2, e2) {
          n2.subscribers.add(i2), e2.addEventListener("abort", function() {
            var e3, t2;
            n2.subscribers.delete(i2), 0 === n2.subscribers.size && (e3 = n2, t2 = r2, setTimeout(function() {
              0 === e3.subscribers.size && I(t2, e3);
            }, 3e3));
          });
        }
        var er = { stack: "dbcore", level: 0, name: "Cache", create: function(k2) {
          var O2 = k2.schema.name;
          return _(_({}, k2), { transaction: function(g2, w2, e2) {
            var _2, t2, x2 = k2.transaction(g2, w2, e2);
            return "readwrite" === w2 && (t2 = (_2 = new AbortController()).signal, e2 = function(b2) {
              return function() {
                if (_2.abort(), "readwrite" === w2) {
                  for (var t3 = /* @__PURE__ */ new Set(), e3 = 0, n2 = g2; e3 < n2.length; e3++) {
                    var r2 = n2[e3], i2 = An["idb://".concat(O2, "/").concat(r2)];
                    if (i2) {
                      var o2 = k2.table(r2), a2 = i2.optimisticOps.filter(function(e4) {
                        return e4.trans === x2;
                      });
                      if (x2._explicit && b2 && x2.mutatedParts) for (var u2 = 0, s2 = Object.values(i2.queries.query); u2 < s2.length; u2++) for (var c2 = 0, l2 = (d2 = s2[u2]).slice(); c2 < l2.length; c2++) jn((p2 = l2[c2]).obsSet, x2.mutatedParts) && (I(d2, p2), p2.subscribers.forEach(function(e4) {
                        return t3.add(e4);
                      }));
                      else if (0 < a2.length) {
                        i2.optimisticOps = i2.optimisticOps.filter(function(e4) {
                          return e4.trans !== x2;
                        });
                        for (var f2 = 0, h2 = Object.values(i2.queries.query); f2 < h2.length; f2++) for (var d2, p2, y2, v2 = 0, m2 = (d2 = h2[f2]).slice(); v2 < m2.length; v2++) null != (p2 = m2[v2]).res && x2.mutatedParts && (b2 && !p2.dirty ? (y2 = Object.isFrozen(p2.res), y2 = Xn(p2.res, p2.req, a2, o2, p2, y2), p2.dirty ? (I(d2, p2), p2.subscribers.forEach(function(e4) {
                          return t3.add(e4);
                        })) : y2 !== p2.res && (p2.res = y2, p2.promise = _e.resolve({ result: y2 }))) : (p2.dirty && I(d2, p2), p2.subscribers.forEach(function(e4) {
                          return t3.add(e4);
                        })));
                      }
                    }
                  }
                  t3.forEach(function(e4) {
                    return e4();
                  });
                }
              };
            }, x2.addEventListener("abort", e2(false), { signal: t2 }), x2.addEventListener("error", e2(false), { signal: t2 }), x2.addEventListener("complete", e2(true), { signal: t2 })), x2;
          }, table: function(c2) {
            var l2 = k2.table(c2), i2 = l2.schema.primaryKey;
            return _(_({}, l2), { mutate: function(t2) {
              var e2 = me.trans;
              if (i2.outbound || "disabled" === e2.db._options.cache || e2.explicit || "readwrite" !== e2.idbtrans.mode) return l2.mutate(t2);
              var n2 = An["idb://".concat(O2, "/").concat(c2)];
              if (!n2) return l2.mutate(t2);
              e2 = l2.mutate(t2);
              return "add" !== t2.type && "put" !== t2.type || !(50 <= t2.values.length || Ln(i2, t2).some(function(e3) {
                return null == e3;
              })) ? (n2.optimisticOps.push(t2), t2.mutatedParts && In(t2.mutatedParts), e2.then(function(e3) {
                0 < e3.numFailures && (I(n2.optimisticOps, t2), (e3 = Qn(0, t2, e3)) && n2.optimisticOps.push(e3), t2.mutatedParts && In(t2.mutatedParts));
              }), e2.catch(function() {
                I(n2.optimisticOps, t2), t2.mutatedParts && In(t2.mutatedParts);
              })) : e2.then(function(r2) {
                var e3 = Qn(0, _(_({}, t2), { values: t2.values.map(function(e4, t3) {
                  var n3;
                  if (r2.failures[t3]) return e4;
                  e4 = null !== (n3 = i2.keyPath) && void 0 !== n3 && n3.includes(".") ? S(e4) : _({}, e4);
                  return w(e4, i2.keyPath, r2.results[t3]), e4;
                }) }), r2);
                n2.optimisticOps.push(e3), queueMicrotask(function() {
                  return t2.mutatedParts && In(t2.mutatedParts);
                });
              }), e2;
            }, query: function(t2) {
              if (!Wn(me, l2) || !Yn("query", t2)) return l2.query(t2);
              var i3 = "immutable" === (null === (o2 = me.trans) || void 0 === o2 ? void 0 : o2.db._options.cache), e2 = me, n2 = e2.requery, r2 = e2.signal, o2 = (function(e3, t3, n3, r3) {
                var i4 = An["idb://".concat(e3, "/").concat(t3)];
                if (!i4) return [];
                if (!(t3 = i4.queries[n3])) return [null, false, i4, null];
                var o3 = t3[(r3.query ? r3.query.index.name : null) || ""];
                if (!o3) return [null, false, i4, null];
                switch (n3) {
                  case "query":
                    var a3 = o3.find(function(e4) {
                      return e4.req.limit === r3.limit && e4.req.values === r3.values && Hn(e4.req.query.range, r3.query.range);
                    });
                    return a3 ? [a3, true, i4, o3] : [o3.find(function(e4) {
                      return ("limit" in e4.req ? e4.req.limit : 1 / 0) >= r3.limit && (!r3.values || e4.req.values) && Jn(e4.req.query.range, r3.query.range);
                    }), false, i4, o3];
                  case "count":
                    a3 = o3.find(function(e4) {
                      return Hn(e4.req.query.range, r3.query.range);
                    });
                    return [a3, !!a3, i4, o3];
                }
              })(O2, c2, "query", t2), a2 = o2[0], e2 = o2[1], u2 = o2[2], s2 = o2[3];
              return a2 && e2 ? a2.obsSet = t2.obsSet : (e2 = l2.query(t2).then(function(e3) {
                var t3 = e3.result;
                if (a2 && (a2.res = t3), i3) {
                  for (var n3 = 0, r3 = t3.length; n3 < r3; ++n3) Object.freeze(t3[n3]);
                  Object.freeze(t3);
                } else e3.result = S(t3);
                return e3;
              }).catch(function(e3) {
                return s2 && a2 && I(s2, a2), Promise.reject(e3);
              }), a2 = { obsSet: t2.obsSet, promise: e2, subscribers: /* @__PURE__ */ new Set(), type: "query", req: t2, dirty: false }, s2 ? s2.push(a2) : (s2 = [a2], (u2 = u2 || (An["idb://".concat(O2, "/").concat(c2)] = { queries: { query: {}, count: {} }, objs: /* @__PURE__ */ new Map(), optimisticOps: [], unsignaledParts: {} })).queries.query[t2.query.index.name || ""] = s2)), Zn(a2, s2, n2, r2), a2.promise.then(function(e3) {
                return { result: Xn(e3.result, t2, null == u2 ? void 0 : u2.optimisticOps, l2, a2, i3) };
              });
            } });
          } });
        } };
        function tr(e2, r2) {
          return new Proxy(e2, { get: function(e3, t2, n2) {
            return "db" === t2 ? r2 : Reflect.get(e3, t2, n2);
          } });
        }
        var nr = (rr.prototype.version = function(t2) {
          if (isNaN(t2) || t2 < 0.1) throw new Y.Type("Given version is not a positive number");
          if (t2 = Math.round(10 * t2) / 10, this.idbdb || this._state.isBeingOpened) throw new Y.Schema("Cannot add version when database is open");
          this.verno = Math.max(this.verno, t2);
          var e2 = this._versions, n2 = e2.filter(function(e3) {
            return e3._cfg.version === t2;
          })[0];
          return n2 || (n2 = new this.Version(t2), e2.push(n2), e2.sort(on), n2.stores({}), this._state.autoSchema = false, n2);
        }, rr.prototype._whenReady = function(e2) {
          var n2 = this;
          return this.idbdb && (this._state.openComplete || me.letThrough || this._vip) ? e2() : new _e(function(e3, t2) {
            if (n2._state.openComplete) return t2(new Y.DatabaseClosed(n2._state.dbOpenError));
            if (!n2._state.isBeingOpened) {
              if (!n2._state.autoOpen) return void t2(new Y.DatabaseClosed());
              n2.open().catch(G);
            }
            n2._state.dbReadyPromise.then(e3, t2);
          }).then(e2);
        }, rr.prototype.use = function(e2) {
          var t2 = e2.stack, n2 = e2.create, r2 = e2.level, i2 = e2.name;
          i2 && this.unuse({ stack: t2, name: i2 });
          e2 = this._middlewares[t2] || (this._middlewares[t2] = []);
          return e2.push({ stack: t2, create: n2, level: null == r2 ? 10 : r2, name: i2 }), e2.sort(function(e3, t3) {
            return e3.level - t3.level;
          }), this;
        }, rr.prototype.unuse = function(e2) {
          var t2 = e2.stack, n2 = e2.name, r2 = e2.create;
          return t2 && this._middlewares[t2] && (this._middlewares[t2] = this._middlewares[t2].filter(function(e3) {
            return r2 ? e3.create !== r2 : !!n2 && e3.name !== n2;
          })), this;
        }, rr.prototype.open = function() {
          var e2 = this;
          return $e(ve, function() {
            return Bn(e2);
          });
        }, rr.prototype._close = function() {
          this.on.close.fire(new CustomEvent("close"));
          var n2 = this._state, e2 = et.indexOf(this);
          if (0 <= e2 && et.splice(e2, 1), this.idbdb) {
            try {
              this.idbdb.close();
            } catch (e3) {
            }
            this.idbdb = null;
          }
          n2.isBeingOpened || (n2.dbReadyPromise = new _e(function(e3) {
            n2.dbReadyResolve = e3;
          }), n2.openCanceller = new _e(function(e3, t2) {
            n2.cancelOpen = t2;
          }));
        }, rr.prototype.close = function(e2) {
          var t2 = (void 0 === e2 ? { disableAutoOpen: true } : e2).disableAutoOpen, e2 = this._state;
          t2 ? (e2.isBeingOpened && e2.cancelOpen(new Y.DatabaseClosed()), this._close(), e2.autoOpen = false, e2.dbOpenError = new Y.DatabaseClosed()) : (this._close(), e2.autoOpen = this._options.autoOpen || e2.isBeingOpened, e2.openComplete = false, e2.dbOpenError = null);
        }, rr.prototype.delete = function(n2) {
          var i2 = this;
          void 0 === n2 && (n2 = { disableAutoOpen: true });
          var o2 = 0 < arguments.length && "object" != typeof arguments[0], a2 = this._state;
          return new _e(function(r2, t2) {
            function e2() {
              i2.close(n2);
              var e3 = i2._deps.indexedDB.deleteDatabase(i2.name);
              e3.onsuccess = Ie(function() {
                var e4, t3, n3;
                e4 = i2._deps, t3 = i2.name, n3 = e4.indexedDB, e4 = e4.IDBKeyRange, bn(n3) || t3 === tt || mn(n3, e4).delete(t3).catch(G), r2();
              }), e3.onerror = Ft(t2), e3.onblocked = i2._fireOnBlocked;
            }
            if (o2) throw new Y.InvalidArgument("Invalid closeOptions argument to db.delete()");
            a2.isBeingOpened ? a2.dbReadyPromise.then(e2) : e2();
          });
        }, rr.prototype.backendDB = function() {
          return this.idbdb;
        }, rr.prototype.isOpen = function() {
          return null !== this.idbdb;
        }, rr.prototype.hasBeenClosed = function() {
          var e2 = this._state.dbOpenError;
          return e2 && "DatabaseClosed" === e2.name;
        }, rr.prototype.hasFailed = function() {
          return null !== this._state.dbOpenError;
        }, rr.prototype.dynamicallyOpened = function() {
          return this._state.autoSchema;
        }, Object.defineProperty(rr.prototype, "tables", { get: function() {
          var t2 = this;
          return O(this._allTables).map(function(e2) {
            return t2._allTables[e2];
          });
        }, enumerable: false, configurable: true }), rr.prototype.transaction = function() {
          var e2 = (function(e3, t2, n2) {
            var r2 = arguments.length;
            if (r2 < 2) throw new Y.InvalidArgument("Too few arguments");
            for (var i2 = new Array(r2 - 1); --r2; ) i2[r2 - 1] = arguments[r2];
            return n2 = i2.pop(), [e3, P(i2), n2];
          }).apply(this, arguments);
          return this._transaction.apply(this, e2);
        }, rr.prototype._transaction = function(e2, t2, n2) {
          var r2 = this, i2 = me.trans;
          i2 && i2.db === this && -1 === e2.indexOf("!") || (i2 = null);
          var o2, a2, u2 = -1 !== e2.indexOf("?");
          e2 = e2.replace("!", "").replace("?", "");
          try {
            if (a2 = t2.map(function(e3) {
              e3 = e3 instanceof r2.Table ? e3.name : e3;
              if ("string" != typeof e3) throw new TypeError("Invalid table argument to Dexie.transaction(). Only Table or String are allowed");
              return e3;
            }), "r" == e2 || e2 === nt) o2 = nt;
            else {
              if ("rw" != e2 && e2 != rt) throw new Y.InvalidArgument("Invalid transaction mode: " + e2);
              o2 = rt;
            }
            if (i2) {
              if (i2.mode === nt && o2 === rt) {
                if (!u2) throw new Y.SubTransaction("Cannot enter a sub-transaction with READWRITE mode when parent transaction is READONLY");
                i2 = null;
              }
              i2 && a2.forEach(function(e3) {
                if (i2 && -1 === i2.storeNames.indexOf(e3)) {
                  if (!u2) throw new Y.SubTransaction("Table " + e3 + " not included in parent transaction.");
                  i2 = null;
                }
              }), u2 && i2 && !i2.active && (i2 = null);
            }
          } catch (n3) {
            return i2 ? i2._promise(null, function(e3, t3) {
              t3(n3);
            }) : Xe(n3);
          }
          var s2 = (function i3(o3, a3, u3, s3, c2) {
            return _e.resolve().then(function() {
              var e3 = me.transless || me, t3 = o3._createTransaction(a3, u3, o3._dbSchema, s3);
              if (t3.explicit = true, e3 = { trans: t3, transless: e3 }, s3) t3.idbtrans = s3.idbtrans;
              else try {
                t3.create(), t3.idbtrans._explicit = true, o3._state.PR1398_maxLoop = 3;
              } catch (e4) {
                return e4.name === z.InvalidState && o3.isOpen() && 0 < --o3._state.PR1398_maxLoop ? (console.warn("Dexie: Need to reopen db"), o3.close({ disableAutoOpen: false }), o3.open().then(function() {
                  return i3(o3, a3, u3, null, c2);
                })) : Xe(e4);
              }
              var n3, r3 = B(c2);
              return r3 && Le(), e3 = _e.follow(function() {
                var e4;
                (n3 = c2.call(t3, t3)) && (r3 ? (e4 = Ue.bind(null, null), n3.then(e4, e4)) : "function" == typeof n3.next && "function" == typeof n3.throw && (n3 = Rn(n3)));
              }, e3), (n3 && "function" == typeof n3.then ? _e.resolve(n3).then(function(e4) {
                return t3.active ? e4 : Xe(new Y.PrematureCommit("Transaction committed too early. See http://bit.ly/2kdckMn"));
              }) : e3.then(function() {
                return n3;
              })).then(function(e4) {
                return s3 && t3._resolve(), t3._completion.then(function() {
                  return e4;
                });
              }).catch(function(e4) {
                return t3._reject(e4), Xe(e4);
              });
            });
          }).bind(null, this, o2, a2, i2, n2);
          return i2 ? i2._promise(o2, s2, "lock") : me.trans ? $e(me.transless, function() {
            return r2._whenReady(s2);
          }) : this._whenReady(s2);
        }, rr.prototype.table = function(e2) {
          if (!m(this._allTables, e2)) throw new Y.InvalidTable("Table ".concat(e2, " does not exist"));
          return this._allTables[e2];
        }, rr);
        function rr(e2, t2) {
          var o2 = this;
          this._middlewares = {}, this.verno = 0;
          var n2 = rr.dependencies;
          this._options = t2 = _({ addons: rr.addons, autoOpen: true, indexedDB: n2.indexedDB, IDBKeyRange: n2.IDBKeyRange, cache: "cloned" }, t2), this._deps = { indexedDB: t2.indexedDB, IDBKeyRange: t2.IDBKeyRange };
          n2 = t2.addons;
          this._dbSchema = {}, this._versions = [], this._storeNames = [], this._allTables = {}, this.idbdb = null, this._novip = this;
          var a2, r2, u2, i2, s2, c2 = { dbOpenError: null, isBeingOpened: false, onReadyBeingFired: null, openComplete: false, dbReadyResolve: G, dbReadyPromise: null, cancelOpen: G, openCanceller: null, autoSchema: true, PR1398_maxLoop: 3, autoOpen: t2.autoOpen };
          c2.dbReadyPromise = new _e(function(e3) {
            c2.dbReadyResolve = e3;
          }), c2.openCanceller = new _e(function(e3, t3) {
            c2.cancelOpen = t3;
          }), this._state = c2, this.name = e2, this.on = mt(this, "populate", "blocked", "versionchange", "close", { ready: [re, G] }), this.once = function(n3, r3) {
            var i3 = function() {
              for (var e3 = [], t3 = 0; t3 < arguments.length; t3++) e3[t3] = arguments[t3];
              o2.on(n3).unsubscribe(i3), r3.apply(o2, e3);
            };
            return o2.on(n3, i3);
          }, this.on.ready.subscribe = p(this.on.ready.subscribe, function(i3) {
            return function(n3, r3) {
              rr.vip(function() {
                var t3, e3 = o2._state;
                e3.openComplete ? (e3.dbOpenError || _e.resolve().then(n3), r3 && i3(n3)) : e3.onReadyBeingFired ? (e3.onReadyBeingFired.push(n3), r3 && i3(n3)) : (i3(n3), t3 = o2, r3 || i3(function e4() {
                  t3.on.ready.unsubscribe(n3), t3.on.ready.unsubscribe(e4);
                }));
              });
            };
          }), this.Collection = (a2 = this, bt(Kt.prototype, function(e3, t3) {
            this.db = a2;
            var n3 = ot, r3 = null;
            if (t3) try {
              n3 = t3();
            } catch (e4) {
              r3 = e4;
            }
            var i3 = e3._ctx, t3 = i3.table, e3 = t3.hook.reading.fire;
            this._ctx = { table: t3, index: i3.index, isPrimKey: !i3.index || t3.schema.primKey.keyPath && i3.index === t3.schema.primKey.name, range: n3, keysOnly: false, dir: "next", unique: "", algorithm: null, filter: null, replayFilter: null, justLimit: true, isMatch: null, offset: 0, limit: 1 / 0, error: r3, or: i3.or, valueMapper: e3 !== X ? e3 : null };
          })), this.Table = (r2 = this, bt(yt.prototype, function(e3, t3, n3) {
            this.db = r2, this._tx = n3, this.name = e3, this.schema = t3, this.hook = r2._allTables[e3] ? r2._allTables[e3].hook : mt(null, { creating: [Z, G], reading: [H, X], updating: [te, G], deleting: [ee, G] });
          })), this.Transaction = (u2 = this, bt(Vt.prototype, function(e3, t3, n3, r3, i3) {
            var o3 = this;
            "readonly" !== e3 && t3.forEach(function(e4) {
              e4 = null === (e4 = n3[e4]) || void 0 === e4 ? void 0 : e4.yProps;
              e4 && (t3 = t3.concat(e4.map(function(e5) {
                return e5.updatesTable;
              })));
            }), this.db = u2, this.mode = e3, this.storeNames = t3, this.schema = n3, this.chromeTransactionDurability = r3, this.idbtrans = null, this.on = mt(this, "complete", "error", "abort"), this.parent = i3 || null, this.active = true, this._reculock = 0, this._blockedFuncs = [], this._resolve = null, this._reject = null, this._waitingFor = null, this._waitingQueue = null, this._spinCount = 0, this._completion = new _e(function(e4, t4) {
              o3._resolve = e4, o3._reject = t4;
            }), this._completion.then(function() {
              o3.active = false, o3.on.complete.fire();
            }, function(e4) {
              var t4 = o3.active;
              return o3.active = false, o3.on.error.fire(e4), o3.parent ? o3.parent._reject(e4) : t4 && o3.idbtrans && o3.idbtrans.abort(), Xe(e4);
            });
          })), this.Version = (i2 = this, bt(yn.prototype, function(e3) {
            this.db = i2, this._cfg = { version: e3, storesSource: null, dbschema: {}, tables: {}, contentUpgrade: null };
          })), this.WhereClause = (s2 = this, bt(Bt.prototype, function(e3, t3, n3) {
            if (this.db = s2, this._ctx = { table: e3, index: ":id" === t3 ? null : t3, or: n3 }, this._cmp = this._ascending = st, this._descending = function(e4, t4) {
              return st(t4, e4);
            }, this._max = function(e4, t4) {
              return 0 < st(e4, t4) ? e4 : t4;
            }, this._min = function(e4, t4) {
              return st(e4, t4) < 0 ? e4 : t4;
            }, this._IDBKeyRange = s2._deps.IDBKeyRange, !this._IDBKeyRange) throw new Y.MissingAPI();
          })), this.on("versionchange", function(e3) {
            0 < e3.newVersion ? console.warn("Another connection wants to upgrade database '".concat(o2.name, "'. Closing db now to resume the upgrade.")) : console.warn("Another connection wants to delete database '".concat(o2.name, "'. Closing db now to resume the delete request.")), o2.close({ disableAutoOpen: false });
          }), this.on("blocked", function(e3) {
            !e3.newVersion || e3.newVersion < e3.oldVersion ? console.warn("Dexie.delete('".concat(o2.name, "') was blocked")) : console.warn("Upgrade '".concat(o2.name, "' blocked by other connection holding version ").concat(e3.oldVersion / 10));
          }), this._maxKey = Qt(t2.IDBKeyRange), this._createTransaction = function(e3, t3, n3, r3) {
            return new o2.Transaction(e3, t3, n3, o2._options.chromeTransactionDurability, r3);
          }, this._fireOnBlocked = function(t3) {
            o2.on("blocked").fire(t3), et.filter(function(e3) {
              return e3.name === o2.name && e3 !== o2 && !e3._state.vcFired;
            }).map(function(e3) {
              return e3.on("versionchange").fire(t3);
            });
          }, this.use(zn), this.use(er), this.use($n), this.use(Mn), this.use(Un);
          var l2 = new Proxy(this, { get: function(e3, t3, n3) {
            if ("_vip" === t3) return true;
            if ("table" === t3) return function(e4) {
              return tr(o2.table(e4), l2);
            };
            var r3 = Reflect.get(e3, t3, n3);
            return r3 instanceof yt ? tr(r3, l2) : "tables" === t3 ? r3.map(function(e4) {
              return tr(e4, l2);
            }) : "_createTransaction" === t3 ? function() {
              return tr(r3.apply(this, arguments), l2);
            } : r3;
          } });
          this.vip = l2, n2.forEach(function(e3) {
            return e3(o2);
          });
        }
        var ir, F = "undefined" != typeof Symbol && "observable" in Symbol ? Symbol.observable : "@@observable", or = (ar.prototype.subscribe = function(e2, t2, n2) {
          return this._subscribe(e2 && "function" != typeof e2 ? e2 : { next: e2, error: t2, complete: n2 });
        }, ar.prototype[F] = function() {
          return this;
        }, ar);
        function ar(e2) {
          this._subscribe = e2;
        }
        try {
          ir = { indexedDB: f.indexedDB || f.mozIndexedDB || f.webkitIndexedDB || f.msIndexedDB, IDBKeyRange: f.IDBKeyRange || f.webkitIDBKeyRange };
        } catch (e2) {
          ir = { indexedDB: null, IDBKeyRange: null };
        }
        function ur(h2) {
          var d2, p2 = false, e2 = new or(function(r2) {
            var i2 = B(h2);
            var o2, a2 = false, u2 = {}, s2 = {}, e3 = { get closed() {
              return a2;
            }, unsubscribe: function() {
              a2 || (a2 = true, o2 && o2.abort(), c2 && Ut.storagemutated.unsubscribe(f2));
            } };
            r2.start && r2.start(e3);
            var c2 = false, l2 = function() {
              return Ge(t2);
            };
            var f2 = function(e4) {
              Sn(u2, e4), jn(s2, u2) && l2();
            }, t2 = function() {
              var t3, n2, e4;
              !a2 && ir.indexedDB && (u2 = {}, t3 = {}, o2 && o2.abort(), o2 = new AbortController(), e4 = (function(e5) {
                var t4 = je();
                try {
                  i2 && Le();
                  var n3 = Ne(h2, e5);
                  return n3 = i2 ? n3.finally(Ue) : n3;
                } finally {
                  t4 && Ae();
                }
              })(n2 = { subscr: t3, signal: o2.signal, requery: l2, querier: h2, trans: null }), Promise.resolve(e4).then(function(e5) {
                p2 = true, d2 = e5, a2 || n2.signal.aborted || (u2 = {}, (function(e6) {
                  for (var t4 in e6) if (m(e6, t4)) return;
                  return 1;
                })(s2 = t3) || c2 || (Ut(Nt, f2), c2 = true), Ge(function() {
                  return !a2 && r2.next && r2.next(e5);
                }));
              }, function(e5) {
                p2 = false, ["DatabaseClosedError", "AbortError"].includes(null == e5 ? void 0 : e5.name) || a2 || Ge(function() {
                  a2 || r2.error && r2.error(e5);
                });
              }));
            };
            return setTimeout(l2, 0), e3;
          });
          return e2.hasValue = function() {
            return p2;
          }, e2.getValue = function() {
            return d2;
          }, e2;
        }
        var sr = nr;
        function cr(e2) {
          var t2 = fr;
          try {
            fr = true, Ut.storagemutated.fire(e2), qn(e2, true);
          } finally {
            fr = t2;
          }
        }
        r(sr, _(_({}, Q), { delete: function(e2) {
          return new sr(e2, { addons: [] }).delete();
        }, exists: function(e2) {
          return new sr(e2, { addons: [] }).open().then(function(e3) {
            return e3.close(), true;
          }).catch("NoSuchDatabaseError", function() {
            return false;
          });
        }, getDatabaseNames: function(e2) {
          try {
            return t2 = sr.dependencies, n2 = t2.indexedDB, t2 = t2.IDBKeyRange, (bn(n2) ? Promise.resolve(n2.databases()).then(function(e3) {
              return e3.map(function(e4) {
                return e4.name;
              }).filter(function(e4) {
                return e4 !== tt;
              });
            }) : mn(n2, t2).toCollection().primaryKeys()).then(e2);
          } catch (e3) {
            return Xe(new Y.MissingAPI());
          }
          var t2, n2;
        }, defineClass: function() {
          return function(e2) {
            a(this, e2);
          };
        }, ignoreTransaction: function(e2) {
          return me.trans ? $e(me.transless, e2) : e2();
        }, vip: gn, async: function(t2) {
          return function() {
            try {
              var e2 = Rn(t2.apply(this, arguments));
              return e2 && "function" == typeof e2.then ? e2 : _e.resolve(e2);
            } catch (e3) {
              return Xe(e3);
            }
          };
        }, spawn: function(e2, t2, n2) {
          try {
            var r2 = Rn(e2.apply(n2, t2 || []));
            return r2 && "function" == typeof r2.then ? r2 : _e.resolve(r2);
          } catch (e3) {
            return Xe(e3);
          }
        }, currentTransaction: { get: function() {
          return me.trans || null;
        } }, waitFor: function(e2, t2) {
          t2 = _e.resolve("function" == typeof e2 ? sr.ignoreTransaction(e2) : e2).timeout(t2 || 6e4);
          return me.trans ? me.trans.waitFor(t2) : t2;
        }, Promise: _e, debug: { get: function() {
          return ie;
        }, set: function(e2) {
          oe(e2);
        } }, derive: o, extend: a, props: r, override: p, Events: mt, on: Ut, liveQuery: ur, extendObservabilitySet: Sn, getByKeyPath: g, setByKeyPath: w, delByKeyPath: function(t2, e2) {
          "string" == typeof e2 ? w(t2, e2, void 0) : "length" in e2 && [].map.call(e2, function(e3) {
            w(t2, e3, void 0);
          });
        }, shallowClone: k, deepClone: S, getObjectDiff: Nn, cmp: st, asap: v, minKey: -1 / 0, addons: [], connections: et, errnames: z, dependencies: ir, cache: An, semVer: "4.2.1", version: "4.2.1".split(".").map(function(e2) {
          return parseInt(e2);
        }).reduce(function(e2, t2, n2) {
          return e2 + t2 / Math.pow(10, 2 * n2);
        }) })), sr.maxKey = Qt(sr.dependencies.IDBKeyRange), "undefined" != typeof dispatchEvent && "undefined" != typeof addEventListener && (Ut(Nt, function(e2) {
          fr || (e2 = new CustomEvent(Lt, { detail: e2 }), fr = true, dispatchEvent(e2), fr = false);
        }), addEventListener(Lt, function(e2) {
          e2 = e2.detail;
          fr || cr(e2);
        }));
        var lr, fr = false, hr = function() {
        };
        return "undefined" != typeof BroadcastChannel && ((hr = function() {
          (lr = new BroadcastChannel(Lt)).onmessage = function(e2) {
            return e2.data && cr(e2.data);
          };
        })(), "function" == typeof lr.unref && lr.unref(), Ut(Nt, function(e2) {
          fr || lr.postMessage(e2);
        })), "undefined" != typeof addEventListener && (addEventListener("pagehide", function(e2) {
          if (!nr.disableBfCache && e2.persisted) {
            ie && console.debug("Dexie: handling persisted pagehide"), null != lr && lr.close();
            for (var t2 = 0, n2 = et; t2 < n2.length; t2++) n2[t2].close({ disableAutoOpen: false });
          }
        }), addEventListener("pageshow", function(e2) {
          !nr.disableBfCache && e2.persisted && (ie && console.debug("Dexie: handling persisted pageshow"), hr(), cr({ all: new _n(-1 / 0, [[]]) }));
        })), _e.rejectionMapper = function(e2, t2) {
          return !e2 || e2 instanceof N || e2 instanceof TypeError || e2 instanceof SyntaxError || !e2.name || !$[e2.name] ? e2 : (t2 = new $[e2.name](t2 || e2.message, e2), "stack" in e2 && l(t2, "stack", { get: function() {
            return this.inner.stack;
          } }), t2);
        }, oe(ie), _(nr, Object.freeze({ __proto__: null, Dexie: nr, liveQuery: ur, Entity: ut, cmp: st, PropModification: ht, replacePrefix: function(e2, t2) {
          return new ht({ replacePrefix: [e2, t2] });
        }, add: function(e2) {
          return new ht({ add: e2 });
        }, remove: function(e2) {
          return new ht({ remove: e2 });
        }, default: nr, RangeSet: _n, mergeRanges: kn, rangesOverlap: On }), { default: nr }), nr;
      });
    })(dexie_min$1);
    return dexie_min$1.exports;
  }
  var dexie_minExports = requireDexie_min();
  const _Dexie = /* @__PURE__ */ getDefaultExportFromCjs(dexie_minExports);
  const DexieSymbol = Symbol.for("Dexie");
  const Dexie = globalThis[DexieSymbol] || (globalThis[DexieSymbol] = _Dexie);
  if (_Dexie.semVer !== Dexie.semVer) {
    throw new Error(`Two different versions of Dexie loaded in the same app: ${_Dexie.semVer} and ${Dexie.semVer}`);
  }
  const {
    liveQuery,
    mergeRanges,
    rangesOverlap,
    RangeSet,
    cmp,
    Entity,
    PropModification,
    replacePrefix,
    add,
    remove,
    DexieYProvider
  } = Dexie;
  class Camarero {
    constructor({ id, nombre, apellidos, activo = 1, pass_field = "", permisos = [], autorizado = 1, email = null }) {
      __publicField(this, "id");
      __publicField(this, "nombre");
      __publicField(this, "apellidos");
      __publicField(this, "activo");
      __publicField(this, "pass_field");
      __publicField(this, "permisos");
      __publicField(this, "autorizado");
      __publicField(this, "email");
      this.id = id || Date.now();
      this.nombre = nombre;
      this.apellidos = apellidos;
      this.activo = activo;
      this.pass_field = pass_field;
      this.permisos = permisos;
      this.autorizado = autorizado;
      this.email = email;
    }
    validate() {
      if (!this.nombre || !this.apellidos) {
        throw new Error("El nombre y los apellidos son obligatorios.");
      }
    }
  }
  __publicField(Camarero, "schema", "++id, nombre, apellidos, activo, autorizado, pass_field, permisos, email");
  class Empresa {
    constructor({ uid, id, nombre, descripcion, url_servidor, activa = 1, usa_cash_keeper = 0, url_cash_keeper = "", usa_pinpad = 0, url_pinpad = "" }) {
      __publicField(this, "uid");
      __publicField(this, "id");
      __publicField(this, "descripcion");
      __publicField(this, "nombre");
      __publicField(this, "url_servidor");
      __publicField(this, "usa_cash_keeper");
      __publicField(this, "url_cash_keeper");
      __publicField(this, "usa_pinpad");
      __publicField(this, "url_pinpad");
      __publicField(this, "activa");
      this.uid = uid;
      this.id = id || Date.now();
      this.nombre = nombre;
      this.activa = activa;
      this.descripcion = descripcion;
      this.url_servidor = url_servidor;
      this.usa_cash_keeper = usa_cash_keeper;
      this.url_cash_keeper = url_cash_keeper;
      this.usa_pinpad = usa_pinpad;
      this.url_pinpad = url_pinpad;
    }
    validate() {
      if (!this.uid || !this.descripcion || !this.url_servidor) {
        throw new Error("UID, descripción y URL del servidor son obligatorios.");
      }
    }
  }
  __publicField(Empresa, "schema", "++id, uid, nombre, activa, descripcion, url_servidor, usa_cash_keeper, url_cash_keeper, usa_pinpad, url_pinpad");
  class Tecla {
    constructor({ ID, nombre, p1, p2, descripcion_t, descripcion_r, tag = null, orden = null, tipo = null, hay_existencias = 1, IDFamilia = 0, IDParentTecla = null, IDSeccion = 0, IDSeccionCom = 0, Precio = 0, RGB = "" }) {
      __publicField(this, "ID");
      __publicField(this, "nombre");
      __publicField(this, "IDFamilia", null);
      __publicField(this, "IDSeccion", null);
      __publicField(this, "IDSeccionCom", null);
      __publicField(this, "Precio", null);
      __publicField(this, "RGB", null);
      __publicField(this, "p1");
      __publicField(this, "p2");
      __publicField(this, "tag", null);
      __publicField(this, "orden", null);
      __publicField(this, "tipo", null);
      __publicField(this, "descripcion_t");
      __publicField(this, "descripcion_r");
      __publicField(this, "IDParentTecla", null);
      __publicField(this, "hay_existencias", 1);
      this.ID = ID || Date.now();
      this.nombre = nombre;
      this.p1 = p1;
      this.p2 = p2;
      this.IDFamilia = IDFamilia;
      this.IDSeccion = IDSeccion;
      this.IDSeccionCom = IDSeccionCom;
      this.Precio = Precio;
      this.RGB = RGB;
      this.tag = tag;
      this.orden = orden;
      this.tipo = tipo;
      this.descripcion_t = descripcion_t;
      this.descripcion_r = descripcion_r;
      this.IDParentTecla = IDParentTecla;
      this.hay_existencias = hay_existencias;
    }
  }
  __publicField(Tecla, "schema", "++ID, nombre, p1, p2, IDFamilia, IDParentTecla, IDSeccion, IDSeccionCom, Precio, RGB, tag, orden, tipo, descripcion_t, descripcion_r, hay_existencias");
  class Cuenta {
    constructor({ ID = null, Estado = "N", Descripcion, descripcion_t, Precio, IDMesa, IDArt = -1, camarero, nomMesa }) {
      __publicField(this, "ID");
      __publicField(this, "Estado");
      __publicField(this, "Descripcion");
      __publicField(this, "descripcion_t");
      __publicField(this, "Precio");
      __publicField(this, "IDPedido");
      __publicField(this, "IDMesa");
      __publicField(this, "IDArt");
      __publicField(this, "receptor");
      __publicField(this, "camarero");
      __publicField(this, "nomMesa");
      __publicField(this, "IDZona");
      __publicField(this, "servido");
      __publicField(this, "UID");
      this.ID = ID || Date.now();
      this.Estado = Estado;
      this.Descripcion = Descripcion;
      this.descripcion_t = descripcion_t;
      this.Precio = Precio;
      this.IDPedido = -1;
      this.IDMesa = IDMesa;
      this.IDArt = IDArt;
      this.receptor = -1;
      this.camarero = camarero;
      this.nomMesa = nomMesa;
      this.IDZona = -1;
      this.servido = 1;
      this.UID = Date.now().toString();
    }
  }
  __publicField(Cuenta, "schema", "++ID, Estado, Descripcion, Descripcion_t, Precio, IDPedido, IDMesa, IDArt, Receptor, Camarero, NomMesa, IDZona, Servido, UID");
  class Seccion {
    constructor({ id, nombre, icono = null }) {
      __publicField(this, "id");
      __publicField(this, "nombre");
      __publicField(this, "icono");
      this.id = id || Date.now();
      this.nombre = nombre;
      this.icono = icono;
    }
    validate() {
      if (!this.nombre) {
        throw new Error("El nombre es obligatorio.");
      }
    }
  }
  __publicField(Seccion, "schema", "++id, nombre, icono");
  class Sugerencia {
    constructor({ id, tecla, sugerencia, incremento = 0 }) {
      __publicField(this, "id");
      __publicField(this, "tecla");
      __publicField(this, "sugerencia");
      __publicField(this, "incremento");
      this.id = id;
      this.tecla = tecla;
      this.sugerencia = sugerencia;
      this.incremento = incremento;
    }
    validate() {
      if (!this.id || !this.tecla) {
        throw new Error("ID y Tecla son obligatorios.");
      }
    }
  }
  __publicField(Sugerencia, "schema", "++id, tecla, sugerencia, incremento");
  class Mesa {
    constructor({ ID, Nombre, RGB, abierta, IDZona, num, Orden, Tarifa }) {
      __publicField(this, "ID");
      __publicField(this, "Nombre");
      __publicField(this, "RGB");
      __publicField(this, "abierta");
      __publicField(this, "IDZona");
      __publicField(this, "num");
      __publicField(this, "Orden");
      __publicField(this, "Tarifa");
      this.ID = ID;
      this.Nombre = Nombre;
      this.RGB = RGB;
      this.abierta = abierta;
      this.IDZona = IDZona;
      this.num = num;
      this.Orden = Orden;
      this.Tarifa = Tarifa;
    }
  }
  __publicField(Mesa, "schema", "++ID, Nombre, RGB, abierta, IDZona, num, Orden, Tarifa");
  class Zona {
    constructor({ id, nombre, rgb, tarifa, icon = null }) {
      __publicField(this, "id");
      __publicField(this, "nombre");
      __publicField(this, "rgb");
      __publicField(this, "tarifa");
      __publicField(this, "icon");
      this.id = id;
      this.nombre = nombre;
      this.rgb = rgb;
      this.tarifa = tarifa;
      this.icon = icon;
    }
  }
  __publicField(Zona, "schema", "++id, nombre, rgb, tarifa, icon");
  class Instruction {
    constructor(url, endpoint, data) {
      __publicField(this, "id");
      __publicField(this, "url");
      __publicField(this, "endpoint");
      __publicField(this, "data");
      __publicField(this, "timestamp");
      // Para mejor trazabilidad
      __publicField(this, "attempts");
      this.url = url;
      this.endpoint = endpoint;
      this.data = data;
      this.timestamp = Date.now();
      this.attempts = 0;
    }
    // Para contar reintentos
    static get schema() {
      return "++id";
    }
  }
  const schemas = {
    camareros: Camarero.schema,
    empresas: Empresa.schema,
    teclas: Tecla.schema,
    lineaspedido: Cuenta.schema,
    seccionescom: Seccion.schema,
    sugerencias: Sugerencia.schema,
    mesas: Mesa.schema,
    zonas: Zona.schema,
    instructionQueue: Instruction.schema
  };
  class DB {
    constructor(dbName = "ValleTPVDatabase") {
      __publicField(this, "db");
      this.db = new Dexie(dbName);
      this.db.version(2).stores(schemas);
    }
    async add(table, data) {
      await this.db.table(table).add(data);
    }
    async getAll(table) {
      return await this.db.table(table).toArray();
    }
    async getById(table, id) {
      return await this.db.table(table).get(id);
    }
    async update(table, id, updates) {
      await this.db.table(table).update(id, updates);
    }
    async remove(table, id) {
      await this.db.table(table).delete(id);
    }
    async syncWithServer(table, uid, url_server) {
      const formData = new FormData();
      formData.append("tb", table);
      formData.append("reg", JSON.stringify(await this.getAll(table)));
      formData.append("uid", uid);
      try {
        const response = await fetch(`${url_server}/api/sync/sync_devices`, {
          method: "POST",
          body: formData
        });
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        if (Array.isArray(data) && data.length > 0) {
          await this.executeServerOperations(data);
          return true;
        }
        return false;
      } catch (error) {
        console.error(`Error al sincronizar la tabla ${table}:`, error);
        return false;
      }
    }
    async executeServerOperations(operations) {
      for (const operation of operations) {
        const { tb, op, obj } = operation;
        const id = obj.id || obj.ID;
        try {
          switch (op) {
            case "insert":
              await this.add(tb, obj);
              break;
            case "md":
              await this.update(tb, id, obj);
              break;
            case "rm":
              await this.remove(tb, id);
              break;
            default:
              console.warn(`Operación desconocida: ${op}`);
          }
        } catch (error) {
          console.error(`Error ejecutando operación ${op} en tabla ${tb}:`, error);
        }
      }
    }
  }
  const dbInstance = new DB();
  class InstructionQueue {
    constructor() {
      __publicField(this, "retryTimeouts", /* @__PURE__ */ new Map());
      __publicField(this, "MAX_QUICK_ATTEMPTS", 3);
      // Intentos rápidos para errores temporales
      __publicField(this, "MAX_TOTAL_ATTEMPTS", 50);
      // Máximo total antes de eliminar definitivamente
      __publicField(this, "BASE_DELAY", 5e3);
      // 5 segundos base
      __publicField(this, "MAX_DELAY", 3e5);
      // 5 minutos máximo entre reintentos
      __publicField(this, "LONG_DELAY", 6e4);
      // 1 minuto para reintentos largos
      __publicField(this, "retryTimeout", null);
      // Timeout único para reintentar toda la cola
      __publicField(this, "isProcessing", false);
      // Bandera para evitar procesamiento concurrente
      __publicField(this, "externalNotifyCallback");
    }
    // Callback alternativo
    // Método para configurar callback externo (usado desde WebSocketHandler)
    setExternalNotifyCallback(callback) {
      this.externalNotifyCallback = callback;
    }
    // Método para enviar mensaje al cliente principal
    async notifyClient(type, value) {
      try {
        if (this.externalNotifyCallback) {
          this.externalNotifyCallback(type, value);
          return;
        }
        if (typeof self !== "undefined" && "clients" in self) {
          const clients = await self.clients.matchAll();
          for (const client of clients) {
            client.postMessage({
              type: "instruction-queue-update",
              action: type,
              value
            });
          }
        } else {
          console.warn("⚠️ notifyClient llamado sin callback externo ni contexto de Service Worker");
        }
      } catch (error) {
        console.error("Error notificando al cliente:", error);
      }
    }
    // Ahora devuelve 'true' si se necesita sincronizar, 'false' si no.
    async addAndProcess(url, endpoint, data) {
      const pendingInstructions = await dbInstance.getAll("instructionQueue");
      if (pendingInstructions.length === 0) {
        const instruction = new Instruction(url, endpoint, data);
        const success = await this.executeInstruction(instruction);
        if (success) {
          return false;
        }
        await dbInstance.add("instructionQueue", instruction);
        await this.notifyClient("increment");
        await this.scheduleRetryForQueue();
        return true;
      } else {
        const instruction = new Instruction(url, endpoint, data);
        await dbInstance.add("instructionQueue", instruction);
        await this.notifyClient("increment");
        return true;
      }
    }
    // processQueueFromSync con procesamiento SECUENCIAL FIFO
    async processQueueFromSync() {
      if (this.isProcessing) return;
      this.isProcessing = true;
      try {
        const allInstructions = await dbInstance.getAll("instructionQueue");
        allInstructions.sort((a, b) => (a.id || 0) - (b.id || 0));
        for (let i = 0; i < allInstructions.length; i++) {
          const instruction = allInstructions[i];
          if (!instruction.id) continue;
          const success = await this.executeInstruction(instruction);
          if (success) {
            await dbInstance.remove("instructionQueue", instruction.id);
            if (this.retryTimeouts.has(instruction.id)) {
              clearTimeout(this.retryTimeouts.get(instruction.id));
              this.retryTimeouts.delete(instruction.id);
            }
            await this.notifyClient("decrement");
          } else {
            await this.scheduleRetryForQueue();
            break;
          }
        }
      } finally {
        this.isProcessing = false;
      }
    }
    // Programar reintento para toda la cola (nuevo enfoque secuencial)
    async scheduleRetryForQueue() {
      if (this.retryTimeout) {
        clearTimeout(this.retryTimeout);
        this.retryTimeout = null;
      }
      const pendingInstructions = await dbInstance.getAll("instructionQueue");
      const firstInstruction = pendingInstructions.sort((a, b) => (a.id || 0) - (b.id || 0))[0];
      if (!firstInstruction) return;
      const attempts = firstInstruction.attempts || 0;
      if (attempts >= this.MAX_TOTAL_ATTEMPTS) {
        for (const instr of pendingInstructions) {
          if (instr.id) {
            await dbInstance.remove("instructionQueue", instr.id);
            await this.notifyClient("decrement");
          }
        }
        return;
      }
      let delay;
      if (attempts <= this.MAX_QUICK_ATTEMPTS) {
        delay = this.BASE_DELAY * Math.pow(2, attempts);
      } else {
        delay = Math.min(this.LONG_DELAY * Math.pow(1.5, attempts - this.MAX_QUICK_ATTEMPTS), this.MAX_DELAY);
      }
      if (firstInstruction.id) {
        await dbInstance.update("instructionQueue", firstInstruction.id, { attempts: attempts + 1 });
      }
      this.retryTimeout = setTimeout(async () => {
        this.retryTimeout = null;
        await this.processQueueFromSync();
      }, delay);
    }
    // Programar reintento con estrategia inteligente
    async scheduleRetry(instruction) {
      if (!instruction.id) return;
      const attempts = instruction.attempts || 0;
      if (attempts >= this.MAX_TOTAL_ATTEMPTS) {
        console.error(`Instrucción #${instruction.id} alcanzó el límite máximo de ${this.MAX_TOTAL_ATTEMPTS} intentos. Eliminando de la cola.`);
        await dbInstance.remove("instructionQueue", instruction.id);
        await this.notifyClient("decrement");
        return;
      }
      let delay;
      if (attempts <= this.MAX_QUICK_ATTEMPTS) {
        delay = this.BASE_DELAY * Math.pow(2, attempts);
      } else {
        delay = Math.min(this.LONG_DELAY * Math.pow(1.5, attempts - this.MAX_QUICK_ATTEMPTS), this.MAX_DELAY);
      }
      if (this.retryTimeouts.has(instruction.id)) {
        clearTimeout(this.retryTimeouts.get(instruction.id));
      }
      const timeoutId = setTimeout(async () => {
        const success = await this.executeInstruction(instruction);
        if (success) {
          await dbInstance.remove("instructionQueue", instruction.id);
          await this.notifyClient("decrement");
          this.retryTimeouts.delete(instruction.id);
        } else {
          await dbInstance.update("instructionQueue", instruction.id, { attempts: instruction.attempts });
          await this.scheduleRetry(instruction);
        }
      }, delay);
      this.retryTimeouts.set(instruction.id, timeoutId);
    }
    // Método para inicializar el contador con las instrucciones pendientes
    async initializeCounter() {
      const allInstructions = await dbInstance.getAll("instructionQueue");
      await this.notifyClient("set", allInstructions.length);
      await this.resumePendingRetries();
    }
    // Reactivar reintentos para instrucciones que quedaron pendientes (ej: después de corte de luz)
    async resumePendingRetries() {
      const pendingInstructions = await dbInstance.getAll("instructionQueue");
      for (const instruction of pendingInstructions) {
        setTimeout(async () => {
          const success = await this.executeInstruction(instruction);
          if (success) {
            await dbInstance.remove("instructionQueue", instruction.id);
            await this.notifyClient("decrement");
          } else {
            await dbInstance.update("instructionQueue", instruction.id, { attempts: instruction.attempts });
            await this.scheduleRetry(instruction);
          }
        }, Math.random() * 5e3);
      }
    }
    // executeInstruction con análisis inteligente de errores
    async executeInstruction(instruction) {
      try {
        instruction.attempts = (instruction.attempts || 0) + 1;
        const { url, endpoint, data } = instruction;
        const formData = new FormData();
        for (const [key, value] of Object.entries(data)) {
          formData.append(key, String(value));
        }
        const response = await fetch(`${url}/${endpoint}`, {
          method: "POST",
          body: formData
        });
        if (response.ok) {
          return true;
        } else if (response.status === 400) {
          console.error(`❌ Instrucción #${instruction.id} datos malformados (400). Eliminando.`);
          await dbInstance.remove("instructionQueue", instruction.id);
          await this.notifyClient("decrement");
          return true;
        } else if (response.status === 401) {
          console.error(`❌ Instrucción #${instruction.id} no autorizado (401). Eliminando.`);
          await dbInstance.remove("instructionQueue", instruction.id);
          await this.notifyClient("decrement");
          return true;
        } else if (response.status === 403) {
          console.error(`❌ Instrucción #${instruction.id} acceso denegado (403). Eliminando.`);
          await dbInstance.remove("instructionQueue", instruction.id);
          await this.notifyClient("decrement");
          return true;
        } else if (response.status === 404) {
          console.error(`❌ Instrucción #${instruction.id} endpoint no encontrado (404). Eliminando.`);
          await dbInstance.remove("instructionQueue", instruction.id);
          await this.notifyClient("decrement");
          return true;
        } else if (response.status === 409) {
          console.error(`❌ Instrucción #${instruction.id} conflicto (409). Eliminando.`);
          await dbInstance.remove("instructionQueue", instruction.id);
          await this.notifyClient("decrement");
          return true;
        } else if (response.status === 422) {
          console.error(`❌ Instrucción #${instruction.id} validación fallida (422). Eliminando.`);
          await dbInstance.remove("instructionQueue", instruction.id);
          await this.notifyClient("decrement");
          return true;
        } else if (response.status >= 400 && response.status < 500) {
          console.error(`❌ Instrucción #${instruction.id} error del cliente (${response.status}). Eliminando.`);
          await dbInstance.remove("instructionQueue", instruction.id);
          await this.notifyClient("decrement");
          return true;
        } else if (response.status === 500) {
          console.error(`❌ Instrucción #${instruction.id} error interno del servidor (500). Eliminando - requiere corrección del código.`);
          await dbInstance.remove("instructionQueue", instruction.id);
          await this.notifyClient("decrement");
          return true;
        } else if (response.status === 502) {
          console.warn(`🔄 Instrucción #${instruction.id} gateway no disponible (502) - intento ${instruction.attempts}. Reintentando...`);
          return false;
        } else if (response.status === 503) {
          console.warn(`🔄 Instrucción #${instruction.id} servicio no disponible (503) - intento ${instruction.attempts}. Reintentando...`);
          return false;
        } else if (response.status === 504) {
          console.warn(`🔄 Instrucción #${instruction.id} timeout del gateway (504) - intento ${instruction.attempts}. Reintentando...`);
          return false;
        } else if (response.status >= 500) {
          console.warn(`🔄 Instrucción #${instruction.id} error del servidor (${response.status}) - intento ${instruction.attempts}. Reintentando...`);
          return false;
        } else {
          console.warn(`❓ Instrucción #${instruction.id} código de estado desconocido (${response.status}) - intento ${instruction.attempts}. Reintentando...`);
          return false;
        }
      } catch (error) {
        console.warn(`🌐 Instrucción #${instruction.id} error de red (intento ${instruction.attempts || 1}). Reintentando...`, error);
        return false;
      }
    }
    // Limpiar todos los timeouts pendientes
    clearAllRetries() {
      for (const [id, timeoutId] of this.retryTimeouts) {
        clearTimeout(timeoutId);
      }
      this.retryTimeouts.clear();
    }
  }
  const instructionQueue = new InstructionQueue();
  instructionQueue.initializeCounter().catch((error) => {
    console.error("Error inicializando contador de instrucciones:", error);
  });
  self.addEventListener("message", (event) => {
    const { url, endpoint, data } = event.data;
    const work = async () => {
      const needsSync = await instructionQueue.addAndProcess(url, endpoint, data);
      if (needsSync && self.registration.sync) {
        await self.registration.sync.register("instrucciones-sync");
      }
    };
    event.waitUntil(work());
  });
  self.addEventListener("sync", (event) => {
    if (event.tag === "instrucciones-sync") {
      event.waitUntil(instructionQueue.processQueueFromSync());
    }
  });
  self.addEventListener("activate", (event) => {
    event.waitUntil(self.clients.claim());
  });
  self.addEventListener("install", (event) => {
    self.skipWaiting();
  });
})();
