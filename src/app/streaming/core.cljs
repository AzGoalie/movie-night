(ns app.streaming.core
  (:require [app.firebase.core :refer [initialize-firebase user]]
            [app.firebase.rooms :as rooms]
            [app.firebase.viewers :as viewers]
            [app.streaming.video :as video]
            [app.utils :refer [create-button get-query-params navigate]]))

(def state (atom {}))
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

(defn- handle-new-viewer [{:keys [id] :as v}]
  (println "A viewer joind with ID:" id)
  (viewers/remove-viewer! v))

(defn- setup-owner [room-id]
  (println "I am the owner")
  (add-stream-button!)
  (video/mute! video-player)
  (swap! state update :subscriptions conj (viewers/watch-viewers! room-id "added" handle-new-viewer)))

(defn- setup-viewer [room-id]
  (println "I am a viewer")
  (viewers/update-viewer! {:id @user :room-id room-id}))

(defn- setup-room
  "Attach event listeners to video player and setup owner or viewer."
  [{:keys [owner id]}]
  (video/add-on-playing! video-player #(set-hidden! status-section true))
  (video/add-on-ended! video-player #(set-hidden! status-section false))

  (if (= owner @user)
    (setup-owner id)
    (setup-viewer id)))

(defn- setup-or-navigate [room]
  (if room
    (setup-room room)
    (navigate "/")))

(defn ^:dev/after-load init []
  (if-let [room-id (:room-id (get-query-params))]
    (initialize-firebase #(-> (rooms/get-room room-id) (.then setup-or-navigate)))
    (navigate "/")))

(defn ^:dev/before-load stop
  "Used in development to reset state for hot-reloading"
  []
  (when-let [capture-button (.getElementById js/document "capture-button")]
    (.remove capture-button))
  (doseq [unsubscribe (:subscriptions @state)] (unsubscribe)))
