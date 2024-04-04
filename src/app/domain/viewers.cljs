(ns app.domain.viewers
  (:require [app.firebase.firestore :refer [filter-change remove-document! update-document! watch-collection!]]))

(defn viewers-path [room-id]
  (str "rooms/" room-id "/viewers"))

(defn viewer-path [{:keys [id room-id]}]
  (str (viewers-path room-id) "/" id))

(defn update-viewer!
  "Update or create a viewer's data."
  [viewer]
  (update-document! (viewer-path viewer) (dissoc viewer :room-id)))

(defn create-viewer!
  "Create a new viewer."
  [room-id user-id]
  (let [viewer {:id user-id :room-id room-id}]
    (update-viewer! viewer)
    viewer))

(defn remove-viewer!
  "Remove a viewer from a room."
  [viewer]
  (remove-document! (viewer-path viewer)))

(defn watch-viewers!
  "Attach a handler for when a viewer joins.
   Returns a function for unsubscribing the handler."
  [room-id handler]
  (watch-collection!
   (viewers-path room-id)
   #(doseq [viewer (filter-change % "added")]
      (handler (assoc viewer :room-id room-id)))))