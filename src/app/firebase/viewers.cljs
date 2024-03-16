(ns app.firebase.viewers
  (:require [app.firebase.core :refer [doc->map filter-snapshot firestore]]
            ["firebase/firestore" :refer [collection deleteDoc doc getDocs onSnapshot setDoc updateDoc]]))

(defn- viewer->map [room-id viewer]
  (when-let [data (doc->map viewer)]
    (assoc data
           :room-id room-id
           :viewer-id (.-id viewer))))

(defn get-viewers
  "Returns a js/Promise of a lazy-sequence of viewers for a given room ID."
  [room-id]
  (-> (collection firestore "rooms" room-id "viewers")
      (getDocs)
      (.then #(map (fn [viewer] (viewer->map room-id viewer)) (.-docs %)))))

(defn add-viewer!
  "Add a viewer to a room."
  [room-id user-id data]
  (setDoc (doc firestore "rooms" room-id "viewers" user-id) (clj->js data)))

(defn update-viewer!
  "Update the viewer's data"
  [room-id user-id data]
  (updateDoc (doc firestore "rooms" room-id "viewers" user-id) (clj->js data)))

(defn remove-viewer!
  "Remove a viewer from a room."
  [room-id viewer]
  (deleteDoc (doc firestore "rooms" room-id "viewers" (.-id viewer))))

(defn watch-new-viewers!
  "Attach a handler for when a viewer joins.
   Returns a function for unsubscribing the handler."
  [room-id handler]
  (onSnapshot (collection firestore "rooms" room-id "viewers")
              #(doseq [v (filter-snapshot % "added")]
                 (handler (viewer->map room-id v)))))

(defn watch-modified-viewers!
  "Attach a handler for when a viewer is modified.
   Returns a function for unsubscribing the handler."
  [room-id handler]
  (onSnapshot (collection firestore "rooms" room-id "viewers")
              #(doseq [v (filter-snapshot % "modified")]
                 (handler (viewer->map room-id v)))))

(defn watch-viewer!
  "Attach a handler for changes to the viewer id.
   Returns a function for unsubscribing the handler."
  [room-id user-id handler]
  (onSnapshot (doc firestore "rooms" room-id "viewers" user-id)
              #(handler (viewer->map room-id %))))
