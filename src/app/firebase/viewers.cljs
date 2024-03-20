(ns app.firebase.viewers
  (:require [app.firebase.core :refer [filter-change remove-document! update-document! watch-collection!]]))

(defn- viewers-path [room-id]
  (str "rooms/" room-id "/viewers"))

(defn- viewer-path [room-id user-id]
  (str (viewers-path room-id) "/" user-id))

(defn update-viewer!
  "Update or create a viewer's data."
  [{:keys [room-id id] :as data}]
  (update-document! (viewer-path room-id id) data))

(defn remove-viewer!
  "Remove a viewer from a room."
  [{:keys [room-id id]}]
  (remove-document! (viewer-path room-id id)))

(defn watch-viewers!
  "Attach a handler for when a viewer joins.
   Returns a function for unsubscribing the handler."
  [room-id type handler]
  (watch-collection!
   (viewers-path room-id)
   #(doseq [viewer (filter-change % type)]
      (handler viewer))))