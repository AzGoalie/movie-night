(ns app.firebase.rooms
  (:require [app.firebase.core :refer [create-document! get-document]]))

(defn get-room
  "Returns a js/Promise of a map containing the room data specified at the given ID."
  [id]
  (-> (get-document (str "rooms/" id))))

(defn create-room!
  "Creates a new room with a random ID and owned by the specified user ID.
   Returns a js/Promise of the room id."
  [owner-id]
  (create-document! "rooms" {:owner owner-id}))