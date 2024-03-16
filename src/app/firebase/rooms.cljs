(ns app.firebase.rooms
  (:require [app.firebase.core :refer [doc->map firestore]]
            ["firebase/firestore" :refer [addDoc collection doc getDoc getDocs]]))

(defn- room->map
  [room]
  (when-let [data (doc->map room)]
    (assoc data :room-id (.-id room))))

(defn get-rooms
  "Returns a js/Promise of a lazy-sequence of rooms."
  []
  (-> (collection firestore "rooms")
      (getDocs)
      (.then #(map room->map (.-docs %)))))

(defn get-room
  "Returns a js/Promise of a room specified at the given ID."
  [id]
  (-> (doc firestore "rooms" id)
      (getDoc)
      (.then room->map)))

(defn create-room!
  "Creates a new room with a random ID and owned by the specified user ID.
   Returns a js/Promise of the room id."
  [owner-id]
  (-> (collection firestore "rooms")
      (addDoc #js {:owner owner-id})
      (.then #(.-id %))))