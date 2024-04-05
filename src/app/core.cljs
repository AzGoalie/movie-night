(ns app.core
  (:require [app.firebase.core :refer [initialize-firebase user]]
            [app.domain.rooms :as rooms]
            [app.utils :refer [navigate]]))

(defn- set-loading [element loading & {:keys [message]}]
  (.setAttribute element "aria-busy" loading)
  (.setAttribute element "aria-label" message))

(defn- navigate-to-new-room  []
  (-> (rooms/create-room! @user)
      (.then #(navigate "/watch" {:room-id %}))))

(defn ^:export handle-create-room []
  (let [create-room-button (.getElementById js/document "create-room-button")]
    (set-loading create-room-button true :message "Creating room...")
    (-> (initialize-firebase)
        (.then navigate-to-new-room)
        (.catch #(set-loading create-room-button false)))))