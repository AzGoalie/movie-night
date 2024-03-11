(ns app.firebase.room
  (:require [app.firebase.core :refer [firestore]]
            ["firebase/firestore" :refer [addDoc collection doc getDoc getDocs]]))

(defn get-rooms
  "Returns a js/Promise of a firebase collection of rooms."
  []
  (getDocs (collection firestore "rooms")))

(defn get-room
  "Returns js/Promise of a firebase document of a room specified at the given ID."
  [id]
  (getDoc (doc firestore "rooms" id)))

(defn create-room
  "Creates a new room with a random ID and owned by the specified user ID.
   Returns a js/Promise of the newly created room."
  [owner]
  (addDoc (collection firestore "rooms") #js {:owner owner}))

(defn get-room-data
  "If the room exists, returns it's data as a map"
  [room]
  (when (.exists room)
    (assoc
     (js->clj (.data room) :keywordize-keys true)
     :room-id (.-id room))))