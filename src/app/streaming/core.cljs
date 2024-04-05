(ns app.streaming.core
  (:require [app.firebase.core :refer [initialize-firebase user]]
            [app.streaming.owner :as owner]
            [app.streaming.viewer :as viewer]
            [app.domain.rooms :as rooms]
            [app.streaming.video :as video]
            [app.utils :refer [get-query-params navigate  set-hidden!]]))

(defn- show-room []
  (let [loading-section (.getElementById js/document "loading")
        room-wrapper (.getElementById js/document "room-wrapper")]
    (set-hidden! loading-section true)
    (set-hidden! room-wrapper false)))

(defn- setup-room
  "Attach event listeners to video player and setup owner or viewer."
  [{:keys [owner id]}]
  (let [video-player   (.getElementById js/document "video-player")
        status-section (.getElementById js/document "status-section")]

    (show-room)

    (video/add-on-playing! video-player #(set-hidden! status-section true))
    (video/add-on-ended! video-player #(set-hidden! status-section false))

    (if (= owner @user)
      (owner/init id video-player status-section)
      (viewer/init id video-player))))

(defn- setup-or-navigate [room]
  (if room
    (setup-room room)
    (navigate "/")))

(defn ^:dev/after-load init []
  (if-let [room-id (:room-id (get-query-params))]
    (initialize-firebase #(-> (rooms/get-room room-id) (.then setup-or-navigate)))
    (navigate "/")))