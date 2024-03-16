(ns app.streaming.peer-connection)

(def ice-config
  {:iceServers           [{:urls ["stun:stun1.l.google.com:19302"
                                  "stun:stun2.l.google.com:19302"]}]
   :iceCandidatePoolSize 10})

(defn- create-negotiateion-needed-handler []
  (fn [event]
    (println "on-negotiation-needed")))

(defn create-peer-connection
  "Create a new js/RTCPeerConnection and attach event listeners."
  [handle-signaling handle-add-track]
  (doto (js/RTCPeerConnection. (clj->js ice-config))
    (.addEventListener "icecandidate" handle-signaling)
    (.addEventListener "negotiationneeded" create-negotiateion-needed-handler)
    (.addEventListener "track" handle-add-track)))

(defn create-session-description
  "Create a session description protocol for the peer-connection and set its local description.
   Returns a promise of the session description protocol."
  [peer-connection]
  (-> (.createOffer peer-connection)
      (.then #(.setLocalDescription peer-connection %))
      (.then #(.. peer-connection -localDescription -sdp))))
