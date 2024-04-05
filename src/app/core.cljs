(ns app.core
  (:require [app.firebase.core :refer [initialize-firebase user]]
            [app.domain.rooms :as rooms]
            [app.utils :refer [navigate]]))

(def create-room-button (.getElementById js/document "create-room-button"))
(def join-room-button (.getElementById js/document "join-room-button"))

(defn- set-loading [element loading & {:keys [message]}]
  (.setAttribute element "aria-busy" loading)
  (.setAttribute element "aria-label" message))

(defn- navigate-to-new-room  []
  (-> (rooms/create-room! @user)
      (.then #(navigate "/watch" {:room-id %}))))

(defn- handle-create-room! []
  (set-loading create-room-button true :message "Creating room...")
  (-> (initialize-firebase navigate-to-new-room)
      (.catch #(set-loading create-room-button false))))

(defn- handle-join-room! []
  (println "Joining room..."))

(defn ^:dev/after-load init []
  (.addEventListener create-room-button "click" handle-create-room!)
  (.addEventListener join-room-button "click" handle-join-room!))

(defn ^:dev/before-load stop
  "Used in development to reset state for hot-reloading"
  []
  (.removeEventListener create-room-button "click" handle-create-room!)
  (.removeEventListener join-room-button "click" handle-join-room!))
