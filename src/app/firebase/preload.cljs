(ns app.firebase.preload
  (:require [app.firebase.core :refer [auth firestore]]
            ["firebase/auth" :refer [connectAuthEmulator]]
            ["firebase/firestore" :refer [connectFirestoreEmulator]]))

(connectAuthEmulator auth "http://localhost:9099")
(connectFirestoreEmulator firestore "localhost" 8080)