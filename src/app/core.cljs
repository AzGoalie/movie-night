(ns app.core
  (:require [app.firebase.core :refer [initialize-firebase user]]
            [app.firebase.rooms :as rooms]
            [app.utils :refer [navigate]]))

(def create-room-button (.getElementById js/document "create-room-button"))
(def join-room-button (.getElementById js/document "join-room-button"))

(defn- handle-create-room! []
  (-> (rooms/create-room! @user)
      (.then #(navigate "/watch" {:room-id %}))))

(defn- handle-join-room! []
  (println "Joining room..."))

(defn setup-buttons []
  (.addEventListener create-room-button "click" handle-create-room!)
  (.addEventListener join-room-button "click" handle-join-room!))

(defn ^:dev/after-load init []
  (initialize-firebase setup-buttons))

(defn ^:dev/before-load stop
  "Used in development to reset state for hot-reloading"
  []
  (.removeEventListener create-room-button "click" handle-create-room!)
  (.removeEventListener join-room-button "click" handle-join-room!))
