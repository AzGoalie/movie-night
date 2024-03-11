(ns app.firebase.viewers
  (:require [app.firebase.core :refer [firestore]]
            ["firebase/firestore" :refer [collection deleteDoc doc getDocs onSnapshot setDoc]]))

(defn get-viewers
  "Returns a js/Promise of a firebase collection of viewers for a given room ID."
  [room-id]
  (getDocs (collection firestore "rooms" room-id "viewers")))

(defn add-viewer
  "Add a viewer to a room."
  [room-id user-id data]
  (setDoc (doc firestore "rooms" room-id "viewers" user-id) (clj->js data)))

(defn remove-viewer
  "Remove a viewer from a room."
  [room-id viewer]
  (deleteDoc (doc firestore "rooms" room-id "viewers" (.-id viewer))))

(defn filter-new-viewers [snapshot handler]
  (.. snapshot (docChanges) (forEach #(when (= (.-type %) "added")
                                        (handler (.-doc %))))))

(defn watch-viewers!
  "Attach a handler for changes to the viewer collection of the room.
   Returns a function for removing the handler."
  [room-id handler]
  (onSnapshot (collection firestore "rooms" room-id "viewers") handler))