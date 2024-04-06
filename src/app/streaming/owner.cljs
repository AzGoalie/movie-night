(ns app.streaming.owner
  (:require [app.domain.viewers :as viewers]
            [app.streaming.video :as video]
            [app.firebase.signaling :refer [FirebaseCaller]]
            [app.webrtc.signaling :refer [send-offer watch-answer watch-ice-candidate]]
            [app.webrtc.peer-connection :as pc]
            [app.utils :refer [create-button remove-element]]))

(def state
  (atom {:connections   []
         :subscriptions []}))

(defn- add-subscription! [subscription]
  (swap! state update :subscriptions conj subscription))

(defn- remove-subscriptions! []
  (doseq [unsubscribe (:subscriptions @state)] (unsubscribe))
  (swap! state assoc :subscriptions []))

(defn- add-connection! [connection]
  (swap! state update :connections conj connection))

(defn- handle-ice-candidate [connection]
  #(.addIceCandidate connection (js/RTCIceCandidate. (clj->js %))))

(defn- handle-answer [connection]
  #(pc/set-remote-description connection % "answer"))

(defn- handle-new-viewer [viewer stream]
  (let [caller     (FirebaseCaller. (viewers/viewer-path viewer))
        connection (pc/create-peer-connection caller #(println "Callees shouldn't be adding tracks..."))]
    (doseq [track (.getTracks stream)] (.addTrack connection track stream))
    (-> (pc/create-offer connection)
        (.then #(send-offer caller %))
        (.then #(add-subscription! (watch-ice-candidate caller (handle-ice-candidate connection))))
        (.then #(add-subscription! (watch-answer caller (handle-answer connection))))
        (.then #(add-connection! connection)))))

(defn- handle-stream [room-id video-player]
  (fn [stream]
    (video/add-stream! video-player stream)
    (add-subscription! (viewers/watch-viewers! room-id #(handle-new-viewer % stream)))))

(defn- create-capture-button [room-id video-player]
  (let [attributes {:id          "capture-button"
                    :textContent "Share your screen"}
        handler    (handle-stream room-id video-player)]
    (create-button attributes #(video/request-stream! handler))))

(defn init [room-id video-player status-section]
  (.appendChild status-section (create-capture-button room-id video-player))
  (video/mute! video-player)
  (video/add-on-ended! video-player remove-subscriptions!))

(defn ^:dev/before-load stop
  "Used in development to reset state for hot-reloading"
  []
  (remove-element "capture-button")
  (remove-subscriptions!))