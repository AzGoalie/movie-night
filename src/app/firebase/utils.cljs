(ns app.firebase.utils
  (:require [app.firebase.core :refer [firestore user]]
            ["firebase/firestore" :refer [addDoc collection deleteDoc doc getDoc onSnapshot setDoc]]))

(defn get-viewers [room-id]
  (collection firestore "rooms" room-id "viewers"))

(defn add-viewer [room-id data]
  (setDoc (doc firestore "rooms" room-id "viewers" @user) (clj->js data)))

(defn remove-viewer [room-id user-id]
  (deleteDoc (doc firestore "rooms" room-id "viewers" user-id)))

(defn on-snapshot [doc handler]
  (onSnapshot doc handler))