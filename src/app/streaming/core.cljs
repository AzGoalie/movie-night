(ns app.streaming.core
  (:require [app.firebase.core :refer [user]]
            [app.firebase.utils :refer [add-viewer get-viewers on-snapshot remove-viewer]]
            [app.streaming.video :as video]
            [app.utils :refer [create-button get-query-params navigate]]
            [app.firebase.room :as room]))

(defonce state (atom {:subscriptions []}))
(def status-section (.getElementById js/document "status-section"))
(def video-player (.getElementById js/document "video-player"))

(defn- handle-start-stream [stream]
  (video/add-on-stream-ended! stream #(video/reset-video-player! video-player))
  (video/add-stream! video-player stream))

(defn- add-stream-button! []
  (let [handler        #(handle-start-stream %)
        attributes     {:textContent "Share your screen"
                        :id          "capture-button"}
        capture-button (create-button attributes  #(video/request-stream! handler))]
    (video/mute! video-player)
    (.appendChild status-section capture-button)))

(defn- handle-new-viewer [room-id viewer]
  (println "A viewer joined")
  (remove-viewer room-id (.-id viewer)))

(defn- filter-new-viewers [room-id snapshot]
  (.forEach (.docChanges snapshot)
            #(when (= (.-type %) "added")
               (handle-new-viewer room-id (.-doc %)))))

(defn- setup-owner [room-id]
  (println "I am the owner")
  (let [viewers (get-viewers room-id)
        handler #(filter-new-viewers room-id %)]
    (add-stream-button!)
    (swap! state update :subscriptions conj (on-snapshot viewers handler))))

(defn- setup-viewer [room-id]
  (println "I am a viewer")
  (add-viewer room-id {:hello "world"}))

(defn- setup-room
  "Attach event listeners. If the room owner, add the capture button."
  [room]
  (let [{:keys [owner room-id]} (room/get-room-data room)]
    (.addEventListener video-player "ended" #(.remove (.-classList status-section) "hidden"))
    (.addEventListener video-player "playing" #(.add (.-classList status-section) "hidden"))

    (if (= owner @user)
      (setup-owner room-id)
      (setup-viewer room-id))))

(defn ^:dev/after-load init []
  (let [room-id (:roomId (get-query-params))]
    (if (nil? room-id)
      (navigate "/")
      (-> (room/get-room room-id)
          (.then setup-room)
          (.catch #(navigate "/"))))))

(defn ^:dev/before-load stop
  "Used in development to reset state for hot-reloading"
  []
  (.remove (.getElementById js/document "capture-button"))
  (doseq [unsubscribe (:subscriptions @state)] (unsubscribe))
  (swap! state assoc :subscriptions []))
  