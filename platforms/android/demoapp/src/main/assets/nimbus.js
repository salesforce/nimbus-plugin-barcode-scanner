(function() {
  "use strict";

  //
  // Copyright (c) 2019, Salesforce.com, inc.
  // All rights reserved.
  // SPDX-License-Identifier: BSD-3-Clause
  // For full license text, see the LICENSE file in the repo root or
  // https://opensource.org/licenses/BSD-3-Clause
  //
  class Nimbus {
    constructor() {
      // There can be many promises so creating a storage for later look-up.
      this.promises = {};
      this.callbacks = {};
      // Dictionary to manage message&subscriber relationship.
      this.listenerMap = {};
      // influenced from
      // https://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
      this.uuidv4 = () => {
        return "10000000-1000-4000-8000-100000000000".replace(/[018]/g, c => {
          const asNumber = Number(c);
          return (
            asNumber ^
            (crypto.getRandomValues(new Uint8Array(1))[0] &
              (15 >> (asNumber / 4)))
          ).toString(16);
        });
      };
      this.promisify = src => {
        let dest = {};
        Object.keys(src).forEach(key => {
          let func = src[key];
          dest[key] = (...args) => {
            args = this.cloneArguments(args);
            args = args.map(arg => {
              if (typeof arg === "object") {
                return JSON.stringify(arg);
              }
              return arg;
            });
            let result = func.call(src, ...args);
            if (result !== undefined) {
              result = JSON.parse(result);
            }
            return Promise.resolve(result);
          };
        });
        return dest;
      };
      this.cloneArguments = args => {
        let clonedArgs = [];
        for (var i = 0; i < args.length; ++i) {
          if (typeof args[i] === "function") {
            const callbackId = this.uuidv4();
            this.callbacks[callbackId] = args[i];
            // TODO: this should generalize better, perhaps with an explicit platform
            // check?
            if (
              typeof _nimbus !== "undefined" &&
              _nimbus.makeCallback !== undefined
            ) {
              // TODO: Android passes only the callbackId string, whereas iOS passes an
              // object with the callbackId property. These need to be merged and handled
              // the same way to eliminate extraneous code paths
              clonedArgs.push(callbackId);
            } else {
              clonedArgs.push({ callbackId });
            }
          } else {
            clonedArgs.push(args[i]);
          }
        }
        return clonedArgs;
      };
      this.callCallback = (callbackId, args) => {
        if (this.callbacks[callbackId]) {
          this.callbacks[callbackId](...args);
        }
      };
      // TODO: This version is called by Android, callCallback is called by iOS. The
      // two need to be consolidated.
      this.callCallback2 = (callbackId, ...args) => {
        this.callCallback(callbackId, args);
      };
      this.releaseCallback = callbackId => {
        delete this.callbacks[callbackId];
      };
      // Native side will callback this method. Match the callback to stored promise
      // in the storage
      this.resolvePromise = (promiseUuid, data, error) => {
        if (error) {
          this.promises[promiseUuid].reject(data);
        } else {
          this.promises[promiseUuid].resolve(data);
        }
        // remove reference to stored promise
        delete this.promises[promiseUuid];
      };
      /**
       * Broadcast a message to subscribed listeners.  Listeners
       * can receive data associated with the message for more
       * processing.
       *
       * @param message String message that is uniquely
       *     registered as a key in the
       *                listener map.  Multiple listeners can
       * get triggered from a message.
       * @param arg Swift encodable type.
       * @return Number of listeners that were called by the
       *     message.
       */
      this.broadcastMessage = (message, arg) => {
        let messageListeners = this.listenerMap[message];
        var handlerCallCount = 0;
        if (messageListeners) {
          messageListeners.forEach(listener => {
            if (arg) {
              listener(arg);
            } else {
              listener();
            }
            handlerCallCount++;
          });
        }
        return handlerCallCount;
      };
      /**
       * Subscribe a listener to message.
       *
       * @param message String message that is uniquely registered as a key
       *     in the
       *                listener map.  Multiple listeners can get triggered
       * from a message.
       * @param listener A method that should be triggered when a message is
       *     broadcasted.
       */
      this.subscribeMessage = (message, listener) => {
        let messageListeners = this.listenerMap[message];
        if (!messageListeners) {
          messageListeners = [];
        }
        messageListeners.push(listener);
        this.listenerMap[message] = messageListeners;
      };
      /**
       * Unsubscribe a listener from a message. Unsubscribed listener
       * will not be triggered.
       *
       * @param message String message that is uniquely registered as a
       *     key in the
       *                listener map.  Multiple listeners can get
       * triggered from a message.
       * @param listener A method that should be triggered when a
       *     message is broadcasted.
       */
      this.unsubscribeMessage = (message, listener) => {
        let messageListeners = this.listenerMap[message];
        if (messageListeners) {
          let counter = 0;
          let found = false;
          for (counter; counter < messageListeners.length; counter++) {
            if (messageListeners[counter] === listener) {
              found = true;
              break;
            }
          }
          if (found) {
            messageListeners.splice(counter, 1);
            this.listenerMap[message] = messageListeners;
          }
        }
      };
      if (
        typeof _nimbus !== "undefined" &&
        _nimbus.nativeExtensionNames !== undefined
      ) {
        // we're on Android, need to wrap native extension methods
        let extensionNames = JSON.parse(_nimbus.nativeExtensionNames());
        extensionNames.forEach(extension => {
          Object.assign(window, {
            [extension]: this.promisify(window[`_${extension}`])
          });
        });
      }
    }
  }
  const nimbus = new Nimbus();
  window.nimbus = nimbus;

  return nimbus;
})();
