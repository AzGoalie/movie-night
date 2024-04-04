(ns app.streaming.viewer
  (:require [app.firebase.core :refer [user]]
            [app.firebase.signaling :refer [FirebaseCallee]]
            [app.domain.viewers :as viewers]
            [app.webrtc.peer-connection :as pc]
            [app.webrtc.signaling :refer [send-answer watch-offer watch-ice-candidate]]
            [app.streaming.video :as video]))

(def state
  (atom {:connection nil
         :subscriptions []}))

(defn- add-subscription! [subscription]
  (swap! state update :subscriptions conj subscription))

(defn- remove-subscriptions! []
  (doseq [unsubscribe (:subscriptions @state)] (unsubscribe))
  (swap! state assoc :subscriptions []))

(defn- handle-add-track [stream]
  (fn [event]
    (doseq [track (.getTracks (first (.-streams event)))]
      (js/console.log track)
      (.addTrack stream track))))

(defn- handle-offer [connection callee]
  (fn [offer]
    (pc/set-remote-description connection offer "offer")
    (-> (pc/create-answer connection)
        (.then #(send-answer callee %)))))

(defn- handle-ice-candidate [connection]
  (fn [candidate]
    (.addIceCandidate connection (js/RTCIceCandidate. (clj->js candidate)))))

(defn init [room-id video-player]
  (let [stream     (js/MediaStream.)
        viewer     (viewers/create-viewer! room-id @user)
        callee     (FirebaseCallee. (viewers/viewer-path viewer))
        connection (pc/create-peer-connection callee (handle-add-track stream))]

    (video/add-stream! video-player stream)

    (add-subscription! (watch-offer callee (handle-offer connection callee)))
    (add-subscription! (watch-ice-candidate callee (handle-ice-candidate connection)))
    (swap! state assoc :connection connection)))

(defn ^:dev/before-load stop
  "Used in development to reset state for hot-reloading"
  []
  (remove-subscriptions!))