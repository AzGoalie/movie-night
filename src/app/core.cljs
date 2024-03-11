(ns app.core
  (:require [app.firebase.core :refer [user]]
            [app.firebase.room :as room]
            [app.utils :refer [navigate]]))

(defn- get-create-room-button []
  (.getElementById js/document "create-room-button"))

(defn- get-join-room-button []
  (.getElementById js/document "join-room-button"))

(defn- handle-create-room! []
  (-> (room/create-room @user)
      (.then #(navigate "/watch" {:roomId (.-id %)}))))

(defn- handle-join-room! []
  (println "Joining room..."))

(defn ^:dev/after-load init []
  (let [create-room-button (get-create-room-button)
        join-room-button   (get-join-room-button)]
    (.addEventListener create-room-button "click" handle-create-room!)
    (.addEventListener join-room-button "click" handle-join-room!)))

(defn ^:dev/before-load stop
  "Used in development to reset state for hot-reloading"
  []
  (let [create-room-button (get-create-room-button)
        join-room-button   (get-join-room-button)]
    (.removeEventListener create-room-button "click" handle-create-room!)
    (.removeEventListener join-room-button "click" handle-join-room!)))
