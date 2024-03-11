(ns app.streaming.core
  (:require [app.firebase.core :refer [user]]
            [app.firebase.rooms :as rooms]
            [app.firebase.viewers :as viewers]
            [app.streaming.video :as video]
            [app.utils :refer [create-button get-query-params navigate]]))

(defonce state (atom {:subscriptions []}))
(def status-section (.getElementById js/document "status-section"))
(def video-player (.getElementById js/document "video-player"))

(defn- set-hidden! [element value]
  (set! (.-hidden element) value))

(defn- handle-start-stream [stream]
  (video/add-stream! video-player stream))

(defn- add-stream-button! []
  (let [attributes     {:textContent "Share your screen"
                        :id          "capture-button"}
        on-click       #(video/request-stream! handle-start-stream)
        capture-button (create-button attributes  on-click)]
    (.appendChild status-section capture-button)))

(defn- handle-new-viewer [room-id viewer]
  (println "A viewer joined")
  (viewers/remove-viewer room-id viewer))

(defn- setup-owner [room-id]
  (println "I am the owner")
  (let [handler         #(handle-new-viewer room-id %)
        wrapped-handler #(viewers/filter-new-viewers % handler)]
    (add-stream-button!)
    (video/mute! video-player)
    (swap! state update :subscriptions conj (viewers/watch-viewers! room-id wrapped-handler))))

(defn- setup-viewer [room-id]
  (println "I am a viewer")
  (viewers/add-viewer room-id @user {:hello "world"}))

(defn- setup-room
  "Attach event listeners to video player and setup owner or viewer."
  [{:keys [owner room-id]}]
  (video/add-on-playing! video-player #(set-hidden! status-section true))
  (video/add-on-ended! video-player #(set-hidden! status-section false))

  (if (= owner @user)
    (setup-owner room-id)
    (setup-viewer room-id)))

(defn- setup-or-navigate [room]
  (if-let [data (rooms/get-room-data room)]
    (setup-room data)
    (navigate "/")))

(defn ^:dev/after-load init []
  (let [room-id (:roomId (get-query-params))]
    (if (nil? room-id)
      (navigate "/")
      (-> (rooms/get-room room-id)
          (.then setup-or-navigate)
          (.catch #(navigate "/"))))))

(defn ^:dev/before-load stop
  "Used in development to reset state for hot-reloading"
  []
  (.remove (.getElementById js/document "capture-button"))
  (doseq [unsubscribe (:subscriptions @state)] (unsubscribe))
  (swap! state assoc :subscriptions []))
  