(ns app.webrtc.peer-connection
  (:require [app.webrtc.signaling :refer [send-candidate]]))

(def ice-config
  {:iceServers           [{:urls ["stun:stun1.l.google.com:19302"
                                  "stun:stun2.l.google.com:19302"]}]
   :iceCandidatePoolSize 10})

(defn- create-negotiateion-needed-handler []
  (fn [event]
    (println "on-negotiation-needed")))

(defn- handle-ice-candidate [event ice-handler]
  (when-let [candidate (.-candidate event)]
    (send-candidate ice-handler candidate)))

(defn create-peer-connection
  "Create a new js/RTCPeerConnection and attach event listeners."
  [ice-handler handle-add-track]
  (doto (js/RTCPeerConnection. (clj->js ice-config))
    (.addEventListener "icecandidate" #(handle-ice-candidate % ice-handler))
    (.addEventListener "negotiationneeded" create-negotiateion-needed-handler)
    (.addEventListener "track" handle-add-track)))

(defn create-offer
  "Create an offer for the peer-connection and set its local description.
   Returns a promise of the session description protocol."
  [peer-connection]
  (-> (.createOffer peer-connection)
      (.then #(.setLocalDescription peer-connection %))
      (.then #(.. peer-connection -localDescription -sdp))))

(defn create-answer
  "Create an answer for the peer-connection and set its local description.
   Returns a promise of the session description protocol."
  [peer-connection]
  (-> (.createAnswer peer-connection)
      (.then #(.setLocalDescription peer-connection %))
      (.then #(.. peer-connection -localDescription -sdp))))

(defn set-remote-description [peer-connection description type]
  (when (and (not (.-currentRemoteDescription peer-connection)) description)
    (.setRemoteDescription peer-connection #js {:type type :sdp description})))